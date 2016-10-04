package com.law.blueinnofora;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.law.blueinnofora.bluetooth.BleAdvertisement;
import com.law.blueinnofora.bluetooth.Pdu;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by gd2 on 2015-07-02.
 * 비콘파서는 사용된다. 어떻게 비콘필드를 BLE광고로부터 디코드하는 라이브러리를 말할 때.
 *
 */
public class BeaconParser {
    private static final String TAG = "BeaconParser";
    private static final Pattern I_PATTERN = Pattern.compile("i\\:(\\d+)\\-(\\d+)(l?)");
    private static final Pattern M_PATTERN = Pattern.compile("m\\:(\\d+)-(\\d+)\\=([0-9A-Fa-f]+)");
    private static final Pattern S_PATTERN = Pattern.compile("s\\:(\\d+)-(\\d+)\\=([0-9A-Fa-f]+)");
    private static final Pattern D_PATTERN = Pattern.compile("d\\:(\\d+)\\-(\\d+)([bl]?)");
    private static final Pattern P_PATTERN = Pattern.compile("p\\:(\\d+)\\-(\\d+)\\:?([\\-\\d]+)?");
    private static final Pattern X_PATTERN = Pattern.compile("x");
    private static final char[] HEX_ARRAY = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};

    private Long mMatchingBeaconTypeCode;
    protected List<Integer> mIdentifierStartOffsets;
    protected List<Integer> mIdentifierEndOffsets;
    protected List<Boolean> mIdentifierLittleEndianFlags;
    protected List<Integer> mDataStartOffsets;
    protected List<Integer> mDataEndOffsets;
    protected List<Boolean> mDataLittleEndianFlags;
    protected Integer mMatchingBeaconTypeCodeStartOffset;
    protected Integer mMatchingBeaconTypeCodeEndOffset;
    protected Integer mServiceUuidStartOffset;
    protected Integer mServiceUuidEndOffset;
    protected Long mServiceUuid;
    protected Boolean mExtraFrame;

    protected Integer mPowerStartOffset;
    protected Integer mPowerEndOffset;
    protected Integer mDBmCorrection;
    protected int[] mHardwareAssistManufacturers = new int[] { 0x004c };

    public BeaconParser() {
        Log.i(TAG,"MY : CON : BeaconParser is constructed!!");
        mIdentifierStartOffsets = new ArrayList<Integer>();
        mIdentifierEndOffsets = new ArrayList<Integer>();
        mDataStartOffsets = new ArrayList<Integer>();
        mDataEndOffsets = new ArrayList<Integer>();
        mDataLittleEndianFlags = new ArrayList<Boolean>();
        mIdentifierLittleEndianFlags = new ArrayList<Boolean>();
    }

    public BeaconParser setBeaconLayout(String beaconLayout) {

        String[] terms =  beaconLayout.split(",");
        mExtraFrame = false; // this is not an extra frame by default

        for (String term : terms) {
            boolean found = false;

            Matcher matcher = I_PATTERN.matcher(term);
            while (matcher.find()) {
                found = true;
                try {
                    int startOffset = Integer.parseInt(matcher.group(1));
                    int endOffset = Integer.parseInt(matcher.group(2));
                    Boolean littleEndian = matcher.group(3).equals("l");
                    mIdentifierLittleEndianFlags.add(littleEndian);
                    mIdentifierStartOffsets.add(startOffset);
                    mIdentifierEndOffsets.add(endOffset);
                } catch (NumberFormatException e) {
                    throw new BeaconLayoutException("Cannot parse integer byte offset in term: " + term);
                }
            }
            matcher = D_PATTERN.matcher(term);
            while (matcher.find()) {
                found = true;
                try {
                    int startOffset = Integer.parseInt(matcher.group(1));
                    int endOffset = Integer.parseInt(matcher.group(2));
                    Boolean littleEndian = matcher.group(3).equals("l");
                    mDataLittleEndianFlags.add(littleEndian);
                    mDataStartOffsets.add(startOffset);
                    mDataEndOffsets.add(endOffset);
                } catch (NumberFormatException e) {
                    throw new BeaconLayoutException("Cannot parse integer byte offset in term: " + term);
                }
            }
            matcher = P_PATTERN.matcher(term);
            while (matcher.find()) {
                found = true;
                try {
                    int startOffset = Integer.parseInt(matcher.group(1));
                    int endOffset = Integer.parseInt(matcher.group(2));
                    int dBmCorrection = 0;
                    if (matcher.group(3) != null) {
                        dBmCorrection = Integer.parseInt(matcher.group(3));
                    }
                    mDBmCorrection=dBmCorrection;
                    mPowerStartOffset=startOffset;
                    mPowerEndOffset=endOffset;
                } catch (NumberFormatException e) {
                    throw new BeaconLayoutException("Cannot parse integer power byte offset in term: " + term);
                }
            }
            matcher = M_PATTERN.matcher(term);
            while (matcher.find()) {
                found = true;
                try {
                    int startOffset = Integer.parseInt(matcher.group(1));
                    int endOffset = Integer.parseInt(matcher.group(2));
                    mMatchingBeaconTypeCodeStartOffset = startOffset;
                    mMatchingBeaconTypeCodeEndOffset = endOffset;
                } catch (NumberFormatException e) {
                    throw new BeaconLayoutException("Cannot parse integer byte offset in term: " + term);
                }
                String hexString = matcher.group(3);
                try {
                    mMatchingBeaconTypeCode = Long.decode("0x"+hexString);
                }
                catch (NumberFormatException e) {
                    throw new BeaconLayoutException("Cannot parse beacon type code: "+hexString+" in term: " + term);
                }
            }
            matcher = S_PATTERN.matcher(term);
            while (matcher.find()) {
                found = true;
                try {
                    int startOffset = Integer.parseInt(matcher.group(1));
                    int endOffset = Integer.parseInt(matcher.group(2));
                    mServiceUuidStartOffset = startOffset;
                    mServiceUuidEndOffset = endOffset;
                } catch (NumberFormatException e) {
                    throw new BeaconLayoutException("Cannot parse integer byte offset in term: " + term);
                }
                String hexString = matcher.group(3);
                try {
                    mServiceUuid = Long.decode("0x"+hexString);
                }
                catch (NumberFormatException e) {
                    throw new BeaconLayoutException("Cannot parse serviceUuid: "+hexString+" in term: " + term);
                }
            }
            matcher = X_PATTERN.matcher(term);
            while (matcher.find()) {
                found = true;
                mExtraFrame = true;
            }

            if (!found) {
                //         LogManager.d(TAG, "cannot parse term %s", term);
                throw new BeaconLayoutException("Cannot parse beacon layout term: " + term);
            }
        }
        if (!mExtraFrame) {
            // extra frames do not have to have identifiers or power fields, but other types do
            if (mIdentifierStartOffsets.size() == 0 || mIdentifierEndOffsets.size() == 0) {
                throw new BeaconLayoutException("You must supply at least one identifier offset with a prefix of 'i'");
            }
            if (mPowerStartOffset == null || mPowerEndOffset == null) {
                throw new BeaconLayoutException("You must supply a power byte offset with a prefix of 'p'");
            }
        }
        if (mMatchingBeaconTypeCodeStartOffset == null || mMatchingBeaconTypeCodeEndOffset == null) {
            throw new BeaconLayoutException("You must supply a matching beacon type expression with a prefix of 'm'");
        }
        return this;
    }

    public int[] getHardwareAssistManufacturers() {
        return mHardwareAssistManufacturers;
    }
    public Long getMatchingBeaconTypeCode() {
        return mMatchingBeaconTypeCode;
    }
    public int getMatchingBeaconTypeCodeStartOffset() {
        return mMatchingBeaconTypeCodeStartOffset;
    }
    public int getMatchingBeaconTypeCodeEndOffset() {
        return mMatchingBeaconTypeCodeEndOffset;
    }
    public Long getServiceUuid() {
        return mServiceUuid;
    }
    public int getMServiceUuidStartOffset() {
        return mServiceUuidStartOffset;
    }
    public int getServiceUuidEndOffset() {
        return mServiceUuidEndOffset;
    }

    @TargetApi(5)
    public Beacon fromScanData(byte[] scanData, int rssi, BluetoothDevice device) {
        return fromScanData(scanData, rssi, device, new Beacon());
    }
    @TargetApi(5)
    protected Beacon fromScanData(byte[] bytesToProcess, int rssi, BluetoothDevice device, Beacon beacon) {

        BleAdvertisement advert = new BleAdvertisement(bytesToProcess);
        Pdu pduToParse = null;
        for (Pdu pdu: advert.getPdus()) {
            if (pdu.getType() == Pdu.GATT_SERVICE_UUID_PDU_TYPE ||
                    pdu.getType() == Pdu.MANUFACTURER_DATA_PDU_TYPE) {
                pduToParse = pdu;
                //   if (LogManager.isVerboseLoggingEnabled()) {
                //       LogManager.d(TAG, "Processing pdu type %02X: %s with startIndex: %d, endIndex: %d", pdu.getType(), bytesToHex(bytesToProcess), pdu.getStartIndex(), pdu.getEndIndex());
                //   }
                break;
            }
            else {
                //     if (LogManager.isVerboseLoggingEnabled()) {
                //        LogManager.d(TAG, "Ignoring pdu type %02X", pdu.getType());
                //     }
            }
        }
        if (pduToParse == null) {
            //       if (LogManager.isVerboseLoggingEnabled()) {
            //         LogManager.d(TAG, "No PDUs to process in this packet.");
            //    }
            return null;
        }

        byte[] serviceUuidBytes = null;
        byte[] typeCodeBytes = longToByteArray(getMatchingBeaconTypeCode(), mMatchingBeaconTypeCodeEndOffset - mMatchingBeaconTypeCodeStartOffset + 1);
        if (getServiceUuid() != null) {
            serviceUuidBytes = longToByteArray(getServiceUuid(), mServiceUuidEndOffset - mServiceUuidStartOffset + 1, false);
        }
        int startByte = pduToParse.getStartIndex();
        boolean patternFound = false;

        if (getServiceUuid() == null) {
            if (byteArraysMatch(bytesToProcess, startByte + mMatchingBeaconTypeCodeStartOffset, typeCodeBytes, 0)) {
                patternFound = true;
            }
        } else {
            if (byteArraysMatch(bytesToProcess, startByte + mServiceUuidStartOffset, serviceUuidBytes, 0) &&
                    byteArraysMatch(bytesToProcess, startByte + mMatchingBeaconTypeCodeStartOffset, typeCodeBytes, 0)) {
                patternFound = true;
            }
        }

        if (patternFound == false) {
            // This is not a beacon
            if (getServiceUuid() == null) {
                //           if (LogManager.isVerboseLoggingEnabled()) {
                //                  LogManager.d(TAG, "This is not a matching Beacon advertisement. (Was expecting %s.  "
                //                               + "The bytes I see are: %s", byteArrayToString(typeCodeBytes),
                //                     bytesToHex(bytesToProcess));

                //       }
            } else {
                //       if (LogManager.isVerboseLoggingEnabled()) {
                //             LogManager.d(TAG, "This is not a matching Beacon advertisement. Was expecting %s.  "
                //                               + "The bytes I see are: %s", byteArrayToString(serviceUuidBytes),
                //                        byteArrayToString(typeCodeBytes),
                //                      bytesToHex(bytesToProcess));
                //           }
            }

            return null;
        } else {
            //  if (LogManager.isVerboseLoggingEnabled()) {
            //        LogManager.d(TAG, "This is a recognized beacon advertisement -- %s seen",
            //                    byteArrayToString(typeCodeBytes));
            //        }
        }

        ArrayList<Identifier> identifiers = new ArrayList<Identifier>();
        for (int i = 0; i < mIdentifierEndOffsets.size(); i++) {
            int endIndex = mIdentifierEndOffsets.get(i) + startByte + 1;
            if (endIndex > pduToParse.getEndIndex()+1) {
                endIndex = pduToParse.getEndIndex()+1; // truncate identifier if it goes over the end of the pdu
            }
            Identifier identifier = Identifier.fromBytes(bytesToProcess, mIdentifierStartOffsets.get(i) + startByte, endIndex, mIdentifierLittleEndianFlags.get(i));
            identifiers.add(identifier);
        }
        ArrayList<Long> dataFields = new ArrayList<Long>();
        for (int i = 0; i < mDataEndOffsets.size(); i++) {
            int endIndex = mDataEndOffsets.get(i) + startByte;
            if (endIndex > pduToParse.getEndIndex()) {
                endIndex = pduToParse.getEndIndex(); // truncate  if it goes over the end of the pdu
            }
            String dataString = byteArrayToFormattedString(bytesToProcess, mDataStartOffsets.get(i) + startByte, endIndex, mDataLittleEndianFlags.get(i));
            dataFields.add(Long.parseLong(dataString));
            // TODO: error handling needed here on the parse
        }

        if (mPowerStartOffset != null) {
            int txPower = 0;
            String powerString = byteArrayToFormattedString(bytesToProcess, mPowerStartOffset + startByte, mPowerEndOffset + startByte, false);
            try {
                txPower = Integer.parseInt(powerString)+mDBmCorrection;
            }
            catch (NumberFormatException e1) {
                // keep default value
            }
            catch (NullPointerException e2) {
                // keep default value
            }
            // make sure it is a signed integer
            if (txPower > 127) {
                txPower -= 256;
            }
            // TODO: error handling needed on the parse
            beacon.mTxPower = txPower;
        }


        int beaconTypeCode = 0;
        String beaconTypeString = byteArrayToFormattedString(bytesToProcess, mMatchingBeaconTypeCodeStartOffset+startByte, mMatchingBeaconTypeCodeEndOffset+startByte, false);
        beaconTypeCode = Integer.parseInt(beaconTypeString);
        // TODO: error handling needed on the parse

        int manufacturer = 0;
        String manufacturerString = byteArrayToFormattedString(bytesToProcess, startByte, startByte+1, true);
        manufacturer = Integer.parseInt(manufacturerString);

        String macAddress = null;
        String name = null;
        if (device != null) {
            macAddress = device.getAddress();
            name = device.getName();
        }

        beacon.mIdentifiers = identifiers;
        beacon.mDataFields = dataFields;
        beacon.mRssi = rssi;
        beacon.mBeaconTypeCode = beaconTypeCode;
        if (mServiceUuid != null) {
            beacon.mServiceUuid = (int) mServiceUuid.longValue();
        }
        else {
            beacon.mServiceUuid = -1;
        }

        beacon.mBluetoothAddress = macAddress;
        beacon.mBluetoothName= name;
        beacon.mManufacturer = manufacturer;
        return beacon;
    }

    //비콘으로부터 광고를 얻는다.
    public byte[] getBeaconAdvertisementData(Beacon beacon) {
        byte[] advertisingBytes;

        int lastIndex = -1;
        if (mMatchingBeaconTypeCodeEndOffset != null && mMatchingBeaconTypeCodeEndOffset > lastIndex) {
            lastIndex = mMatchingBeaconTypeCodeEndOffset;
        }
        if (mPowerEndOffset != null && mPowerEndOffset > lastIndex) {
            lastIndex = mPowerEndOffset;
        }
        for (int identifierNum = 0; identifierNum < this.mIdentifierStartOffsets.size(); identifierNum++) {
            if (this.mIdentifierEndOffsets.get(identifierNum) != null && this.mIdentifierEndOffsets.get(identifierNum) > lastIndex) {
                lastIndex = this.mIdentifierEndOffsets.get(identifierNum);
            }
        }
        for (int identifierNum = 0; identifierNum < this.mDataEndOffsets.size(); identifierNum++) {
            if (this.mDataEndOffsets.get(identifierNum) != null && this.mDataEndOffsets.get(identifierNum) > lastIndex) {
                lastIndex = this.mDataEndOffsets.get(identifierNum);
            }
        }

        advertisingBytes = new byte[lastIndex+1-2];
        long beaconTypeCode = this.getMatchingBeaconTypeCode();

        // set type code
        for (int index = this.mMatchingBeaconTypeCodeStartOffset; index <= this.mMatchingBeaconTypeCodeEndOffset; index++) {
            byte value = (byte) (this.getMatchingBeaconTypeCode() >> (8*(this.mMatchingBeaconTypeCodeEndOffset-index)) & 0xff);
            advertisingBytes[index-2] = value;
        }

        // set identifiers
        for (int identifierNum = 0; identifierNum < this.mIdentifierStartOffsets.size(); identifierNum++) {
            byte[] identifierBytes = beacon.getIdentifier(identifierNum).toByteArrayOfSpecifiedEndianness(this.mIdentifierLittleEndianFlags.get(identifierNum));
            for (int index = this.mIdentifierStartOffsets.get(identifierNum); index <= this.mIdentifierEndOffsets.get(identifierNum); index ++) {
                int identifierByteIndex = this.mIdentifierEndOffsets.get(identifierNum)-index;
                if (identifierByteIndex < identifierBytes.length) {
                    advertisingBytes[index-2] = (byte) identifierBytes[this.mIdentifierEndOffsets.get(identifierNum)-index];
                }
                else {
                    advertisingBytes[index-2] = 0;
                }
            }
        }

        // set power
        for (int index = this.mPowerStartOffset; index <= this.mPowerEndOffset; index ++) {
            advertisingBytes[index-2] = (byte) (beacon.getTxPower() >> (8*(index - this.mPowerStartOffset)) & 0xff);
        }

        // set data fields
        for (int dataFieldNum = 0; dataFieldNum < this.mDataStartOffsets.size(); dataFieldNum++) {
            long dataField = beacon.getDataFields().get(dataFieldNum);
            for (int index = this.mDataStartOffsets.get(dataFieldNum); index <= this.mDataEndOffsets.get(dataFieldNum); index ++) {
                int endianCorrectedIndex = index;
                if (this.mDataLittleEndianFlags.get(dataFieldNum)) {
                    endianCorrectedIndex = this.mDataEndOffsets.get(dataFieldNum) - index;
                }
                advertisingBytes[endianCorrectedIndex-2] = (byte) (dataField >> (8*(index - this.mDataStartOffsets.get(dataFieldNum))) & 0xff);
            }
        }
        return advertisingBytes;
    }

    //
    public BeaconParser setMatchingBeaconTypeCode(Long typeCode) {
        mMatchingBeaconTypeCode = typeCode;
        return this;
    }

    //계산한다. 바이트의 사이즈를
    public int getIdentifierByteCount(int identifierNum) {
        return mIdentifierEndOffsets.get(identifierNum) - mIdentifierStartOffsets.get(identifierNum) + 1;
    }

    //비콘의 identifier의 숫자를 리턴한다.
    public int getIdentifierCount() {
        return mIdentifierStartOffsets.size();
    }

    //비콘의 데이타필드 수를 리턴한다.
    public int getDataFieldCount() {
        return mDataStartOffsets.size();
    }

    //
    protected static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static class BeaconLayoutException extends RuntimeException {
        public BeaconLayoutException(String s) {
            super(s);
        }
    }

    public static byte[] longToByteArray(long longValue, int length) {
        return longToByteArray(longValue, length, true);
    }

    public static byte[] longToByteArray(long longValue, int length, boolean bigEndian) {
        byte[] array = new byte[length];
        for (int i = 0; i < length; i++){
            int adjustedI = bigEndian ? i : length - i -1;
            long mask = 0xffl << (length-adjustedI-1)*8;
            long shift = (length-adjustedI-1)*8;
            long value = ((longValue & mask)  >> shift);
            array[i] = (byte) value;
        }
        return array;
    }
    private boolean byteArraysMatch(byte[] array1, int offset1, byte[] array2, int offset2) {
        int minSize = array1.length > array2.length ? array2.length : array1.length;
        for (int i = 0; i <  minSize; i++) {
            if (array1[i+offset1] != array2[i+offset2]) {
                return false;
            }
        }
        return true;
    }
    private String byteArrayToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02x", bytes[i]));
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    private String byteArrayToFormattedString(byte[] byteBuffer, int startIndex, int endIndex, boolean littleEndian) {
        byte[] bytes = new byte[endIndex-startIndex+1];
        if (littleEndian) {
            for (int i = 0; i <= endIndex-startIndex; i++) {
                bytes[i] = byteBuffer[startIndex+bytes.length-1-i];
            }
        }
        else {
            for (int i = 0; i <= endIndex-startIndex; i++) {
                bytes[i] = byteBuffer[startIndex+i];
            }
        }


        int length = endIndex-startIndex +1;
        // We treat a 1-4 byte number as decimal string
        if (length < 5) {
            long number = 0l;
            for (int i = 0; i < bytes.length; i++)  {
                long byteValue = (long) (bytes[bytes.length - i-1] & 0xff);
                long positionValue = (long) Math.pow(256.0,i*1.0);
                long calculatedValue =  (byteValue * positionValue);
                number += calculatedValue;
            }
            return Long.toString(number);
        }

        // We treat a 7+ byte number as a hex string
        String hexString = bytesToHex(bytes);

        // And if it is a 12 byte number we add dashes to it to make it look like a standard UUID
        if (bytes.length == 16) {
            StringBuilder sb = new StringBuilder();
            sb.append(hexString.substring(0,8));
            sb.append("-");
            sb.append(hexString.substring(8,12));
            sb.append("-");
            sb.append(hexString.substring(12,16));
            sb.append("-");
            sb.append(hexString.substring(16,20));
            sb.append("-");
            sb.append(hexString.substring(20,32));
            return sb.toString();
        }
        return "0x"+hexString;
    }
}

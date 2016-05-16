package com.law.blueinnofora.bluetooth;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

/**
 * Created by gd2 on 2015-07-02.
 * BLE스캔으로부터의 byte string을 payload data unit으로 바꾼다.
 */
public class Pdu {
    private static final String  TAG = "Pdu";
    public static final byte MANUFACTURER_DATA_PDU_TYPE = (byte) 0xff;
    public static final byte GATT_SERVICE_UUID_PDU_TYPE = (byte) 0x16;

    private byte mType;
    private int mDeclaredLength;
    private int mStartIndex;
    private int mEndIndex;
    private byte[] mBytes;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static Pdu parse(byte[] bytes, int startIndex) {
//        Log.i(TAG, "MY : about PDU!!");
        Pdu pdu = null;
        if (bytes.length-startIndex >= 2) {
            byte length = bytes[startIndex];
            if (length > 0) {
                byte type = bytes[startIndex + 1];
                int firstIndex = startIndex + 2;
                if (firstIndex < bytes.length) {
                    pdu = new Pdu();
                    pdu.mEndIndex = firstIndex + length - 2;
                    if (pdu.mEndIndex >= bytes.length) {
                        pdu.mEndIndex = bytes.length - 1;
                    }
                    pdu.mType = type;
                    pdu.mDeclaredLength = length;
                    pdu.mStartIndex = firstIndex;
                    pdu.mBytes = bytes;
                }
            }
        }
        return pdu;
    }


    /**
     * PDU type field
     * @return
     */
    public byte getType() {
        return mType;
    }

    /**
     * PDU length from header
     * @return
     */
    public int getDeclaredLength() {
        return mDeclaredLength;
    }

    /**
     * Actual PDU length (may be less than declared length if fewer bytes are actually available.)
     * @return
     */
    public int getActualLength() {
        return mEndIndex - mStartIndex + 1;
    }

    /**
     * Start index within byte buffer of PDU
     * @return
     */
    public int getStartIndex() {
        return mStartIndex;
    }

    /**
     * End index within byte buffer of PDU
     * @return
     */
    public int getEndIndex() {
        return mEndIndex;
    }

}

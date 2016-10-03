package com.law.blueinnofora;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.law.blueinnofora.client.BeaconDataFactory;
import com.law.blueinnofora.client.NullBeaconDataFactory;
import com.law.blueinnofora.distance.DistanceCalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.LogManager;

/**
 * Created by gd2 on 2015-07-02.
 * ???? ??????? ??????? ???????? ???? ???? ????? ?????? ?????? ???????.
 * ?????? ??? ????? ????? identifier?? ?????????.
 * ?????? BLE ???? ??????? ???? ?????? identifier?? ???, ????? ???? ?????? ???.
 */
public class Beacon implements Parcelable {
    private static final String TAG = "BeaconClass";

    private static final List<Long> UNMODIFIABLE_LIST_OF_LONG = Collections.unmodifiableList(new ArrayList<Long>());
    private static final List<Identifier> UNMODIFIABLE_LIST_OF_IDENTIFIER = Collections.unmodifiableList(new ArrayList<Identifier>());

    //???????? ???? ?? ?????? ???? ???????
    protected static boolean sHardwareEqualityEnforced = false;
    protected static DistanceCalculator sDistanceCalculator = null;

    //identifier?? ????
    protected List<Identifier> mIdentifiers;
    //?????
    protected List<Long> mDataFields;
    protected List<Long> mExtraDataFields;
    //???
    protected Double mDistance;
    //??? ??????
    protected int mRssi;
    //???
    protected int mTxPower;
    //?????
    protected String mBluetoothAddress;
    //???? ????
    private Double mRunningAverageRssi = null;
    //???? ????? ?????
    protected static BeaconDataFactory beaconDataFactory = new NullBeaconDataFactory();
    //???? ????? ??????? ????? ????? ??? ? ???? ?????? ?????? ??????? ????????? ???
    protected int mBeaconTypeCode;
    //???? ?????? ?????
    protected int mManufacturer;
    //32??? ???? UUID GATT?? ????????? ??????? ????
    protected int mServiceUuid = -1;
    //??????? ?????? ????
    protected String mBluetoothName;

    //????? ??????? ???
    public static final Parcelable.Creator<Beacon> CREATOR
            = new Parcelable.Creator<Beacon>() {
        public Beacon createFromParcel(Parcel in) {
            return new Beacon(in);
        }

        public Beacon[] newArray(int size) {
            return new Beacon[size];
        }
    };

    //??? ??? ????
    public static void setDistanceCalculator(DistanceCalculator dc) {
        sDistanceCalculator = dc;
    }
    public static DistanceCalculator getDistanceCalculator() {
        return sDistanceCalculator;
    }

    //????? ???? ??? ???
    public static void setHardwareEqualityEnforced(boolean e) {
        sHardwareEqualityEnforced = e;
    }

    protected Beacon(Parcel in) {
        Log.i(TAG,"MY : CON : Beacon constructed!!");
        int size = in.readInt();

        this.mIdentifiers = new ArrayList<Identifier>(size);
        for (int i = 0; i < size; i++) {
            mIdentifiers.add(Identifier.parse(in.readString()));
        }
        mDistance = in.readDouble();
        mRssi = in.readInt();
        mTxPower = in.readInt();
        mBluetoothAddress = in.readString();
        mBeaconTypeCode = in.readInt();
        mServiceUuid = in.readInt();
        int dataSize = in.readInt();
        this.mDataFields = new ArrayList<Long>(dataSize);
        for (int i = 0; i < dataSize; i++) {
            mDataFields.add(in.readLong());
        }
        int extraDataSize = in.readInt();
     //   if (LogManager.isVerboseLoggingEnabled()) {
     //       LogManager.d(TAG, "reading "+extraDataSize+" extra data fields from parcel");
     //   }
        this.mExtraDataFields = new ArrayList<Long>(extraDataSize);
        for (int i = 0; i < extraDataSize; i++) {
            mExtraDataFields.add(in.readLong());
        }
        mManufacturer = in.readInt();
        mBluetoothName = in.readString();
    }

    protected Beacon(Beacon otherBeacon) {
        super();
        mIdentifiers = new ArrayList<Identifier>(otherBeacon.mIdentifiers.size());
        mDataFields = new ArrayList<Long>(otherBeacon.mDataFields.size());
        mExtraDataFields = new ArrayList<Long>(otherBeacon.mExtraDataFields.size());
        for (int i = 0; i < otherBeacon.mIdentifiers.size(); i++) {
            mIdentifiers.add(otherBeacon.mIdentifiers.get(i));
        }
        for (int i = 0; i < otherBeacon.mDataFields.size(); i++) {
            mDataFields.add(otherBeacon.mDataFields.get(i));
        }
        for (int i = 0; i < otherBeacon.mExtraDataFields.size(); i++) {
            mExtraDataFields.add(otherBeacon.mExtraDataFields.get(i));
        }
        this.mDistance = otherBeacon.mDistance;
        this.mRunningAverageRssi = otherBeacon.mRunningAverageRssi;
        this.mRssi = otherBeacon.mRssi;
        this.mTxPower = otherBeacon.mTxPower;
        this.mBluetoothAddress = otherBeacon.mBluetoothAddress;
        this.mBeaconTypeCode = otherBeacon.getBeaconTypeCode();
        this.mServiceUuid = otherBeacon.getServiceUuid();
        this.mBluetoothName = otherBeacon.mBluetoothName;
    }
    protected Beacon() {
        mIdentifiers = new ArrayList<Identifier>(1);
        mDataFields = new ArrayList<Long>(1);
        mExtraDataFields = new ArrayList<Long>(1);
    }

    //???? ??? rssi?? ???????? ???????? ?????
    public void setRunningAverageRssi(double rssi) {
        mRunningAverageRssi = rssi;
        mDistance = null; // force calculation of accuracy and proximity next time they are requested
    }

    //???? ??? ?????? rssi ????
    public void setRssi(int rssi) {
        mRssi = rssi;
    }

    public int getManufacturer() {
        return mManufacturer;
    }
    public int getServiceUuid() {
        return mServiceUuid;
    }

    public Identifier getIdentifier(int i) {
        return mIdentifiers.get(i);
    }
    public Identifier getId2() {
        return mIdentifiers.get(1);
    }
    public Identifier getId3() {
        return mIdentifiers.get(2);
    }
    public List<Long> getDataFields() {
        if (mDataFields.getClass().isInstance(UNMODIFIABLE_LIST_OF_LONG)) {
            return mDataFields;
        }
        else {
            return Collections.unmodifiableList(mDataFields);
        }
    }
    public List<Long> getExtraDataFields() {
        if (mExtraDataFields.getClass().isInstance(UNMODIFIABLE_LIST_OF_LONG)) {
            return mExtraDataFields;
        }
        else {
            return Collections.unmodifiableList(mExtraDataFields);
        }
    }
    public void setExtraDataFields(List<Long> fields) {
        mExtraDataFields = fields;
    }
    public List<Identifier> getIdentifiers() {
        if (mIdentifiers.getClass().isInstance(UNMODIFIABLE_LIST_OF_IDENTIFIER)) {
            return mIdentifiers;
        }
        else {
            return Collections.unmodifiableList(mIdentifiers);
        }
    }
    //??????
    public double getDistance() {
        if (mDistance == null) {
            double bestRssiAvailable = mRssi;
            if (mRunningAverageRssi != null) {
                bestRssiAvailable = mRunningAverageRssi;
            }
            else {
            //    LogManager.d(TAG, "Not using running average RSSI because it is null");
            }
            mDistance = calculateDistance(mTxPower, bestRssiAvailable);
        }
        return mDistance;
    }

    public int getRssi() {
        return mRssi;
    }
    public int getTxPower() {
        return mTxPower;
    }
    public int getBeaconTypeCode() { return mBeaconTypeCode; }
    public String getBluetoothAddress() {
        return mBluetoothAddress;
    }
    public String getBluetoothName() {
        return mBluetoothName;
    }

    //?????? ?????? ??????.
    @Override
    public int hashCode() {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (Identifier identifier: mIdentifiers) {
            sb.append("id");
            sb.append(i);
            sb.append(": ");
            sb.append(identifier.toString());
            sb.append(" ");
            i++;
        }
        if (sHardwareEqualityEnforced) {
            sb.append(mBluetoothAddress);
        }
        return sb.toString().hashCode();
    }

    //????? ???? ?????? ??????(???? identifier?? ??????) rssi?? distance?? ???????.
    @Override
    public boolean equals(Object that) {
        if (!(that instanceof Beacon)) {
            return false;
        }
        Beacon thatBeacon = (Beacon) that;
        if (this.mIdentifiers.size() != thatBeacon.mIdentifiers.size()) {
            return false;
        }
        // all identifiers must match
        for (int i = 0; i < this.mIdentifiers.size(); i++) {
            if (!this.mIdentifiers.get(i).equals(thatBeacon.mIdentifiers.get(i))) {
                return false;
            }
        }
        return sHardwareEqualityEnforced ?
                this.getBluetoothAddress().equals(thatBeacon.getBluetoothAddress()) :
                true;
    }

    //?????? ????????? ??????? ?????.
    public void requestData(BeaconDataNotifier notifier) {
        beaconDataFactory.requestBeaconData(this, notifier);
    }

    //unique identifier?? ????????? ???????.
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (Identifier identifier: mIdentifiers) {
            if (i > 1) {
                sb.append(" ");
            }
            sb.append("id");
            sb.append(i);
            sb.append(": ");
            sb.append(identifier == null ? "null" : identifier.toString());
            i++;
        }
        return sb.toString();
    }

    //???????? for parcelabel
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mIdentifiers.size());
  //      LogManager.d(TAG, "serializing identifiers of size %s", mIdentifiers.size());
        Log.i(TAG,"Beacon : serializing identifiers of size");

        for (Identifier identifier: mIdentifiers) {
            out.writeString(identifier == null ? null : identifier.toString());
        }
        out.writeDouble(getDistance());
        out.writeInt(mRssi);
        out.writeInt(mTxPower);
        out.writeString(mBluetoothAddress);
        out.writeInt(mBeaconTypeCode);
        out.writeInt(mServiceUuid);
        out.writeInt(mDataFields.size());
        for (Long dataField: mDataFields) {
            out.writeLong(dataField);
        }
   //     if (LogManager.isVerboseLoggingEnabled()) {
    //        LogManager.d(TAG, "writing "+mExtraDataFields.size()+" extra data fields to parcel");
    //    }
        out.writeInt(mExtraDataFields.size());
        for (Long dataField: mExtraDataFields) {
            out.writeLong(dataField);
        }
        out.writeInt(mManufacturer);
        out.writeString(mBluetoothName);

    }
    //?? ?????? ??????? ????? ???????? ???????.
    public boolean isExtraBeaconData() {
        return mIdentifiers.size() == 0 && mDataFields.size() != 0;
    }

    protected static Double calculateDistance(int txPower, double bestRssiAvailable) {
        if (Beacon.getDistanceCalculator() != null) {
            return Beacon.getDistanceCalculator().calculateDistance(txPower, bestRssiAvailable);
        }
        else {
        //    LogManager.e(TAG, "Distance calculator not set.  Distance will bet set to -1");
            Log.i(TAG,"Beacon : Distance calculator not set.  Distance will bet set to -1");
            return -1.0;
        }
    }

    /**
     * Builder class for Beacon objects. Provides a convenient way to set the various fields of a
     * Beacon
     *
     * <p>Example:
     *
     * <pre>
     * Beacon beacon = new Beacon.Builder()
     *         .setId1(&quot;2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6&quot;)
     *         .setId2("1")
     *         .setId3("2")
     *         .build();
     * </pre>
     */
    public static class Builder {
        protected Beacon mBeacon;
        private Identifier mId1, mId2, mId3;

        /**
         * Creates a builder instance
         */
        public Builder() {
            mBeacon = new Beacon();
        }

        /**
         * Builds an instance of this beacon based on parameters set in the Builder
         * @return beacon
         */
        public Beacon build() {
            if (mId1!= null) {
                mBeacon.mIdentifiers.add(mId1);
                if (mId2!= null) {
                    mBeacon.mIdentifiers.add(mId2);
                    if (mId3!= null) {
                        mBeacon.mIdentifiers.add(mId3);
                    }
                }
            }
            return mBeacon;
        }

        /**
         * @param beacon the beacon whose fields we should copy to this beacon builder
         * @return
         */
        public Builder copyBeaconFields(Beacon beacon) {
            setIdentifiers(beacon.getIdentifiers());
            setBeaconTypeCode(beacon.getBeaconTypeCode());
            setDataFields(beacon.getDataFields());
            setBluetoothAddress(beacon.getBluetoothAddress());
            setBluetoothName(beacon.getBluetoothName());
            setExtraDataFields(beacon.getExtraDataFields());
            setManufacturer(beacon.getManufacturer());
            setTxPower(beacon.getTxPower());
            setRssi(beacon.getRssi());
            setServiceUuid(beacon.getServiceUuid());
            return this;
        }

        /**
         * @see Beacon#mIdentifiers
         * @param identifiers identifiers to set
         * @return builder
         */
        public Builder setIdentifiers(List<Identifier>identifiers) {
            mId1 = null;
            mId2 = null;
            mId3 = null;
            mBeacon.mIdentifiers = identifiers;
            return this;
        }

        /**
         * Convenience method allowing the first beacon identifier to be set as a String.  It will
         * be parsed into an Identifier object
         * @param id1String string to parse into an identifier
         * @return builder
         */
        public Builder setId1(String id1String) {
            mId1 = Identifier.parse(id1String);
            return this;
        }

        /**
         * Convenience method allowing the second beacon identifier to be set as a String.  It will
         * be parsed into an Identifier object
         * @param id2String string to parse into an identifier
         * @return builder
         */
        public Builder setId2(String id2String) {
            mId2 = Identifier.parse(id2String);
            return this;
        }

        /**
         * Convenience method allowing the third beacon identifier to be set as a String.  It will
         * be parsed into an Identifier object
         * @param id3String string to parse into an identifier
         * @return builder
         */
        public Builder setId3(String id3String) {
            mId3 = Identifier.parse(id3String);
            return this;
        }

        /**
         * @see Beacon#mRssi
         * @param rssi
         * @return builder
         */
        public Builder setRssi(int rssi) {
            mBeacon.mRssi = rssi;
            return this;
        }

        /**
         * @see Beacon#mTxPower
         * @param txPower
         * @return builder
         */
        public Builder setTxPower(int txPower) {
            mBeacon.mTxPower = txPower;
            return this;
        }

        /**
         * @see Beacon#mBeaconTypeCode
         * @param beaconTypeCode
         * @return builder
         */
        public Builder setBeaconTypeCode(int beaconTypeCode) {
            mBeacon.mBeaconTypeCode = beaconTypeCode;
            return this;
        }

        /**
         * @see Beacon#mServiceUuid
         * @param serviceUuid
         * @return builder
         */
        public Builder setServiceUuid(int serviceUuid) {
            mBeacon.mServiceUuid = serviceUuid;
            return this;
        }

        /**
         * @see Beacon#mBluetoothAddress
         * @param bluetoothAddress
         * @return builder
         */
        public Builder setBluetoothAddress(String bluetoothAddress) {
            mBeacon.mBluetoothAddress = bluetoothAddress;
            return this;
        }

        /**
         * @see Beacon#mDataFields
         * @param dataFields
         * @return builder
         */
        public Builder setDataFields(List<Long> dataFields) {
            mBeacon.mDataFields = dataFields;
            return this;
        }

        /**
         * @see Beacon#mDataFields
         * @param extraDataFields
         * @return builder
         */
        public Builder setExtraDataFields(List<Long> extraDataFields) {
            mBeacon.mExtraDataFields = extraDataFields;
            return this;
        }

        /**
         * @see Beacon#mManufacturer
         * @param manufacturer
         * @return builder
         */
        public Builder setManufacturer(int manufacturer) {
            mBeacon.mManufacturer = manufacturer;
            return this;
        }

        /**
         * @see Beacon#mBluetoothName
         * @param name
         * @return builder
         */
        public Builder setBluetoothName(String name) {
            mBeacon.mBluetoothName = name;
            return this;
        }

    }


}

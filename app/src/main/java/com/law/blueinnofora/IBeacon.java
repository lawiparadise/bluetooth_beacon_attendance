package com.law.blueinnofora;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by gd2 on 2015-07-02.
 */
public class IBeacon extends Beacon {
    protected static final String TAG = "IBeacon";

    //비콘이 파르셀라벨 임플리먼트 하기에 해야하는것
    public static final Parcelable.Creator<IBeacon> CREATOR
            = new Parcelable.Creator<IBeacon>() {
        public IBeacon createFromParcel(Parcel in) {
            return new IBeacon(in);
        }

        public IBeacon[] newArray(int size) {
            return new IBeacon[size];
        }
    };

    protected IBeacon(Beacon beacon) {
        super();
        this.mBluetoothAddress = beacon.mBluetoothAddress;
        this.mIdentifiers = beacon.mIdentifiers;
        this.mBeaconTypeCode = beacon.mBeaconTypeCode;
        this.mDataFields = beacon.mDataFields;
        this.mDistance = beacon.mDistance;
        this.mRssi = beacon.mRssi;
        this.mTxPower = beacon.mTxPower;
    }

    protected IBeacon() {
    }

    //파르셀 때문에 필요한것
    protected IBeacon(Parcel in) {
        super(in);
    }
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
    }


    //제조사 구분 리턴
    public int getMfgReserved() {
        return mDataFields.get(0).intValue();
    }


    /**
     * Builder class for AltBeacon objects. Provides a convenient way to set the various fields of a
     * Beacon
     *
     * <p>Example:
     *
     * <pre>
     * Beacon beacon = new Beacon.Builder()
     *         .setId1(&quot;2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6&quot;)
     *         .setId2("1")
     *         .setId3("2")
     *         .setMfgReserved(3);
     *         .build();
     * </pre>
     */
    public static class Builder extends Beacon.Builder {
        @Override
        public Beacon build() {
            return new IBeacon(super.build());
        }
        public Builder setMfgReserved(int mfgReserved) {
            if (mBeacon.mDataFields.size() != 0) {
                mBeacon.mDataFields = new ArrayList<Long>();
            }
            mBeacon.mDataFields.add((long)mfgReserved);
            return this;
        }
    }


}

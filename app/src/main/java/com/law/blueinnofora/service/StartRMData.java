package com.law.blueinnofora.service;

import android.os.Parcel;
import android.os.Parcelable;

import com.law.blueinnofora.Region;

/**
 * Created by gd2 on 2015-07-02.
 */
public class StartRMData implements Parcelable {
    private Region region;
    private long scanPeriod;
    private long betweenScanPeriod;
    private boolean backgroundFlag;
    private String callbackPackageName;

    public StartRMData(Region region, String callbackPackageName) {
        this.region = region;
        this.callbackPackageName = callbackPackageName;
    }
    public StartRMData(long scanPeriod, long betweenScanPeriod, boolean backgroundFlag) {
        this.scanPeriod = scanPeriod;
        this.betweenScanPeriod = betweenScanPeriod;
        this.backgroundFlag = backgroundFlag;
    }

    public StartRMData(Region region, String callbackPackageName, long scanPeriod, long betweenScanPeriod, boolean backgroundFlag) {
        this.scanPeriod = scanPeriod;
        this.betweenScanPeriod = betweenScanPeriod;
        this.region = region;
        this.callbackPackageName = callbackPackageName;
        this.backgroundFlag = backgroundFlag;
    }

    public long getScanPeriod() { return scanPeriod; }
    public long getBetweenScanPeriod() { return betweenScanPeriod; }
    public Region getRegionData() {
        return region;
    }
    public String getCallbackPackageName() {
        return callbackPackageName;
    }
    public boolean getBackgroundFlag() { return backgroundFlag; }
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(region, flags);
        out.writeString(callbackPackageName);
        out.writeLong(scanPeriod);
        out.writeLong(betweenScanPeriod);
        out.writeByte((byte) (backgroundFlag ? 1 : 0));
    }

    public static final Parcelable.Creator<StartRMData> CREATOR
            = new Parcelable.Creator<StartRMData>() {
        public StartRMData createFromParcel(Parcel in) {
            return new StartRMData(in);
        }

        public StartRMData[] newArray(int size) {
            return new StartRMData[size];
        }
    };

    private StartRMData(Parcel in) {
        region = in.readParcelable(StartRMData.class.getClassLoader());
        callbackPackageName = in.readString();
        scanPeriod = in.readLong();
        betweenScanPeriod = in.readLong();
        backgroundFlag = in.readByte() != 0;
    }
}

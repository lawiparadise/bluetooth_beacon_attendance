package com.law.blueinnofora;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gd2 on 2015-07-02.
 * 비콘과 만나는 지역을 내가 설정해주는듯?
 */
public class Region implements Parcelable{
    private static final String TAG = "Region";

    //걍 만들어야하는것
    public static final Parcelable.Creator<Region> CREATOR = new Parcelable.Creator<Region>(){
        public Region createFromParcel(Parcel in) {
            return new Region(in);
        }

        public Region[] newArray(int size){
            return new Region[size];
        }
    };

    protected final List<Identifier> mIdentifiers;
    protected final String mUniqueId;

    //레인징 또는 모니터링에 쓰기위해 레지온을 만든다.
    public Region(String uniqueId, Identifier id1, Identifier id2, Identifier id3){
        Log.i(TAG, "MY : CON : Region is constructed!!");
        this.mIdentifiers = new ArrayList<Identifier>(3);
        this.mIdentifiers.add(id1);
        this.mIdentifiers.add(id2);
        this.mIdentifiers.add(id3);
        this.mUniqueId = uniqueId;
        if(uniqueId==null){
            throw new NullPointerException("uniqueId may not be null");
        }

    }
    public Region(String uniqueId, List<Identifier> identifiers) {
        Log.i(TAG, "MY : CON2 : Region is constructed!!");
        this.mIdentifiers = new ArrayList<Identifier>(identifiers);
        this.mUniqueId = uniqueId;
        if (uniqueId == null) {
            throw new NullPointerException("uniqueId may not be null");
        }
    }
    public Identifier getId1() {
        return getIdentifier(0);
    }
    public Identifier getId2() {
        return getIdentifier(1);
    }
    public Identifier getId3() {
        return getIdentifier(2);
    }
    public Identifier getIdentifier(int i) {
        return mIdentifiers.size() > i ? mIdentifiers.get(i) : null;
    }
    public String getUniqueId() {
        return mUniqueId;
    }

    public boolean matchesBeacon(Beacon beacon) {
        // all identifiers must match, or the region identifier must be null
        for (int i = 0; i < this.mIdentifiers.size(); i++) {
            if (beacon.getIdentifiers().size() <= i && mIdentifiers.get(i) == null) {
                // If the beacon has fewer identifiers than the region, but the region's
                // corresponding identifier is null, consider it a match
            }
            else {
                if (mIdentifiers.get(i) != null && !mIdentifiers.get(i).equals(beacon.mIdentifiers.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }
    @Override
    public int hashCode() {
        return this.mUniqueId.hashCode();
    }
    @Override
    public boolean equals(Object other) {
        if (other instanceof Region) {
            return ((Region)other).mUniqueId.equals(this.mUniqueId);
        }
        return false;
    }
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
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mUniqueId);
        out.writeInt(mIdentifiers.size());

        for (Identifier identifier: mIdentifiers) {
            if (identifier != null) {
                out.writeString(identifier.toString());
            }
            else {
                out.writeString(null);
            }
        }
    }

    protected Region(Parcel in) {
        Log.i(TAG, "MY : CON3 : Region is constructed!!");

        mUniqueId = in.readString();
        int size = in.readInt();
        mIdentifiers = new ArrayList<Identifier>(size);
        for (int i = 0; i < size; i++) {
            String identifierString = in.readString();
            if (identifierString == null) {
                mIdentifiers.add(null);
            } else {
                Identifier identifier = Identifier.parse(identifierString);
                mIdentifiers.add(identifier);
            }
        }
    }
    @Override
    @Deprecated
    public Region clone() {
        return new Region(mUniqueId, mIdentifiers);
    }
}

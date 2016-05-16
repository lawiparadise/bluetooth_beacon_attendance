package com.law.blueinnofora;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

/**
 * Created by gd2 on 2015-07-02.
 * 아이비콘만 파싱함
 */
public class IBeaconParser extends BeaconParser {
    public static final String TAG = "IBeaconParser";

    public IBeaconParser() {
        super();
        Log.i(TAG, "MY : CON : IBeaconParser is constructed!!");
        mHardwareAssistManufacturers = new int[]{0x0118}; // Radius networks
        //하드웨어 적용 제조사 수정해야할듯? 아이비콘으로
       this.setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");

        //알트비콘 이걸로 해도 아이비콘이 스캔이 된다.
        //this.setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
    }
    @TargetApi(5)
    @Override
    public Beacon fromScanData(byte[] scanData, int rssi, BluetoothDevice device) {
        return fromScanData(scanData, rssi, device, new IBeacon());
    }

}

package com.law.blueinnofora;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

/**
 * Created by gd2 on 2015-07-02.
 * ���̺��ܸ� �Ľ���
 */
public class IBeaconParser extends BeaconParser {
    public static final String TAG = "IBeaconParser";

    public IBeaconParser() {
        super();
        Log.i(TAG, "MY : CON : IBeaconParser is constructed!!");
        mHardwareAssistManufacturers = new int[]{0x0118}; // Radius networks
        //�ϵ���� ���� ������ �����ؾ��ҵ�? ���̺�������
       this.setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24");

        //��Ʈ���� �̰ɷ� �ص� ���̺����� ��ĵ�� �ȴ�.
        //this.setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
    }
    @TargetApi(5)
    @Override
    public Beacon fromScanData(byte[] scanData, int rssi, BluetoothDevice device) {
        return fromScanData(scanData, rssi, device, new IBeacon());
    }

}

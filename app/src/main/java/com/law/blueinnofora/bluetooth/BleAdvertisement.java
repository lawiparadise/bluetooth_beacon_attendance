package com.law.blueinnofora.bluetooth;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gd2 on 2015-07-02.
 * parse한다 byte array를. BLE광고를 PDU로 나타내면서.
 */
public class BleAdvertisement {
    private static final String TAG = "BleAdvertisement";
    private List<Pdu> mPdus;
    private byte[] mBytes;

    public BleAdvertisement(byte[] bytes) {
        Log.i(TAG,"MY : CON : BleAdvertisement constructed!!");
        mBytes = bytes;
        mPdus = parsePdus();
    }

    private List<Pdu> parsePdus() {
        ArrayList<Pdu> pdus = new ArrayList<Pdu>();
        Pdu pdu = null;
        int index = 0;
        do {
            pdu = Pdu.parse(mBytes, index);
            if (pdu != null) {
                index = index + pdu.getDeclaredLength()+1;
                pdus.add(pdu);
            }
        }
        while (pdu != null && index < mBytes.length);
        return pdus;
    }

    /**
     * The list of PDUs inside the advertisement
     * @return
     */
    public List<Pdu> getPdus() {
        return mPdus;
    }
}


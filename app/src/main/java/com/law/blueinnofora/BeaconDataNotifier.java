package com.law.blueinnofora;

import com.law.blueinnofora.client.DataProviderException;

/**
 * Created by gd2 on 2015-07-02.
 * 웹서버에서 사용가능한 비콘데이타가 있으면? 알린다.
 */
public interface BeaconDataNotifier {
    public void beaconDataUpdate(Beacon beacon, BeaconData data, DataProviderException exception);

}

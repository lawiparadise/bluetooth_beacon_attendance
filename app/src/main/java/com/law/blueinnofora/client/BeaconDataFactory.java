package com.law.blueinnofora.client;

import com.law.blueinnofora.Beacon;
import com.law.blueinnofora.BeaconDataNotifier;

/**
 * Created by gd2 on 2015-07-02.
 * 외부 비콘데이타에 접근?
 */
public interface BeaconDataFactory {
    public void requestBeaconData(Beacon beacon, BeaconDataNotifier notifier);
}

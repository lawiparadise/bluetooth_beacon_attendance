package com.law.blueinnofora.client;

import com.law.blueinnofora.Beacon;
import com.law.blueinnofora.BeaconDataNotifier;

/**
 * Created by gd2 on 2015-07-02.
 * �ܺ� ���ܵ���Ÿ�� ����?
 */
public interface BeaconDataFactory {
    public void requestBeaconData(Beacon beacon, BeaconDataNotifier notifier);
}

package com.law.blueinnofora;

import com.law.blueinnofora.client.DataProviderException;

/**
 * Created by gd2 on 2015-07-02.
 * ���������� ��밡���� ���ܵ���Ÿ�� ������? �˸���.
 */
public interface BeaconDataNotifier {
    public void beaconDataUpdate(Beacon beacon, BeaconData data, DataProviderException exception);

}

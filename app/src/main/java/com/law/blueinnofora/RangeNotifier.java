package com.law.blueinnofora;

import java.util.Collection;

/**
 * Created by gd2 on 2015-07-02.
 * �� �������̽��� ���ܰŸ��˸��� ���Ź޴� Ŭ������ ���� implement�ȴ�.
 */
public interface RangeNotifier {
    //1�ʿ� �ѹ��� �� �ȴ�.  ������ ���ܰ��� �Ÿ��� �������
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region);

}

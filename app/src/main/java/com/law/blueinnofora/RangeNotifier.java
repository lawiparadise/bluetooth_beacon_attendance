package com.law.blueinnofora;

import java.util.Collection;

/**
 * Created by gd2 on 2015-07-02.
 * 이 인터페이스는 비콘거리알림을 수신받는 클래스에 의해 implement된다.
 */
public interface RangeNotifier {
    //1초에 한번씩 콜 된다.  비저블 비콘과의 거리를 재기위해
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region);

}

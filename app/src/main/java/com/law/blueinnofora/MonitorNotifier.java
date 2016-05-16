package com.law.blueinnofora;

import com.law.blueinnofora.Region;

/**
 * Created by gd2 on 2015-07-02.
 * 이 인터페이스는 비콘 감시 알림을 받는 클래스에 의해 임플리먼트 된다.
 */
public interface MonitorNotifier {
    //비콘이 있는 영역 안에 안드로이드 디바이스가 있다는걸 나타낸다.
    public static final int INSIDE = 1;
    //비콘이 있는 영역 안에 안드로이드 디바스가 없다는걸 나타낸다.
    public static final int OUTSIDE = 0;
    //Region안에 한개의 비콘이라도 있다면 콜 된다.
    public void didEnterRegion(Region region);
    //Region안에 비콘이 없다면 콜 된다.
    public void didExitRegion(Region region);
    //안에있는지 밖에있는지 1또는 0으로 매개변수를 전달하면서 Region 안에 있거나 혹은 없거나 콜 된다.
    public void didDetermineStateForRegion(int state, Region region);
}

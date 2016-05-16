package com.law.blueinnofora;

import com.law.blueinnofora.Region;

/**
 * Created by gd2 on 2015-07-02.
 * �� �������̽��� ���� ���� �˸��� �޴� Ŭ������ ���� ���ø���Ʈ �ȴ�.
 */
public interface MonitorNotifier {
    //������ �ִ� ���� �ȿ� �ȵ���̵� ����̽��� �ִٴ°� ��Ÿ����.
    public static final int INSIDE = 1;
    //������ �ִ� ���� �ȿ� �ȵ���̵� ��ٽ��� ���ٴ°� ��Ÿ����.
    public static final int OUTSIDE = 0;
    //Region�ȿ� �Ѱ��� �����̶� �ִٸ� �� �ȴ�.
    public void didEnterRegion(Region region);
    //Region�ȿ� ������ ���ٸ� �� �ȴ�.
    public void didExitRegion(Region region);
    //�ȿ��ִ��� �ۿ��ִ��� 1�Ǵ� 0���� �Ű������� �����ϸ鼭 Region �ȿ� �ְų� Ȥ�� ���ų� �� �ȴ�.
    public void didDetermineStateForRegion(int state, Region region);
}

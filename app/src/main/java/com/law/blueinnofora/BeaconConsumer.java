package com.law.blueinnofora;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

/**
 * Created by gd2 on 2015-07-02.
 * ���ܰ� ��ȣ�ۿ��� ���ϴ� ��Ƽ��Ƽ�� ���񽺸� ���� �������̽���.
 * ���ܸų����� �Բ� ���ȴ�.
 * ���ܼ��񽺰� ��뷹�� �Ǿ��� �� �ݹ��� �����Ѵ�.
 */
public interface BeaconConsumer {
    //���ܼ��񽺰� ����ǰ� ���ܸŴ����� ���� ���� ����� accept �� �غ� �Ǿ��� �� �� �ȴ�.
    public void onBeaconServiceConnect();

    //���ܸŴ����� ���� ���񽺳� ��Ƽ��Ƽ�� ���ؽ�Ʈ�� ������ �� �� �ȴ�.
    public Context getApplicationContext();
    //���ܼ��񽺷κ��� ���������Ӱ� unbind �Ǿ��� �� ���ܸų����� ���� �� �ȴ�.
    public void unbindService(ServiceConnection connection);
    //���ܼ��񽺿� ���������Ӱ� bind �Ǿ��� �� �� �ȴ�.
    public boolean bindService(Intent intent, ServiceConnection connection, int mode);



}

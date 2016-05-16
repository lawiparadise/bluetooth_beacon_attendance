package com.law.blueinnofora;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

/**
 * Created by gd2 on 2015-07-02.
 * 비콘과 상호작용을 원하는 액티비티나 서비스를 위한 인터페이스다.
 * 비콘매너지와 함께 사용된다.
 * 비콘서비스가 사용레디가 되었을 때 콜백을 제공한다.
 */
public interface BeaconConsumer {
    //비콘서비스가 실행되고 비콘매니저를 통해 너의 명령이 accept 될 준비가 되었을 때 콜 된다.
    public void onBeaconServiceConnect();

    //비콘매니저가 너의 서비스나 액티비티의 컨텍스트를 겟했을 때 콜 된다.
    public Context getApplicationContext();
    //비콘서비스로부터 비콘컨슈머가 unbind 되었을 때 비콘매너지에 의해 콜 된다.
    public void unbindService(ServiceConnection connection);
    //비콘서비스와 비콘컨슈머가 bind 되었을 때 콜 된다.
    public boolean bindService(Intent intent, ServiceConnection connection, int mode);



}

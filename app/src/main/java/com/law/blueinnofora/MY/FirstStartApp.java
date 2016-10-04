package com.law.blueinnofora.MY;

import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.law.blueinnofora.BeaconManager;
import com.law.blueinnofora.CalendarMonthViewActivity;
import com.law.blueinnofora.Region;
import com.law.blueinnofora.ScheduleInputActivity;
import com.law.blueinnofora.startup.BootstrapNotifier;
import com.law.blueinnofora.startup.RegionBootstrap;

import static android.support.v4.app.ActivityCompat.startActivityForResult;


/**
 * Created by gd2 on 2015-07-02.
 * 이 어플리케이션은 앱을 실행시켰을 때 실행된다.
 * 가장 먼저 시작되는 메니페스트에 등록되어있는 어플리케이션임
 * BootstrapNotifier를 implements한다.
 * BootstrapNitifier는 MonitorNotifier를 상속한다.
 * MonitorNotifier는 Region클래스를 사용한다.
 * Region은 Identifier를 사용한다.
 * Region은 Beacon을 사용한다.
 */
public class FirstStartApp extends Application implements BootstrapNotifier {
    public CalendarMonthViewActivity calendarMonthViewActivity;
    public MainActivity mainActivity;

    private String STUDENTNAME="HYUN HONG"; //어플 꺼도 여기에 있는 내용은 저장 되는듯..??
    private int ID=0;
    private String MAJOR="";


    public static final int REQUEST_CODE_SCHEDULE_INPUT = 1001;

    private static final String TAG = "FirstStartApp";
    private RegionBootstrap regionBootstrap; //이거 하려면 비콘매너지 필요하고 비콘매니저를 실행하기위해서 복붙 엄청했다.
    // 그 과정에서 비콘매니저에 알트비콘 대신 아이비콘으로 세팅 해놨다.
    //하지만 getBeaconParsers.add로 추가해도 되는거긴하니까 굳이 필요는 없었을 듯도 하다.

    @Override
    public void onCreate(){
        super.onCreate();
        Log.i(TAG, "MY : ONCR : App start!!");

        BeaconManager beaconmanager = BeaconManager.getInstanceForApplication(this);
        Region region = new Region("com.example.myapp.boostrapRegion", null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);
        //레지온 부트스트랩까지 컨스트럭트 하고 그다음 메인엑티비티
        Log.e(TAG,"MY : Main START!!! ");
    }
    @Override
    public void didDetermineStateForRegion(int arg0, Region arg1) {
        // Don't care
    }

    @Override
    public void didEnterRegion(Region arg0){
        //비콘 신호받았을 때 켜지는게 문제가 아닌게, 비콘 신호받아서 켜진 CalendarMonthViewActivity 액티비티에서는 터치로하는게 저장이 됨.


        //  saveECTClass();
        //    showScheduleInput();
        CalendarMonthViewActivity.ONOFF=true;
        Intent intent = new Intent(this, CalendarMonthViewActivity.class);
        //    Intent intent = new Intent(this,calendarMonthViewActivity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP); //?
        //   intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(intent);

/*        Intent i=new Intent(getApplicationContext(),CalendarMonthViewActivity.class);
        startActivity(i);*/


        // This call to disable will make it so the activity below only gets launched the first time a beacon is seen (until the next time the app is launched)
        // if you want the Activity to launch every single time beacons come into view, remove this call.

/*     //   regionBootstrap.disable();
        Intent intent = new Intent(this, ECTClass.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //?
       // startActivityForResult(intent, 1);
        this.startActivity(intent);*/

    }
    @Override
    public void didExitRegion(Region arg0) {
        //       Toast.makeText(this,"EXIT",Toast.LENGTH_SHORT).show(); 이렇게 만들면 죽음
        // Don't care
    }

    private void showScheduleInput() {

        Intent intent = new Intent(this, ScheduleInputActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //?
        // startActivityForResult(intent, 1);
        this.startActivity(intent);

/*
        Intent intent = new Intent(this, ScheduleInputActivity.class);

      int todayPosition = monthViewAdapter.getTodayPosition();
        WeatherCurrentCondition weather = monthViewAdapter.getWeather(monthViewAdapter.todayYear, monthViewAdapter.todayMonth, todayPosition);
        if (weather != null) {
            String weatherIconUrl = weather.getIconURL();
            intent.putExtra("weatherIconUrl", weatherIconUrl);
        }

        startActivityForResult(intent, REQUEST_CODE_SCHEDULE_INPUT);*/

    }

    private void saveECTClass(){
        Log.i(TAG, "MY : MET : saveECTCLASS");
        Toast.makeText(this,"Attendace Checked!!",Toast.LENGTH_SHORT).show();

        Intent i = new Intent();


    }

    public void setSTUDENTNAME(String st){
        this.STUDENTNAME=st;
    }
    public String getSTUDENTNAME(){
        return STUDENTNAME;
    }
    public void setID(int id){
        this.ID=id;
    }
    public int getID(){
        return ID;
    }
    public void setMAJOR(String major){
        this.MAJOR=major;
    }
    public String getMAJOR(){
        return MAJOR;
    }

}

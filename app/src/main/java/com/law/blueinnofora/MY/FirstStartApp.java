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
 * АМ ѕоЗГё®ДЙАМјЗАє ѕЫА» ЅЗЗаЅГДЧА» ¶§ ЅЗЗаµИґЩ.
 * °ЎАе ёХАъ ЅГАЫµЗґВ ёЮґПЖдЅєЖ®їЎ µо·ПµЗѕоАЦґВ ѕоЗГё®ДЙАМјЗАУ
 * BootstrapNotifierё¦ implementsЗСґЩ.
 * BootstrapNitifierґВ MonitorNotifierё¦ »ујУЗСґЩ.
 * MonitorNotifierґВ RegionЕ¬·ЎЅєё¦ »зїлЗСґЩ.
 * RegionАє Identifierё¦ »зїлЗСґЩ.
 * RegionАє BeaconА» »зїлЗСґЩ.
 */
public class FirstStartApp extends Application implements BootstrapNotifier {
    public CalendarMonthViewActivity calendarMonthViewActivity;
    public MainActivity mainActivity;

    private String STUDENTNAME="HYUN HONG"; //ѕоЗГ ІЁµµ ї©±вїЎ АЦґВ і»їлАє АъАе µЗґВµн..??
    private String ID="0";
    private String MAJOR="";


    public static final int REQUEST_CODE_SCHEDULE_INPUT = 1001;

    private static final String TAG = "FirstStartApp";
    private RegionBootstrap regionBootstrap; //АМ°Е ЗП·Бёй єсДЬёЕіКБц ЗКїдЗП°н єсДЬёЕґПАъё¦ ЅЗЗаЗП±вА§ЗШј­ є№єЩ ѕцГ»ЗЯґЩ.
    // ±Ч °ъБ¤їЎј­ єсДЬёЕґПАъїЎ ѕЛЖ®єсДЬ ґлЅЕ ѕЖАМєсДЬАё·О јјЖГ ЗШіщґЩ.
    //ЗПБцёё getBeaconParsers.add·О ГЯ°ЎЗШµµ µЗґВ°Е±дЗПґП±о ±»АМ ЗКїдґВ ѕшѕъА» µнµµ ЗПґЩ.

    @Override
    public void onCreate(){
        super.onCreate();
        Log.i(TAG, "MY : ONCR : App start!!");

        BeaconManager beaconmanager = BeaconManager.getInstanceForApplication(this);
        Region region = new Region("com.example.myapp.boostrapRegion", null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);
        //·№БцїВ єОЖ®ЅєЖ®·¦±оБц ДБЅєЖ®·°Ж® ЗП°н ±ЧґЩАЅ ёЮАОїўЖјєсЖј
        Log.e(TAG,"MY : Main START!!! ");
    }
    @Override
    public void didDetermineStateForRegion(int arg0, Region arg1) {
        // Don't care
    }

    @Override
    public void didEnterRegion(Region arg0){
        //єсДЬ ЅЕИЈ№ЮѕТА» ¶§ ДСБцґВ°Ф №®Б¦°Ў ѕЖґС°Ф, єсДЬ ЅЕИЈ№ЮѕЖј­ ДСБш CalendarMonthViewActivity ѕЧЖјєсЖјїЎј­ґВ ЕНДЎ·ОЗПґВ°Ф АъАеАМ µК.


  //  saveECTClass();
   //    showScheduleInput();
        CalendarMonthViewActivity.ONOFF=true;
        Intent intent = new Intent(this, CalendarMonthViewActivity.class);
    //    Intent intent = new Intent(this,calendarMonthViewActivity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP); //АМ·ё°Ф ЗПёй µИґЩ.....or
      //  intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);   //АМ·ё°Ф add·Оµµ °ЎґЙЗПґЩ
     //   intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    //       intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
 //       Toast.makeText(this,"EXIT",Toast.LENGTH_SHORT).show(); АМ·ё°Ф ёёµйёй БЧАЅ
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
    public void setID(String id){
        this.ID=id;
    }
    public String getID(){
        return ID;
    }
    public void setMAJOR(String major){
        this.MAJOR=major;
    }
    public String getMAJOR(){
        return MAJOR;
    }

}

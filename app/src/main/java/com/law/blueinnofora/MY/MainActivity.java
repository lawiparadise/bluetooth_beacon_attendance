package com.law.blueinnofora.MY;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.law.blueinnofora.BeaconManager;
import com.law.blueinnofora.R;
import com.law.blueinnofora.service.BeaconService;


public class MainActivity extends ActionBarActivity {
    protected static final String TAG = "MainActivity";
    FirstStartApp app;

    BeaconService beaconservice = new BeaconService();
    private Button RESCANButton;
    private Button STOPBUTTON;
    public static Button STATEBUTTON;
    public static int STATE=0;
    public static final int ABSENCE=1;
    public static final int ATTENDANCE=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "MY : ONCR : MAINACTIVITY");
        app = (FirstStartApp)getApplicationContext();
        ((FirstStartApp) getApplicationContext()).mainActivity=this;

        BeaconManager beaconmanager = BeaconManager.getInstanceForApplication(this);
        
        RESCANButton = (Button) findViewById(R.id.RESCANBUTTON2);
        RESCANButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beaconservice.setRESCAN(true);
     /*           if(!beaconservice.getRESCAN()) {
                    beaconservice.setRESCAN(true);
                    RESCANButton.setText("scan..");
                }
                else{
                    RESCANButton.setText("Already scanning");
                }
                */

     /*           if(beaconservice.getRESCAN()) {
                    beaconservice.setRESCAN(false);
                    RESCANButton.setText("RESCAN");
                }
                else{
                    beaconservice.setRESCAN(true);
                    RESCANButton.setText("stop");

                }*/
            }
        });




        STOPBUTTON=(Button)findViewById(R.id.STOPBUTTON);
        STOPBUTTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beaconservice.setRESCAN(false);
            }
        });

        STATEBUTTON=(Button)findViewById(R.id.STATEBUTTON);
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "MY : ONRE : MAINACTIVITY");

    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "MY : ONPA : MAINACTIVITY");



    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "MY : ONDE : MAINACTIVITY");
    //    beaconManager.unbind(this);
     //   Toast.makeText(this,"hi",Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
    public static void updateUi(int state){
        if(state==2)
            STATEBUTTON.setText("ATTENDANCE");
        Log.e(TAG,"setText");

        //유아이 업데이트는 핸들러로 값을 받아서 업데이트 시켜준다.
        // 과목별 날짜별 출석,지각,결석을 확인할 수 있어야 한다.
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case ABSENCE:
                break;
            case ATTENDANCE:
               if(resultCode== Activity.RESULT_OK){
                    Log.e(TAG,"4 onACtivityResult");
                    updateUi(ATTENDANCE);
               }

                break;
            default:
                break;
        }

    }

/*
    //핸들러로 출첵이 완료되었을 때, disable함수로 비콘 사용을 끝낸다.
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg){
            switch(msg.what){
                case ATTENDANCE :
                    updateUi(ATTENDANCE);
                    break;
                default:
                    break;
            }
        }
    };
*/

    public void onECTButtonClicked(View v){
        Intent i = new Intent(this,ECTClass.class);
        startActivity(i);
        updateUi(STATE);




/*        Log.e(TAG,"1onECTBUttonClicked");
        Intent i = null;
        i = new Intent(this, ECTClass.class);
        startActivityForResult(i, ATTENDANCE);
        Log.e(TAG,"2startActivityForResult");*/
    }



}

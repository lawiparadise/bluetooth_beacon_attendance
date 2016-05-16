package com.law.blueinnofora.MY;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import com.law.blueinnofora.R;


/**
 * Created by gd2 on 2015-07-03.
 */
public class ECTClass extends ActionBarActivity {
    private  Activity ac;
 //   private final Handler mHandler=null;
//   private static ECTClass client = null;
    private Context mContext;
    public static String STATE="initial"; // 출결 상태 조정

    protected static final String TAG = "ECTClassActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ectclass);
        Log.i(TAG, "MY : ONCR : ECTClassActivity");
    }

    public void onABClicked(View v){
        Toast.makeText(getApplication(),"Attendance Checked!!",Toast.LENGTH_SHORT).show();
        MainActivity.updateUi(2);
        finish();


/*        //왜 인텐트로 하는지는 모르겠찌만 일단 시도
        Log.e(TAG, "3 setResult");
        Intent i = new Intent();
        i.putExtra(STATE, "2");
        setResult(Activity.RESULT_OK, i);
        */
       // mHandler = new Handler();
     //   Message msg = mHandler.obtainMessage(MainActivity.ABSENCE);
//        Message msg = mHandler.obtainMessage(MainActivity.ATTENDANCE);
//        mHandler.sendMessage(msg);


    }

/*    public static ECTClass getInstanceForApplication(){
        if(client==null){
            Log.i(TAG,"ECTCLass is constructedd!!!");
            client = new ECTClass();
        }
        return client;
    }


    protected ECTClass(){
    }

    public ECTClass(Activity ac, Handler han){

    }

    public ECTClass(Activity a, Handler han){
        ac=a;
        mHandler=han;
    }
*/








}

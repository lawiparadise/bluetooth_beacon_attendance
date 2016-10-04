package com.law.blueinnofora;


import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class
GCMIntentService extends IntentService {

    private static final String TAG = "GCMIntentService";

    /**
     * 생성자
     */
    public GCMIntentService() {
        super(TAG);

        Log.d(TAG, "GCMIntentService() called.");
    }

    /*
     * 전달받은 인텐트 처리
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();

        Log.d(TAG, "action : " + action);

    }

}
/**
 * Radius Networks, Inc.
 * http://www.radiusnetworks.com
 *
 * @author David G. Young
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.law.blueinnofora.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;


public class Callback {
    private static final String TAG = "Callback";
    private Intent intent;
    public Callback(String intentPackageName) {
        Log.i(TAG,"MY : CON : Callback is constructed!!!!!!!!!!");
        if (intentPackageName != null) {
            intent = new Intent();
            intent.setComponent(new ComponentName(intentPackageName, "com.law.blueinnofora.BeaconIntentProcessor"));
            //이 부분이 핵심이다. BeaconIntentProcessor를 인텐트에 넣고 그걸 밑에 call함수에서 실행시켜준다!! 여기빠지면 실행 안된다.

        }
    }
    public Intent getIntent() {
        return intent;
    }
    public void setIntent(Intent intent) {
        this.intent = intent;
    }
    /**
     * Tries making the callback, first via messenger, then via intent
     *
     * @param context
     * @param dataName
     * @param data
     * @return false if it callback cannot be made
     */
    public boolean call(Context context, String dataName, Parcelable data) {
        Log.e(TAG, "MY : callback call is start!!");
        if (intent != null) {
       //     LogManager.d(TAG, "attempting callback via intent: %s", intent.getComponent());
            intent.putExtra(dataName, data);
            context.startService(intent);
            return true;
        }
        return false;
    }
}

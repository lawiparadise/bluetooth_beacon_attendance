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
package com.law.blueinnofora;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.law.blueinnofora.service.MonitoringData;
import com.law.blueinnofora.service.RangingData;

/**
 * Converts internal intents to notifier callbacks
 */
@TargetApi(3)
public class BeaconIntentProcessor extends IntentService {
    private static final String TAG = "BeaconIntentProcessor";

    public BeaconIntentProcessor() {
        super("BeaconIntentProcessor"); //이걸로 인텐트의 인텐트페키지네임을 결정하는듯하다.
        Log.e(TAG,"MY : CON : beaconintentprocessor is constructed!!!");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //     LogManager.d(TAG, "got an intent to process");

        MonitoringData monitoringData = null;
        RangingData rangingData = null;

        if (intent != null && intent.getExtras() != null) {
            monitoringData = (MonitoringData) intent.getExtras().get("monitoringData"); //intent로 받은 모니터링 데이타를 저장한다.
            rangingData = (RangingData) intent.getExtras().get("rangingData");
        }

        if (rangingData != null) {
            //   LogManager.d(TAG, "got ranging data");
            if (rangingData.getBeacons() == null) {
                //      LogManager.w(TAG, "Ranging data has a null beacons collection");
            }
            RangeNotifier notifier = BeaconManager.getInstanceForApplication(this).getRangingNotifier();
            java.util.Collection<Beacon> beacons = rangingData.getBeacons();
            if (notifier != null) {
                notifier.didRangeBeaconsInRegion(beacons, rangingData.getRegion());
            }
            else {
                //          LogManager.d(TAG, "but ranging notifier is null, so we're dropping it.");
            }
            RangeNotifier dataNotifier = BeaconManager.getInstanceForApplication(this).getDataRequestNotifier();
            if (dataNotifier != null) {
                dataNotifier.didRangeBeaconsInRegion(beacons, rangingData.getRegion());
            }
        }

        if (monitoringData != null) {
            //         LogManager.d(TAG, "got monitoring data");
            MonitorNotifier notifier = BeaconManager.getInstanceForApplication(this).getMonitoringNotifier();
            if (notifier != null) {
                //        LogManager.d(TAG, "Calling monitoring notifier: %s", notifier);
                notifier.didDetermineStateForRegion(monitoringData.isInside() ? MonitorNotifier.INSIDE : MonitorNotifier.OUTSIDE, monitoringData.getRegion());
                if (monitoringData.isInside()) { //state를 가지고 모니터링데이타를 이용하여 지역 안에있는지 밖에 있는지 측정한다.
                    Log.e(TAG,"MY : didenterregion!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1111");
                    notifier.didEnterRegion(monitoringData.getRegion());
                    //     notifier.didEnterRegion(monitoringData.getRegion());
                    //이거다 찾았다...ㅠㅠ

                }
                else {
                    notifier.didExitRegion(monitoringData.getRegion());
                    Log.e(TAG, "MY : didExitRegion!!!!!");
                }
            }
        }
    }
}


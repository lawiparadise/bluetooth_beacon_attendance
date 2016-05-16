package com.law.blueinnofora.client;

import android.os.Handler;

import com.law.blueinnofora.Beacon;
import com.law.blueinnofora.BeaconDataNotifier;

/**
 * Created by gd2 on 2015-07-02.
 */
public class NullBeaconDataFactory implements BeaconDataFactory {
    @Override
    public void requestBeaconData(Beacon beacon, final BeaconDataNotifier notifier) {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                notifier.beaconDataUpdate(null, null, new DataProviderException("You need to configure a beacon data service to use this feature."));
            }
        });
    }
}

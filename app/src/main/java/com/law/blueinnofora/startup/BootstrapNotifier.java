package com.law.blueinnofora.startup;

import android.content.Context;

import com.law.blueinnofora.MonitorNotifier;

/**
 * Created by gd2 on 2015-07-02.
 */
public interface BootstrapNotifier extends MonitorNotifier {
    public Context getApplicationContext();
}

package com.law.blueinnofora.startup;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.util.Log;

import com.law.blueinnofora.BeaconConsumer;
import com.law.blueinnofora.BeaconManager;
import com.law.blueinnofora.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gd2 on 2015-07-02.
 * ������ ���� ��׶��忡�� �����°� �¾����ִ°� ������ش�.���ܷ����¿� ���� ���
 * �ν���Ʈ ����� ���۷����� Ȧ���ϴ°����� ���ܿ����� ��׶��� ��ĳ���� �߱�ȴ�.
 * ��Ī ������ �߰ߵǸ� ��Ʈ��Ʈ����Ƽ���̾��� didEnterRegion�� �� �ȴ�.
 */
public class RegionBootstrap {
    protected static final String TAG = "AppStarter";
    private BeaconManager beaconManager;
    private BootstrapNotifier application;
    private List<Region> regions;
    private boolean disabled = false;
    private BeaconConsumer beaconConsumer;

    public RegionBootstrap(BootstrapNotifier application, Region region) {
        Log.i(TAG,"MY : CON : RegionBootStrap is constructed!!, waiting for beaconservice connection");
        if (application.getApplicationContext() == null) {
            throw new NullPointerException("The BootstrapNotifier instance is returning null from its getApplicationContext() method.  Have you implemented this method?");
        }
        beaconManager = BeaconManager.getInstanceForApplication(application.getApplicationContext());

        this.application = application;
        regions = new ArrayList<Region>();
        regions.add(region);
        beaconConsumer = new InternalBeaconConsumer(); //�긦 ������ ����� ����
        beaconManager.bind(beaconConsumer); //���� �����ӷ� �Ŵ����� ���ε带 �� �Ŵ����� ���ε带 �ϴ°� �ȿ��� ���ܼ��񽺰� ��������µ�
  //      LogManager.d(TAG, "Waiting for BeaconService connection");
        Log.i(TAG,"MY : Con : Waiting for BeaconService connection");

    }

    public RegionBootstrap(BootstrapNotifier application, List<Region> regions) {
        if (application.getApplicationContext() == null) {
            throw new NullPointerException("The BootstrapNotifier instance is returning null from its getApplicationContext() method.  Have you implemented this method?");
        }
        beaconManager = BeaconManager.getInstanceForApplication(application.getApplicationContext());

        this.application = application;
        this.regions = regions;

        beaconConsumer = new InternalBeaconConsumer();
        beaconManager.bind(beaconConsumer);
    //    LogManager.d(TAG, "Waiting for BeaconService connection");
        Log.i(TAG,"MY : CON2 : RegionBootStrap is constructed!!, waiting for beaconservice connection");
    }

    //�߰����� ��Ʈ��Ʈ�� �ݹ��� ���´�. �ѹ� ���ú�� ���Ŀ�.
    public void disable() {
        Log.e(TAG,"MY : disable for once received");
        if (disabled) {
            return;
        }
        disabled = true;
        try {
            for (Region region : regions) {
                beaconManager.stopMonitoringBeaconsInRegion(region);
            }
        } catch (RemoteException e) {
       //     LogManager.e(e, TAG, "Can't stop bootstrap regions");
            Log.i(TAG,"MY : Can't stop bootstrap regions");
        }
        beaconManager.unbind(beaconConsumer);
    }

    private class InternalBeaconConsumer implements BeaconConsumer {

        /**
         * Method reserved for system use
         */
        @Override
        public void onBeaconServiceConnect() {
            Log.i(TAG,"MY : onBeaconServiceConnect");
        //    LogManager.d(TAG, "Activating background region monitoring");
            beaconManager.setMonitorNotifier(application);
            try {
                for (Region region : regions) {
                  //  LogManager.d(TAG, "Background region monitoring activated for region %s", region);
                    Log.i(TAG, "MY : Background region monitoring activated for region");

                    beaconManager.startMonitoringBeaconsInRegion(region);


                    if (beaconManager.isBackgroundModeUninitialized()) {
                        beaconManager.setBackgroundMode(true);

                    }
                }
            } catch (RemoteException e) {
              //  LogManager.e(e, TAG, "Can't set up bootstrap regions");
                Log.i(TAG,"MY : Can't set up bootstrap regions");

            }
        }

        /**
         * Method reserved for system use
         */
        @Override
        public boolean bindService(Intent intent, ServiceConnection conn, int arg2) {
            return application.getApplicationContext().bindService(intent, conn, arg2);
        }

        /**
         * Method reserved for system use
         */
        @Override
        public Context getApplicationContext() {
            return application.getApplicationContext();
        }

        /**
         * Method reserved for system use
         */
        @Override
        public void unbindService(ServiceConnection conn) {
            application.getApplicationContext().unbindService(conn);
        }
    }

}

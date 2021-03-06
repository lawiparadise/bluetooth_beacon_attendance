package com.law.blueinnofora.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.law.blueinnofora.Beacon;
import com.law.blueinnofora.BeaconManager;
import com.law.blueinnofora.BeaconParser;
import com.law.blueinnofora.Region;
import com.law.blueinnofora.bluetooth.BluetoothCrashResolver;
import com.law.blueinnofora.distance.DistanceCalculator;
import com.law.blueinnofora.distance.ModelSpecificDistanceCalculator;
import com.law.blueinnofora.service.scanner.CycledLeScanCallback;
import com.law.blueinnofora.service.scanner.CycledLeScanner;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

/**
 * Created by gd2 on 2015-07-02.
 * 비콘서비스 안에서 콜백의 콜함수를 실행시킨다. 콜 함수가 실행되면 비콘인텐트프로세서 서비스가 실행된다.
 */

@TargetApi(5)
public class BeaconService extends Service {

    public static final String TAG = "BeaconService";

    private Map<Region, RangeState> rangedRegionState = new HashMap<Region, RangeState>();
    private Map<Region, MonitorState> monitoredRegionState = new HashMap<Region, MonitorState>();
    int trackedBeaconsPacketCount;
    private Handler handler = new Handler();
    private int bindCount = 0;
    private BluetoothCrashResolver bluetoothCrashResolver;
    private DistanceCalculator defaultDistanceCalculator = null;
    private List<BeaconParser> beaconParsers;
    private CycledLeScanner mCycledScanner;
    private boolean mBackgroundFlag = false;
    private GattBeaconTracker mGattBeaconTracker = new GattBeaconTracker();

    public static boolean RESCAN = false;
        /*
     * The scan period is how long we wait between restarting the BLE advertisement scans
     * Each time we restart we only see the unique advertisements once (e.g. unique beacons)
     * So if we want updates, we have to restart.  For updates at 1Hz, ideally we
     * would restart scanning that often to get the same update rate.  The trouble is that when you
     * restart scanning, it is not instantaneous, and you lose any beacon packets that were in the
     * air during the restart.  So the more frequently you restart, the more packets you lose.  The
     * frequency is therefore a tradeoff.  Testing with 14 beacons, transmitting once per second,
     * here are the counts I got for various values of the SCAN_PERIOD:
     *
     * Scan period     Avg beacons      % missed
     *    1s               6                 57
     *    2s               10                29
     *    3s               12                14
     *    5s               14                0
     *
     * Also, because beacons transmit once per second, the scan period should not be an even multiple
     * of seconds, because then it may always miss a beacon that is synchronized with when it is stopping
     * scanning.
     *
     */

    private List<Beacon> simulatedScanData = null;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class BeaconBinder extends Binder {
        public BeaconService getService() {
            //     LogManager.i(TAG, "getService of BeaconBinder called");
            // Return this instance of LocalService so clients can call public methods
            return BeaconService.this;
        }
    }

    /**
     * Command to the service to display a message
     */
    public static final int MSG_START_RANGING = 2;
    public static final int MSG_STOP_RANGING = 3;
    public static final int MSG_START_MONITORING = 4;
    public static final int MSG_STOP_MONITORING = 5;
    public static final int MSG_SET_SCAN_PERIODS = 6;

    static class IncomingHandler extends Handler {
        private final WeakReference<BeaconService> mService;

        IncomingHandler(BeaconService service) {
            mService = new WeakReference<BeaconService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            BeaconService service = mService.get();
            StartRMData startRMData = (StartRMData) msg.obj;

            if (service != null) {
                switch (msg.what) {
                    case MSG_START_RANGING:
                        //    LogManager.i(TAG, "start ranging received");
                        service.startRangingBeaconsInRegion(startRMData.getRegionData(), new com.law.blueinnofora.service.Callback(startRMData.getCallbackPackageName()));
                        service.setScanPeriods(startRMData.getScanPeriod(), startRMData.getBetweenScanPeriod(), startRMData.getBackgroundFlag());
                        break;
                    case MSG_STOP_RANGING:
                        //        LogManager.i(TAG, "stop ranging received");
                        service.stopRangingBeaconsInRegion(startRMData.getRegionData());
                        service.setScanPeriods(startRMData.getScanPeriod(), startRMData.getBetweenScanPeriod(), startRMData.getBackgroundFlag());
                        break;
                    case MSG_START_MONITORING:
                        //    LogManager.i(TAG, "start monitoring received");
                        service.startMonitoringBeaconsInRegion(startRMData.getRegionData(), new com.law.blueinnofora.service.Callback(startRMData.getCallbackPackageName()));
                        service.setScanPeriods(startRMData.getScanPeriod(), startRMData.getBetweenScanPeriod(), startRMData.getBackgroundFlag());
                        break;
                    case MSG_STOP_MONITORING:
                        //       LogManager.i(TAG, "stop monitoring received");
                        service.stopMonitoringBeaconsInRegion(startRMData.getRegionData());
                        service.setScanPeriods(startRMData.getScanPeriod(), startRMData.getBetweenScanPeriod(), startRMData.getBackgroundFlag());
                        break;
                    case MSG_SET_SCAN_PERIODS:
                        //       LogManager.i(TAG, "set scan intervals received");
                        service.setScanPeriods(startRMData.getScanPeriod(), startRMData.getBetweenScanPeriod(), startRMData.getBackgroundFlag());
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler(this));

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        //    LogManager.i(TAG, "binding");
        bindCount++;
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //      LogManager.i(TAG, "unbinding");
        bindCount--;
        return false;
    }

    @Override
    public void onCreate() {
        //       LogManager.i(TAG, "beaconService version %s is starting up", BuildConfig.VERSION_NAME );
        Log.i(TAG,"MY : ONCR : BeaconService is constructed!!");
        bluetoothCrashResolver = new BluetoothCrashResolver(this);
        bluetoothCrashResolver.start();
        mCycledScanner = CycledLeScanner.createScanner(this, BeaconManager.DEFAULT_FOREGROUND_SCAN_PERIOD,
                BeaconManager.DEFAULT_FOREGROUND_BETWEEN_SCAN_PERIOD, mBackgroundFlag,  mCycledLeScanCallback,  bluetoothCrashResolver);

        beaconParsers = BeaconManager.getInstanceForApplication(getApplicationContext()).getBeaconParsers();
        defaultDistanceCalculator =  new ModelSpecificDistanceCalculator(this, BeaconManager.getDistanceModelUpdateUrl());
        Beacon.setDistanceCalculator(defaultDistanceCalculator);

        // Look for simulated scan data
        try {
            Class klass = Class.forName("com.law.blueinnofora.SimulatedScanData");
            java.lang.reflect.Field f = klass.getField("beacons");

            this.simulatedScanData = (List<Beacon>) f.get(null);

        } catch (ClassNotFoundException e) {
            Log.e(TAG,"MY : to make service, if SimulatedScanData no exist then, what?");
            //        LogManager.d(TAG, "No org.altbeacon.beacon.SimulatedScanData class exists.");
        } catch (Exception e) {
            Log.e(TAG,"MY : Cannot get simulated Scan data ");
            //         LogManager.e(e, TAG, "Cannot get simulated Scan data.  Make sure your org.altbeacon.beacon.SimulatedScanData class defines a field with the signature 'public static List<Beacon> beacons'");
        }
    }

    @Override
    @TargetApi(18)
    public void onDestroy() {
        if (android.os.Build.VERSION.SDK_INT < 18) {
            //        LogManager.w(TAG, "Not supported prior to API 18.");
            return;
        }
        bluetoothCrashResolver.stop();
        //    LogManager.i(TAG, "onDestroy called.  stopping scanning");
        handler.removeCallbacksAndMessages(null);
        mCycledScanner.stop();
    }
    /**
     * methods for clients
     */

    public void startRangingBeaconsInRegion(Region region, Callback callback) {
        Log.e(TAG, "MY : startRangingingBeaconInRegion");
        synchronized (rangedRegionState) {
            if (rangedRegionState.containsKey(region)) {
                //              LogManager.i(TAG, "Already ranging that region -- will replace existing region.");
                rangedRegionState.remove(region); // need to remove it, otherwise the old object will be retained because they are .equal
            }
            rangedRegionState.put(region, new RangeState(callback));
            //        LogManager.d(TAG, "Currently ranging %s regions.", rangedRegionState.size());
        }
        mCycledScanner.start();
    }

    public void stopRangingBeaconsInRegion(Region region) {
        int rangedRegionCount;
        synchronized (rangedRegionState) {
            rangedRegionState.remove(region);
            rangedRegionCount = rangedRegionState.size();
            //           LogManager.d(TAG, "Currently ranging %s regions.", rangedRegionState.size());
        }

        if (rangedRegionCount == 0 && monitoredRegionState.size() == 0) {
            mCycledScanner.stop();
        }
    }

    public void startMonitoringBeaconsInRegion(Region region, Callback callback) { //여기서 콜백 생성!!
        Log.i(TAG, "MY : startMonitoringBeaconInRegion");
        //     LogManager.d(TAG, "startMonitoring called");
        synchronized (monitoredRegionState) {
            if (monitoredRegionState.containsKey(region)) {
                //            LogManager.i(TAG, "Already monitoring that region -- will replace existing region monitor.");
                monitoredRegionState.remove(region); // need to remove it, otherwise the old object will be retained because they are .equal
            }
            monitoredRegionState.put(region, new MonitorState(callback));
        }
        //      LogManager.d(TAG, "Currently monitoring %s regions.", monitoredRegionState.size());

        //스캔 시작
        mCycledScanner.start();

    }

    public void stopMonitoringBeaconsInRegion(Region region) {
        int monitoredRegionCount;
        //    LogManager.d(TAG, "stopMonitoring called");
        synchronized (monitoredRegionState) {
            monitoredRegionState.remove(region);
            monitoredRegionCount = monitoredRegionState.size();
        }
        //     LogManager.d(TAG, "Currently monitoring %s regions.", monitoredRegionState.size());
        if (monitoredRegionCount == 0 && rangedRegionState.size() == 0) {
            mCycledScanner.stop();
        }
    }

    public void setScanPeriods(long scanPeriod, long betweenScanPeriod, boolean backgroundFlag) {
        mCycledScanner.setScanPeriods(scanPeriod, betweenScanPeriod, backgroundFlag);
    }

    private CycledLeScanCallback mCycledLeScanCallback = new CycledLeScanCallback() {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            try {
                new ScanProcessor().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                        new ScanData(device, rssi, scanRecord));
            }
            catch (RejectedExecutionException e) {
                //           LogManager.w(TAG, "Ignoring scan result because we cannot keep up.");
            }
        }

        @Override
        public void onCycleEnd() {
            processExpiredMonitors();
            processRangeData();
            // If we want to use simulated scanning data, do it here.  This is used for testing in an emulator
            if (simulatedScanData != null) {
                // if simulatedScanData is provided, it will be seen every scan cycle.  *in addition* to anything actually seen in the air
                // it will not be used if we are not in debug mode
                //    LogManager.w(TAG, "Simulated scan data is deprecated and will be removed in a future release. Please use the new BeaconSimulator interface instead.");

                if (0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
                    for (Beacon beacon : simulatedScanData) {
                        processBeaconFromScan(beacon);
                    }
                } else {
                    //                LogManager.w(TAG, "Simulated scan data provided, but ignored because we are not running in debug mode.  Please remove simulated scan data for production.");
                }
            }
            if (BeaconManager.getBeaconSimulator() != null) {
                // if simulatedScanData is provided, it will be seen every scan cycle.  *in addition* to anything actually seen in the air
                // it will not be used if we are not in debug mode
                if (BeaconManager.getBeaconSimulator().getBeacons() != null) {
                    if (0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
                        for (Beacon beacon : BeaconManager.getBeaconSimulator().getBeacons()) {
                            processBeaconFromScan(beacon);
                        }
                    } else {
                        //               LogManager.w(TAG, "Beacon simulations provided, but ignored because we are not running in debug mode.  Please remove beacon simulations for production.");
                    }
                } else {
                    //           LogManager.w(TAG, "getBeacons is returning null. No simulated beacons to report.");
                }
            }
        }
    };

    private void processRangeData() {
        synchronized(rangedRegionState) {
            Iterator<Region> regionIterator = rangedRegionState.keySet().iterator();
            while (regionIterator.hasNext()) {
                Region region = regionIterator.next();
                RangeState rangeState = rangedRegionState.get(region);
//                LogManager.d(TAG, "Calling ranging callback");
                rangeState.getCallback().call(BeaconService.this, "rangingData", new RangingData(rangeState.finalizeBeacons(), region));
            }
        }
    }

    private void processExpiredMonitors() {
        Log.e(TAG,"MY : processExpireMonitors!!!!!");
        synchronized (monitoredRegionState) {
            Iterator<Region> monitoredRegionIterator = monitoredRegionState.keySet().iterator();
            while (monitoredRegionIterator.hasNext()) {
                Region region = monitoredRegionIterator.next();
                MonitorState state = monitoredRegionState.get(region);
                if (state.isNewlyOutside() || RESCAN) { //새로운 스테이트면 콜 실행한다.
                    RESCAN=false;
                    //                  LogManager.d(TAG, "found a monitor that expired: %s", region);
                    state.getCallback().call(BeaconService.this, "monitoringData", new MonitoringData(state.isInside(), region));
                }
            }
        }
    }

    private void processBeaconFromScan(Beacon beacon) {
        if (Stats.getInstance().isEnabled()) {
            Stats.getInstance().log(beacon);
        }
        trackedBeaconsPacketCount++;
        //     if (LogManager.isVerboseLoggingEnabled()) {
        //        LogManager.d(TAG,
        //                 "beacon detected : %s", beacon.toString());
        //     }

        beacon = mGattBeaconTracker.track(beacon);
        // If this is a Gatt beacon that should be ignored, it will be set to null as a result of
        // the above
        if (beacon == null) {
            //          if (LogManager.isVerboseLoggingEnabled()) {
            //               LogManager.d(TAG,
            //                    "not processing detections for GATT extra data beacon");
            //         }
        }
        else {
            List<Region> matchedRegions = null;
            synchronized(monitoredRegionState) {
                matchedRegions = matchingRegions(beacon,
                        monitoredRegionState.keySet());
            }
            Iterator<Region> matchedRegionIterator = matchedRegions.iterator();
            while (matchedRegionIterator.hasNext()) {
                Region region = matchedRegionIterator.next();
                MonitorState state = monitoredRegionState.get(region);
                if (state != null && state.markInside()) {
                    state.getCallback().call(BeaconService.this, "monitoringData",
                            new MonitoringData(state.isInside(), region));
                }
            }

            //        LogManager.d(TAG, "looking for ranging region matches for this beacon");
            synchronized (rangedRegionState) {
                matchedRegions = matchingRegions(beacon, rangedRegionState.keySet());
                matchedRegionIterator = matchedRegions.iterator();
                while (matchedRegionIterator.hasNext()) {
                    Region region = matchedRegionIterator.next();
                    //         LogManager.d(TAG, "matches ranging region: %s", region);
                    RangeState rangeState = rangedRegionState.get(region);
                    if (rangeState != null) {
                        rangeState.addBeacon(beacon);
                    }
                }
            }
        }
    }

    private class ScanData {
        public ScanData(BluetoothDevice device, int rssi, byte[] scanRecord) {
            this.device = device;
            this.rssi = rssi;
            this.scanRecord = scanRecord;
        }
        int rssi;
        BluetoothDevice device;
        byte[] scanRecord;
    }

    private class ScanProcessor extends AsyncTask<ScanData, Void, Void> {
        DetectionTracker mDetectionTracker = DetectionTracker.getInstance();

        @Override
        protected Void doInBackground(ScanData... params) {
            ScanData scanData = params[0];
            Beacon beacon = null;

            for (BeaconParser parser : BeaconService.this.beaconParsers) {
                beacon = parser.fromScanData(scanData.scanRecord,
                        scanData.rssi, scanData.device);
                if (beacon != null) {
                    break;
                }
            }
            if (beacon != null) {
                mDetectionTracker.recordDetection();
                processBeaconFromScan(beacon);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    private List<Region> matchingRegions(Beacon beacon, Collection<Region> regions) {
        List<Region> matched = new ArrayList<Region>();
        Iterator<Region> regionIterator = regions.iterator();
        while (regionIterator.hasNext()) {
            Region region = regionIterator.next();
            if (region.matchesBeacon(beacon)) {
                matched.add(region);
            } else {
                //          LogManager.d(TAG, "This region (%s) does not match beacon: %s", region, beacon);
            }

        }

        return matched;
    }

    public void setRESCAN(boolean boo){
        RESCAN = boo;
    }
    public boolean getRESCAN(){
        return RESCAN;
    }


}

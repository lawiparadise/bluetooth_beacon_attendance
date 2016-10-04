package com.law.blueinnofora;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.law.blueinnofora.service.BeaconService;
import com.law.blueinnofora.service.RangeState;
import com.law.blueinnofora.service.RangedBeacon;
import com.law.blueinnofora.service.RunningAverageRssiFilter;
import com.law.blueinnofora.service.StartRMData;
import com.law.blueinnofora.simulator.BeaconSimulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by gd2 on 2015-07-02.
 * 비콘과 액티비티(or서비스)의 상호작용을 위한 셋업을 위해 이용된다.
 */
@TargetApi(4)
public class BeaconManager {
    private static final String TAG = "BeaconManager";
    private Context mContext;
    protected static BeaconManager client = null;
    private final ConcurrentMap<BeaconConsumer, ConsumerInfo> consumers = new ConcurrentHashMap<BeaconConsumer,ConsumerInfo>();
    private Messenger serviceMessenger = null;
    protected RangeNotifier rangeNotifier = null;
    protected RangeNotifier dataRequestNotifier = null;
    protected MonitorNotifier monitorNotifier = null;
    private final ArrayList<Region> monitoredRegions = new ArrayList<Region>();
    private final ArrayList<Region> rangedRegions = new ArrayList<Region>();
    private final ArrayList<BeaconParser> beaconParsers = new ArrayList<BeaconParser>();
    private boolean mBackgroundMode = false;
    private boolean mBackgroundModeUninitialized = true;

    private static boolean sAndroidLScanningDisabled = false;
    private static boolean sManifestCheckingDisabled = false;

    //라이브러리 디버깅을 보여주고 싶으면 트루하라
    @Deprecated
    public static void setDebug(boolean debug) {
        if (debug) {
            //       LogManager.setLogger(Loggers.verboseLogger());
            //       LogManager.setVerboseLoggingEnabled(true);
        } else {
            //       LogManager.setLogger(Loggers.empty());
            //      LogManager.setVerboseLoggingEnabled(false);
        }
    }

    /**
     * The default duration in milliseconds of the Bluetooth scan cycle
     */
    public static final long DEFAULT_FOREGROUND_SCAN_PERIOD = 1100;
    /**
     * The default duration in milliseconds spent not scanning between each Bluetooth scan cycle
     */
    public static final long DEFAULT_FOREGROUND_BETWEEN_SCAN_PERIOD = 0;
    /**
     * The default duration in milliseconds of the Bluetooth scan cycle when no ranging/monitoring clients are in the foreground
     */
    public static final long DEFAULT_BACKGROUND_SCAN_PERIOD = 1*1000;
    /**
     * The default duration in milliseconds spent not scanning between each Bluetooth scan cycle when no ranging/monitoring clients are in the foreground
     */
    public static final long DEFAULT_BACKGROUND_BETWEEN_SCAN_PERIOD = 5*1000;

    private long foregroundScanPeriod = DEFAULT_FOREGROUND_SCAN_PERIOD;
    private long foregroundBetweenScanPeriod = DEFAULT_FOREGROUND_BETWEEN_SCAN_PERIOD;
    private long backgroundScanPeriod = DEFAULT_BACKGROUND_SCAN_PERIOD;
    private long backgroundBetweenScanPeriod = DEFAULT_BACKGROUND_BETWEEN_SCAN_PERIOD;

    /**
     * Sets the duration in milliseconds of each Bluetooth LE scan cycle to look for beacons.
     * This function is used to setup the period before calling {@link #bind} or when switching
     * between background/foreground. To have it effect on an already running scan (when the next
     * cycle starts), call {@link #updateScanPeriods}
     *
     * @param p
     */
    public void setForegroundScanPeriod(long p) {
        foregroundScanPeriod = p;
    }

    /**
     * Sets the duration in milliseconds between each Bluetooth LE scan cycle to look for beacons.
     * This function is used to setup the period before calling {@link #bind} or when switching
     * between background/foreground. To have it effect on an already running scan (when the next
     * cycle starts), call {@link #updateScanPeriods}
     *
     * @param p
     */
    public void setForegroundBetweenScanPeriod(long p) {
        foregroundBetweenScanPeriod = p;
    }

    /**
     * Sets the duration in milliseconds of each Bluetooth LE scan cycle to look for beacons.
     * This function is used to setup the period before calling {@link #bind} or when switching
     * between background/foreground. To have it effect on an already running scan (when the next
     * cycle starts), call {@link #updateScanPeriods}
     *
     * @param p
     */
    public void setBackgroundScanPeriod(long p) {
        backgroundScanPeriod = p;
    }

    /**
     * Sets the duration in milliseconds spent not scanning between each Bluetooth LE scan cycle when no ranging/monitoring clients are in the foreground
     *
     * @param p
     */
    public void setBackgroundBetweenScanPeriod(long p) {
        backgroundBetweenScanPeriod = p;
    }

    /**
     * An accessor for the singleton instance of this class.  A context must be provided, but if you need to use it from a non-Activity
     * or non-Service class, you can attach it to another singleton or a subclass of the Android Application class.
     */
    public static BeaconManager getInstanceForApplication(Context context) {
        if (client == null) {
            //     LogManager.d(TAG, "BeaconManager instance creation");
            Log.i(TAG,"MY : CON : BeaconManager>>GetInstanceForApplication!!");
            client = new BeaconManager(context);
        }
        else
            Log.i(TAG,"MY : CON : BeaconManager already exist");
        return client;
    }

    protected BeaconManager(Context context) {
        Log.i(TAG,"MY : CON : BeaconManager instance creation!!");

        mContext = context;
        if (!sManifestCheckingDisabled) {
            verifyServiceDeclaration();
        }
        //this.beaconParsers.add(new AltBeaconParser());
        this.beaconParsers.add(new IBeaconParser());

    }

    //활성화된 비콘파서의 리스트를 얻는다.
    public List<BeaconParser> getBeaconParsers() {
        if (isAnyConsumerBound()) {
            return Collections.unmodifiableList(beaconParsers);
        }
        return beaconParsers;
    }

    //BLE가 안드로이드디바이스에서 제공되는지 체크한다. 그리고 사용가능하게 만든다.
    @TargetApi(18)
    public boolean checkAvailability() throws BleNotAvailableException {
        if (android.os.Build.VERSION.SDK_INT < 18) {
            throw new BleNotAvailableException("Bluetooth LE not supported by this device");
        }
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            throw new BleNotAvailableException("Bluetooth LE not supported by this device");
        } else {
            if (((BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().isEnabled()) {
                return true;
            }
        }
        return false;
    }

    //bind한다. 안드로이드 액티비티(or서비스)와 비콘서비스를. 액티비티(or서비스)는 반드시 beaconConsumer를 implement해야한다.콜백을 받기위해
    public void bind(BeaconConsumer consumer) {
        Log.i(TAG,"MY : bind started");
        if (android.os.Build.VERSION.SDK_INT < 18) {
            //    LogManager.w(TAG, "Not supported prior to API 18.  Method invocation will be ignored");
            return;
        }
        synchronized (consumers) {
            ConsumerInfo consumerInfo = consumers.putIfAbsent(consumer, new ConsumerInfo());
            if (consumerInfo != null) {
                //        LogManager.d(TAG, "This consumer is already bound");
                Log.i(TAG,"MY : synchronized >> this consumer is already bound");
            }
            else {
                //     LogManager.d(TAG, "This consumer is not bound.  binding: %s", consumer);
                Log.i(TAG,"MY : this consumer is not bound so will start bindservice");
                Intent intent = new Intent(consumer.getApplicationContext(), BeaconService.class);
                consumer.bindService(intent, beaconServiceConnection, Context.BIND_AUTO_CREATE);
                //아마 이 바인드 때문에 비콘서비스가 생성 될듯 하고,
                //    LogManager.d(TAG, "consumer count is now: %s", consumers.size());
            }
        }
    }

    //unbind. destroy때 싫행된다.
    public void unbind(BeaconConsumer consumer) {
        if (android.os.Build.VERSION.SDK_INT < 18) {
            //      LogManager.w(TAG, "Not supported prior to API 18.  Method invocation will be ignored");
            return;
        }
        synchronized (consumers) {
            if (consumers.containsKey(consumer)) {
                //       LogManager.d(TAG, "Unbinding");
                consumer.unbindService(beaconServiceConnection);
                consumers.remove(consumer);
                if (consumers.size() == 0) {
                    // If this is the last consumer to disconnect, the service will exit
                    // release the serviceMessenger.
                    serviceMessenger = null;
                }
            }
            else {
                //      LogManager.d(TAG, "This consumer is not bound to: %s", consumer);
                //      LogManager.d(TAG, "Bound consumers: ");
                Set<Map.Entry<BeaconConsumer, ConsumerInfo>> consumers = this.consumers.entrySet();
                for (Map.Entry<BeaconConsumer, ConsumerInfo> consumerEntry : consumers) {
                    //              LogManager.d(TAG, String.valueOf(consumerEntry.getValue()));
                }
            }
        }
    }

    //passed beacon consumer가 서비스에 바운드 되엇는지 묻는다.
    public boolean isBound(BeaconConsumer consumer) {
        synchronized(consumers) {
            return consumer != null && consumers.get(consumer) != null && (serviceMessenger != null);
        }
    }
    //any beacon consumer가 서비스에 바운드 되엇는지 묻는다.
    public boolean isAnyConsumerBound() {
        synchronized(consumers) {
            return consumers.size() > 0 && (serviceMessenger != null);
        }
    }

    //이 함수는 알린다. 비콘서비스를. 그 비콘서비스는 앱이 백그라운드에서 움직이는지 포그라운드에서 움직이는지.
    public void setBackgroundMode(boolean backgroundMode) {
        if (android.os.Build.VERSION.SDK_INT < 18) {
            //     LogManager.w(TAG, "Not supported prior to API 18.  Method invocation will be ignored");
        }
        mBackgroundModeUninitialized = false;
        if (backgroundMode != mBackgroundMode) {
            mBackgroundMode = backgroundMode;
            try {
                this.updateScanPeriods();
            } catch (RemoteException e) {
                //   LogManager.e(TAG, "Cannot contact service to set scan periods");
            }
        }
    }

    //어떤 콜이 백그라운드모드로 섹팅이 되었는지 아닌지 나타내준다.
    public boolean isBackgroundModeUninitialized() {
        return mBackgroundModeUninitialized;
    }

    //?
    public void setRangeNotifier(RangeNotifier notifier) {
        rangeNotifier = notifier;
    }

    //?
    public void setMonitorNotifier(MonitorNotifier notifier) {
        monitorNotifier = notifier;
    }

    //비콘을 레인징하는걸 스타트. 비콘서비스가 passed region object를 맷치하는 비콘을 찾는것을 시작하기위한 비콘 서비스를 말해준다.
    @TargetApi(18)
    public void startRangingBeaconsInRegion(Region region) throws RemoteException {
        if (android.os.Build.VERSION.SDK_INT < 18) {
            // LogManager.w(TAG, "Not supported prior to API 18.  Method invocation will be ignored");
            return;
        }
        if (serviceMessenger == null) {
            throw new RemoteException("The BeaconManager is not bound to the service.  Call beaconManager.bind(BeaconConsumer consumer) and wait for a callback to onBeaconServiceConnect()");
        }
        Message msg = Message.obtain(null, BeaconService.MSG_START_RANGING, 0, 0);
        StartRMData obj = new StartRMData(region, callbackPackageName(), this.getScanPeriod(), this.getBetweenScanPeriod(), this.mBackgroundMode);
        msg.obj = obj;
        serviceMessenger.send(msg);
        synchronized (rangedRegions) {
            rangedRegions.add(region);
        }
    }

    //비콘을 레인징 하는걸 스탑. 비콘 찾는걸 멈추라고 비콘서비스에게 말한다.
    @TargetApi(18)
    public void stopRangingBeaconsInRegion(Region region) throws RemoteException {
        if (android.os.Build.VERSION.SDK_INT < 18) {
            //   LogManager.w(TAG, "Not supported prior to API 18.  Method invocation will be ignored");
            return;
        }
        if (serviceMessenger == null) {
            throw new RemoteException("The BeaconManager is not bound to the service.  Call beaconManager.bind(BeaconConsumer consumer) and wait for a callback to onBeaconServiceConnect()");
        }
        Message msg = Message.obtain(null, BeaconService.MSG_STOP_RANGING, 0, 0);
        StartRMData obj = new StartRMData(region, callbackPackageName(), this.getScanPeriod(), this.getBetweenScanPeriod(), this.mBackgroundMode);
        msg.obj = obj;
        serviceMessenger.send(msg);
        synchronized (rangedRegions) {
            Region regionToRemove = null;
            for (Region rangedRegion : rangedRegions) {
                if (region.getUniqueId().equals(rangedRegion.getUniqueId())) {
                    regionToRemove = rangedRegion;
                }
            }
            rangedRegions.remove(regionToRemove);
        }
    }

    //비콘 모니터링 스타트
    @TargetApi(18)
    public void startMonitoringBeaconsInRegion(Region region) throws RemoteException {
        if (android.os.Build.VERSION.SDK_INT < 18) {
            //         LogManager.w(TAG, "Not supported prior to API 18.  Method invocation will be ignored");
            return;
        }
        if (serviceMessenger == null) {
            Log.e(TAG,"MY : 안된다");
            throw new RemoteException("The BeaconManager is not bound to the service.  Call beaconManager.bind(BeaconConsumer consumer) and wait for a callback to onBeaconServiceConnect()");
        }
        Message msg = Message.obtain(null, BeaconService.MSG_START_MONITORING, 0, 0);


        StartRMData obj = new StartRMData(region, callbackPackageName(), this.getScanPeriod(), this.getBetweenScanPeriod(), this.mBackgroundMode);

        msg.obj = obj;
        serviceMessenger.send(msg);
        synchronized (monitoredRegions) {
            monitoredRegions.add(region);

        }
    }

    //비콘 모니터링 스탑
    @TargetApi(18)
    public void stopMonitoringBeaconsInRegion(Region region) throws RemoteException {
        if (android.os.Build.VERSION.SDK_INT < 18) {
            //      LogManager.w(TAG, "Not supported prior to API 18.  Method invocation will be ignored");
            return;
        }
        if (serviceMessenger == null) {
            throw new RemoteException("The BeaconManager is not bound to the service.  Call beaconManager.bind(BeaconConsumer consumer) and wait for a callback to onBeaconServiceConnect()");
        }
        Message msg = Message.obtain(null, BeaconService.MSG_STOP_MONITORING, 0, 0);
        StartRMData obj = new StartRMData(region, callbackPackageName(), this.getScanPeriod(), this.getBetweenScanPeriod(), this.mBackgroundMode);
        msg.obj = obj;
        serviceMessenger.send(msg);
        synchronized (monitoredRegions) {
            Region regionToRemove = null;
            for (Region monitoredRegion : monitoredRegions) {
                if (region.getUniqueId().equals(monitoredRegion.getUniqueId())) {
                    regionToRemove = monitoredRegion;
                }
            }
            monitoredRegions.remove(regionToRemove);
        }
    }

    //스캔 사이클 바꾸기
    @TargetApi(18)
    public void updateScanPeriods() throws RemoteException {
        if (android.os.Build.VERSION.SDK_INT < 18) {
            //    LogManager.w(TAG, "Not supported prior to API 18.  Method invocation will be ignored");
            return;
        }
        if (serviceMessenger == null) {
            throw new RemoteException("The BeaconManager is not bound to the service.  Call beaconManager.bind(BeaconConsumer consumer) and wait for a callback to onBeaconServiceConnect()");
        }
        Message msg = Message.obtain(null, BeaconService.MSG_SET_SCAN_PERIODS, 0, 0);
        //     LogManager.d(TAG, "updating background flag to %s", mBackgroundMode);
        //   LogManager.d(TAG, "updating scan period to %s, %s", this.getScanPeriod(), this.getBetweenScanPeriod());
        StartRMData obj = new StartRMData(this.getScanPeriod(), this.getBetweenScanPeriod(), this.mBackgroundMode);
        msg.obj = obj;
        serviceMessenger.send(msg);
    }

    //
    private String callbackPackageName() {
        String packageName = mContext.getPackageName();
        //   LogManager.d(TAG, "callback packageName: %s", packageName);
        return packageName;
    }

    //비콘서비스가 만들어지고 실행 된다.
    private ServiceConnection beaconServiceConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(TAG,"MY : we have a connection to the service now");
            ///     LogManager.d(TAG, "we have a connection to the service now");
            serviceMessenger = new Messenger(service);
            synchronized(consumers) {
                Iterator<Map.Entry<BeaconConsumer, ConsumerInfo>> iter = consumers.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<BeaconConsumer, ConsumerInfo> entry = iter.next();

                    if (!entry.getValue().isConnected) {
                        entry.getKey().onBeaconServiceConnect();
                        entry.getValue().isConnected = true;
                    }
                }
            }
        }

        // Called when the connection with the service disconnects
        public void onServiceDisconnected(ComponentName className) {
            Log.i(TAG,"MY : onServiceDisconnected!!");
            //         LogManager.e(TAG, "onServiceDisconnected");
            serviceMessenger = null;
        }
    };

    public MonitorNotifier getMonitoringNotifier() {
        return this.monitorNotifier;
    }
    public RangeNotifier getRangingNotifier() {
        return this.rangeNotifier;
    }

    //모니터된 리지온 리스트
    public Collection<Region> getMonitoredRegions() {
        synchronized(this.monitoredRegions) {
            return new ArrayList<Region>(this.monitoredRegions);
        }
    }

    //레인지된 리지온 리스트
    public Collection<Region> getRangedRegions() {
        synchronized(this.rangedRegions) {
            return new ArrayList<Region>(this.rangedRegions);
        }
    }



    /**
     * Convenience method for logging debug by the library
     *
     * @param tag
     * @param message
     * @deprecated This will be removed in a later release. Use
     * {@link org.altbeacon.beacon.logging.LogManager#d(String, String, Object...)} instead.
     */
    //   @Deprecated
    //  public static void logDebug(String tag, String message) {
    //      LogManager.d(tag, message);
    //  }

    /**
     * Convenience method for logging debug by the library
     *
     * @param tag
     * @param message
     * @param t
     * @deprecated This will be removed in a later release. Use
     * {@link org.altbeacon.beacon.logging.LogManager#d(Throwable, String, String, Object...)}
     * instead.
     */
    //   @Deprecated
    //   public static void logDebug(String tag, String message, Throwable t) {
    //     LogManager.d(t, tag, message);
    //  }

    protected static BeaconSimulator beaconSimulator;

    protected static String distanceModelUpdateUrl = "http://data.altbeacon.org/android-distance.json";

    public static String getDistanceModelUpdateUrl() {
        return distanceModelUpdateUrl;
    }

    public static void setDistanceModelUpdateUrl(String url) {
        distanceModelUpdateUrl = url;
    }

    /**
     * Default class for rssi filter/calculation implementation
     */
    protected static Class rssiFilterImplClass = RunningAverageRssiFilter.class;

    public static void setRssiFilterImplClass(Class c) {
        rssiFilterImplClass = c;
    }

    public static Class getRssiFilterImplClass() {
        return rssiFilterImplClass;
    }

    /**
     * Allow the library to use a tracking cache
     * @param useTrackingCache
     */
    public static void setUseTrackingCache(boolean useTrackingCache) {
        RangeState.setUseTrackingCache(useTrackingCache);
    }

    /**
     * Set the period of time, in which a beacon did not receive new
     * measurements
     * @param maxTrackingAge in milliseconds
     */
    public void setMaxTrackingAge(int maxTrackingAge) {
        RangedBeacon.setMaxTrackinAge(maxTrackingAge);
    }

    public static void setBeaconSimulator(BeaconSimulator beaconSimulator) {
        BeaconManager.beaconSimulator = beaconSimulator;
    }

    public static BeaconSimulator getBeaconSimulator() {
        return BeaconManager.beaconSimulator;
    }


    protected void setDataRequestNotifier(RangeNotifier notifier) {
        this.dataRequestNotifier = notifier;
    }

    protected RangeNotifier getDataRequestNotifier() {
        return this.dataRequestNotifier;
    }

    private class ConsumerInfo {
        public boolean isConnected = false;
    }

    private long getScanPeriod() {
        if (mBackgroundMode) {
            return backgroundScanPeriod;
        } else {
            return foregroundScanPeriod;
        }
    }

    private long getBetweenScanPeriod() {
        if (mBackgroundMode) {
            return backgroundBetweenScanPeriod;
        } else {
            return foregroundBetweenScanPeriod;
        }
    }

    //비콘서비스를 생성하는게 아니라 단순히 패키지매니저가 0이 아닌지만 확인해준다.
    private void verifyServiceDeclaration() {
        Log.i(TAG,"MY : CON : do verifyServiceDeclaration");
        final PackageManager packageManager = mContext.getPackageManager();
        final Intent intent = new Intent(mContext, BeaconService.class);

        List resolveInfo = packageManager.queryIntentServices(intent,PackageManager.MATCH_DEFAULT_ONLY);

        if (resolveInfo.size() == 0) { //이 부분이 문제다. 이거 왜 안되지? 비콘서비스 안에 안넣어서 그런듯하다. 넣어보자
            //service에 내용 안채워넣은게 문제가 아니라 메니페스트에 등록을 안한게 문제였다.
            throw new ServiceNotDeclaredException();
        }


    }

    public class ServiceNotDeclaredException extends RuntimeException {
        public ServiceNotDeclaredException() {
            super("The BeaconService is not properly declared in AndroidManifest.xml.  If using Eclipse," +
                    " please verify that your project.properties has manifestmerger.enabled=true");
            //내용 확인 하였다. 매니페스트에 넣었더니 됨
        }
    }

    //?
    public static boolean isAndroidLScanningDisabled() {
        return sAndroidLScanningDisabled;
    }

    //?
    public static void setAndroidLScanningDisabled(boolean disabled) {
        sAndroidLScanningDisabled = disabled;
    }

    //?
    public static void setsManifestCheckingDisabled(boolean disabled) {
        sManifestCheckingDisabled = disabled;
    }

}

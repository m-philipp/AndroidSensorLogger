package ess.imu_logger.app.bluetoothLogger;

/**
 * Created by martin on 21.11.2014.
 */

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Date;
import java.util.UUID;

import ess.imu_logger.libs.data_save.SensorDataSavingService;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class LighterBluetoothService extends Service {
    private final static String TAG = LighterBluetoothService.class.getName();

    public static final String KEY_DEVICEADDR = "device_addr";

    public static final String ACTION_NEW_LIGHTER_ANNOTATION = "ess.imu_logger.app.bluetoothLogger.LIGHTER";

    public String KEY_SCANSTARTDELAY = "scan_timeout";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private Handler mHandler;
    private long mScanStartDelay = 1L;

    private Boolean connected = false;

    public final static UUID UUID_SERVICE =
            UUID.fromString("595403fb-f50e-4902-a99d-b39ffa4bb134");
    public final static UUID UUID_TIME_MEASUREMENT =
            UUID.fromString("595403fc-f50e-4902-a99d-b39ffa4bb134");
    public final static UUID UUID_CCC =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (mBluetoothGatt==null) {
                Log.e(TAG, "onConnectionStateChange: problem problem");
                mBluetoothGatt = gatt;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server. " + status);
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
                close();
                mHandler.postDelayed(mStartLEScan, mScanStartDelay);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "onServicesDiscovered received: " + status);
                return;
            }

            BluetoothGattCharacteristic c =
                    gatt.getService(UUID_SERVICE).getCharacteristic(UUID_TIME_MEASUREMENT);

            if (c == null) {
                Log.w(TAG, "onServiceDiscovered TIME characteristics UUID not found!");
                return;
            }

            // subscribing
            gatt.setCharacteristicNotification(c, true);
            BluetoothGattDescriptor config = c.getDescriptor(UUID_CCC);
            config.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            boolean rw = gatt.writeDescriptor(config);
            Log.d(TAG, "attempting to subscribe characteristic " + rw);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic c) {
            Log.w(TAG, "characteristics changed ");

            long send_time = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0),
                    evnt_time = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 4),
                    diff = send_time - evnt_time;

            Date date = new Date(System.currentTimeMillis() - diff);
            Log.w(TAG, "got event at " + date);

            // TODO do something with that Ligher Event
        }
    };

    private static boolean serviceIsInitialized = false;

    private BroadcastReceiver mBluetoothChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Bluetooth Enable / Disable
            close();
            mHandler.postDelayed(mStartLEScan, 10);
        }
    };


    public void clear_mac_addr() {
        disconnect();
        mBluetoothDeviceAddress = null;
        PreferenceManager.getDefaultSharedPreferences(LighterBluetoothService.this).edit().
                putString(KEY_DEVICEADDR, mBluetoothDeviceAddress).apply();
    }

    public String get_mac_addr() {
        if (mBluetoothDeviceAddress==null)
            return "none";

        return mBluetoothDeviceAddress;
    }

    public void onCreate(){
        super.onCreate();

        IntentFilter mif = new IntentFilter();
        mif.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothChangeReceiver, mif);

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        mBluetoothDeviceAddress = PreferenceManager.getDefaultSharedPreferences(this).
                getString(KEY_DEVICEADDR, null);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (mBluetoothManager == null || mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a mBluetoothManager / BluetoothAdapter.");
            return START_NOT_STICKY;
        }

        /** create a handler on the UI thread */
        mHandler = new Handler(Looper.getMainLooper());
        // mScanStartDelay = PreferenceManager.getDefaultSharedPreferences(this).getLong(KEY_SCANSTARTDELAY, 20 * 1000);
        mHandler.postDelayed(mStartLEScan, mScanStartDelay); // 10);

        return START_STICKY;
    }


    public class LocalBinder extends Binder {
        LighterBluetoothService getService() {
            return LighterBluetoothService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onDestroy() {
        Log.e(TAG, "service destroyed");
        super.onDestroy();
    }

    private boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null &&
                address.equals(mBluetoothDeviceAddress) &&
                mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            return true;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // use auto-connect, whenever possible
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);

        Log.d(TAG, "Trying to create a new connection. ");
        mBluetoothDeviceAddress = address;
        return true;
    }

    private void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    private void close() {
        Log.e(TAG, "connection closed.");

        if (mBluetoothGatt == null) {
            return;
        }

        mBluetoothGatt.close();
        mBluetoothGatt = null;

        connected = false;

        mHandler.postDelayed(mStartLEScan, mScanStartDelay);
    }

    private Runnable mStartLEScan = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "starting LE/Lighter Scan.");
            mBluetoothAdapter.startLeScan(bleScanCallback);
        }
    };

    private final BluetoothAdapter.LeScanCallback bleScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    Log.i(TAG, "found device " + device.getName() + " with rssi" + rssi);


                    // store BLE Scan results
                    Intent intent = new Intent(SensorDataSavingService.BROADCAST_BLE_RSSI);
                    sendBroadcast(intent);


                    if (connected || device == null || device.getName() == null || !device.getName().contains("iLitIt"))
                        return; // must be something else

                    if (mBluetoothDeviceAddress != null &&
                            !mBluetoothDeviceAddress.equals(device.getAddress()))
                        return;

                    if (mBluetoothDeviceAddress == null) {
                        mBluetoothDeviceAddress = device.getAddress();
                        PreferenceManager.getDefaultSharedPreferences(LighterBluetoothService.this).edit().
                                putString(KEY_DEVICEADDR, mBluetoothDeviceAddress).apply();
                    }

                    Log.w(TAG, "stopping the scan, found a device " + device.getAddress() );
                    //mBluetoothAdapter.stopLeScan(this);

                    connected = connect(mBluetoothDeviceAddress);


                    //mHandler.postDelayed(mStartLEScan, mScanStartDelay);
                    //mHandler.postDelayed(stopLEScan, timeout_ms)
                }
            };
}
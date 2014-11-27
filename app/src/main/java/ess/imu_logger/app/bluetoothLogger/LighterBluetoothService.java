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

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private long mScanStartDelay = 1L;


    private BroadcastReceiver mBluetoothChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Bluetooth Enable / Disable
            mHandler.postDelayed(mStartLEScan, 10);
        }
    };



    public void onCreate(){
        super.onCreate();

        IntentFilter mif = new IntentFilter();
        mif.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothChangeReceiver, mif);

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (mBluetoothManager == null || mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a mBluetoothManager / BluetoothAdapter.");
            return START_NOT_STICKY;
        }

        /** create a handler on the UI thread */
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(mStartLEScan, mScanStartDelay);

        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "service destroyed");
        super.onDestroy();
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
                    Log.i(TAG, "found device " + device.getName() + " with rssi" + rssi + " Address: " + device.getAddress());

                    // store BLE Scan results
                    Intent intent = new Intent(SensorDataSavingService.BROADCAST_BLE_RSSI);
                    intent.putExtra(SensorDataSavingService.EXTRA_BLE_DEVICE_NAME, device.getName());
                    intent.putExtra(SensorDataSavingService.EXTRA_BLE_DEVICE_ADDRESS, device.getAddress());
                    intent.putExtra(SensorDataSavingService.EXTRA_BLE_RSSI, rssi);
                    sendBroadcast(intent);


                }
            };
}
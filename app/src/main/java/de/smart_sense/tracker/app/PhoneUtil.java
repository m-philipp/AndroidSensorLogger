package de.smart_sense.tracker.app;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import de.smart_sense.tracker.app.bluetoothLogger.BluetoothScannerService;
import de.smart_sense.tracker.app.logging.AppLoggingService;
import de.smart_sense.tracker.libs.Util;
import de.smart_sense.tracker.libs.data_save.SensorDataSavingService;
import de.smart_sense.tracker.libs.data_zip_upload.ZipUploadService;

import static de.smart_sense.tracker.libs.Util.isServiceRunning;

/**
 * Created by martin on 09.01.2015.
 */
public class PhoneUtil {

    private static final String TAG = "de.smart_sense.tracker.app.PhoneUtil";


    public static void updateLoggingState(Context context, SharedPreferences sharedPrefs) {

        Log.d(TAG, "update Logging State");

        if (sharedPrefs.getBoolean(Util.PREFERENCES_SENSOR_ACTIVATE, false)) {

            startBackgroundLogging(context, sharedPrefs);
            return;

        }

        stopBackgroundLogging(context);

    }

    public static void startBackgroundLogging(Context context, SharedPreferences sharedPrefs) {

        Log.d(TAG, "starting Background Logging ...");

        Intent loggingServiceIntent = new Intent(context, AppLoggingService.class);
        loggingServiceIntent.setAction(AppLoggingService.ACTION_START_LOGGING);
        context.startService(loggingServiceIntent);

        Intent sensorDataSavingServiceIntent = new Intent(context, SensorDataSavingService.class);
        sensorDataSavingServiceIntent.setAction(SensorDataSavingService.ACTION_START_SERVICE);
        context.startService(sensorDataSavingServiceIntent);

        if (sharedPrefs.getBoolean(Util.PREFERENCES_BLUETOOTH_RSSI, false)) {
            Intent bluetoothServiceIntent = new Intent(context, BluetoothScannerService.class);
            context.startService(bluetoothServiceIntent);
        }


    }


    public static void stopBackgroundLogging(Context context) {

        Log.d(TAG, "stopping Background Logging ...");

        Intent loggingServiceIntent = new Intent(context, AppLoggingService.class);
        loggingServiceIntent.setAction(AppLoggingService.ACTION_STOP_LOGGING);
        context.startService(loggingServiceIntent);

        Intent sensorDataSavingServiceIntent = new Intent(context, SensorDataSavingService.class);
        sensorDataSavingServiceIntent.setAction(SensorDataSavingService.ACTION_STOP_SERVICE);
        context.startService(sensorDataSavingServiceIntent);

        if (isBluetoothServiceRunning(context)) {
            Intent bluetoothServiceIntent = new Intent(context, BluetoothScannerService.class);
            bluetoothServiceIntent.setAction(BluetoothScannerService.ACTION_STOP_SERVICE);
            context.startService(bluetoothServiceIntent);
        }
    }


    public static boolean isLoggingServiceRunning(Context context) {
        return isServiceRunning(context, AppLoggingService.class.getName());
    }

    public static boolean isBluetoothServiceRunning(Context context) {
        return isServiceRunning(context, BluetoothScannerService.class.getName());
    }


}

package ess.imu_logger.wear;

/**
 * Created by martin on 09.09.2014.
 */

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.File;

import ess.imu_logger.libs.TransferDataAsAssets;
import ess.imu_logger.libs.Util;
import ess.imu_logger.libs.data_save.SensorDataSavingService;
import ess.imu_logger.libs.logging.LoggingService;
import ess.imu_logger.wear.logging.WearLoggingService;


/**
 * Listens for a message telling it to start the Wearable MainActivity.
 */
public class WearableMessageListenerService extends WearableListenerService implements
        GoogleApiClient.OnConnectionFailedListener {


    SharedPreferences sharedPrefs;


    private static final String TAG = "ess.imu_logger.wear.WearableMessageListenerService";

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);


    }


    @Override
    public void onMessageReceived(MessageEvent event) {

        Log.d(TAG, "onMessageReceived");

        if (event.getPath().equals(Util.GAC_PATH_TEST_ACTIVITY)) {

            Log.d(TAG, "Message: " + Util.GAC_PATH_TEST_ACTIVITY);
            Toast.makeText(this, "Hello from Phone!", Toast.LENGTH_SHORT).show();

            //note();

        } else if (event.getPath().equals(Util.GAC_PATH_START_LOGGING)) {

            Log.d(TAG, "GAC Start Logging");
            // TODO start Main Activity
            // if (!isLoggingServiceRunning() || !isSensorDataSavingServiceRunning())
            startBackgroundLogging();


        } else if (event.getPath().equals(Util.GAC_PATH_STOP_LOGGING)) {

            Log.d(TAG, "GAC Stop Logging");
            stopBackgroundLogging();

        } else if (event.getPath().equals(Util.GAC_PATH_CONFIRM_FILE_RECEIVED)) {

            Log.d(TAG, "Received GAC_PATH: " + Util.GAC_PATH_CONFIRM_FILE_RECEIVED + " removing File: " + new String(event.getData()));

            // delete file
            deleteLogFile(new String(event.getData()));

            // transfer next File
            Intent mServiceIntent = new Intent(this, TransferDataAsAssets.class);
            mServiceIntent.setAction(TransferDataAsAssets.ACTION_TRANSFER);
            this.startService(mServiceIntent);

        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        Log.d(TAG, "onDataChanged");

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d(TAG, "DataItem deleted: " + event.getDataItem().getUri());
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                Log.d(TAG, "DataItem changed: " + event.getDataItem().getUri());

                String eventUri = event.getDataItem().getUri().toString();
                if (eventUri.contains(Util.GAC_PATH_PREFERENCES)) {

                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    DataMapItem dataItem = DataMapItem.fromDataItem(event.getDataItem());


                    Boolean data = dataItem.getDataMap().getBoolean(Util.PREFERENCES_ACCELEROMETER);
                    Log.d(TAG, "Acc = " + data);
                    editor.putBoolean(Util.PREFERENCES_ACCELEROMETER, data);

                    data = dataItem.getDataMap().getBoolean(Util.PREFERENCES_GYROSCOPE);
                    Log.d(TAG, "gyro = " + data);
                    editor.putBoolean(Util.PREFERENCES_GYROSCOPE, data);

                    data = dataItem.getDataMap().getBoolean(Util.PREFERENCES_MAGNETIC_FIELD);
                    Log.d(TAG, "mag = " + data);
                    editor.putBoolean(Util.PREFERENCES_MAGNETIC_FIELD, data);

                    data = dataItem.getDataMap().getBoolean(Util.PREFERENCES_AMBIENT_LIGHT);
                    Log.d(TAG, "ambient light = " + data);
                    editor.putBoolean(Util.PREFERENCES_AMBIENT_LIGHT, data);

                    data = dataItem.getDataMap().getBoolean(Util.PREFERENCES_PROXIMITY);
                    Log.d(TAG, "proximity = " + data);
                    editor.putBoolean(Util.PREFERENCES_PROXIMITY, data);

                    data = dataItem.getDataMap().getBoolean(Util.PREFERENCES_TEMPERATURE);
                    Log.d(TAG, "temperature = " + data);
                    editor.putBoolean(Util.PREFERENCES_TEMPERATURE, data);

                    data = dataItem.getDataMap().getBoolean(Util.PREFERENCES_HUMIDITY);
                    Log.d(TAG, "humidity = " + data);
                    editor.putBoolean(Util.PREFERENCES_HUMIDITY, data);

                    data = dataItem.getDataMap().getBoolean(Util.PREFERENCES_PRESSURE);
                    Log.d(TAG, "pressure = " + data);
                    editor.putBoolean(Util.PREFERENCES_PRESSURE, data);

                    data = dataItem.getDataMap().getBoolean(Util.PREFERENCES_ROTATION);
                    Log.d(TAG, "rotation = " + data);
                    editor.putBoolean(Util.PREFERENCES_ROTATION, data);

                    data = dataItem.getDataMap().getBoolean(Util.PREFERENCES_GRAVITY);
                    Log.d(TAG, "gravity = " + data);
                    editor.putBoolean(Util.PREFERENCES_GRAVITY, data);

                    data = dataItem.getDataMap().getBoolean(Util.PREFERENCES_LINEAR_ACCELEROMETER);
                    Log.d(TAG, "lin acc = " + data);
                    editor.putBoolean(Util.PREFERENCES_LINEAR_ACCELEROMETER, data);

                    data = dataItem.getDataMap().getBoolean(Util.PREFERENCES_STEPS);
                    Log.d(TAG, "steps = " + data);
                    editor.putBoolean(Util.PREFERENCES_STEPS, data);


                    String dataString = dataItem.getDataMap().getString(Util.PREFERENCES_NAME);
                    Log.d(TAG, "name = " + dataString);
                    editor.putString(Util.PREFERENCES_NAME, dataString);

                    dataString = dataItem.getDataMap().getString(Util.PREFERENCES_ANNOTATION_NAME);
                    Log.d(TAG, "name = " + dataString);
                    editor.putString(Util.PREFERENCES_ANNOTATION_NAME, dataString);

                    data = dataItem.getDataMap().getBoolean(Util.PREFERENCES_ANONYMIZE);
                    Log.d(TAG, "anonymize = " + dataString);
                    editor.putBoolean(Util.PREFERENCES_ANONYMIZE, data);

                    dataString = dataItem.getDataMap().getString(Util.PREFERENCES_SAMPLING_RATE);
                    Log.d(TAG, "sampling rate = " + dataString);
                    editor.putString(Util.PREFERENCES_SAMPLING_RATE, dataString);


                    editor.commit();


                    // start home screen
                    // TODO remove this
                    // startStartActivity();
                }

            }

        }
    }

    private void startStartActivity() {

        Log.d(TAG, "starting Start Activity ...");

        Intent startActivityIntent = new Intent(this, WearStartActivity.class);
        startActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(startActivityIntent);

    }


    private void startBackgroundLogging() {

        Log.d(TAG, "starting Background Logging ...");


        Intent loggingServiceIntent = new Intent(this, WearLoggingService.class);
        loggingServiceIntent.setAction(LoggingService.ACTION_START_LOGGING);
        this.startService(loggingServiceIntent);


        Intent sensorDataSavingServiceIntent = new Intent(this, SensorDataSavingService.class);
        sensorDataSavingServiceIntent.setAction(SensorDataSavingService.ACTION_START_SERVICE);
        this.startService(sensorDataSavingServiceIntent);


    }

    public void stopBackgroundLogging() {

        Log.d(TAG, "stopping Background Logging ...");

        Intent loggingServiceIntent = new Intent(this, WearLoggingService.class);
        loggingServiceIntent.setAction(LoggingService.ACTION_STOP_LOGGING);
        this.startService(loggingServiceIntent);

        Intent sensorDataSavingServiceIntent = new Intent(this, SensorDataSavingService.class);
        sensorDataSavingServiceIntent.setAction(SensorDataSavingService.ACTION_STOP_SERVICE);
        this.startService(sensorDataSavingServiceIntent);

    }

    private void deleteLogFile(String filename) {

        // TODO do this in another thread.

        Util.checkDirs();
        Log.d(TAG, "original filename: " + filename);
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        String absFileName = dir.getAbsolutePath() + File.separator + Util.fileDir + File.separator + filename;

        Log.d(TAG, "deleting: " + absFileName);

        File f = new File(absFileName);
        f.setWritable(true);
        if (f.delete()) {
            Log.d(TAG, f.getName() + " is deleted!");
        } else {
            Log.d(TAG, f.getName() + " Delete operation is failed.");
        }

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Failed to connect to Google Api Client");
    }

    protected boolean isLoggingServiceRunning() {
        return isServiceRunning(LoggingService.class.getName());
    }

    protected boolean isSensorDataSavingServiceRunning() {
        return isServiceRunning(SensorDataSavingService.class.getName());
    }

    protected boolean isServiceRunning(String classname) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (classname.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    /*
    PendingResult<DataItemBuffer> results = Wearable.DataApi.getDataItems(mGoogleApiClient);
    results.setResultCallback(new ResultCallback<DataItemBuffer>() {
        @Override
        public void onResult(DataItemBuffer dataItems) {
            if (dataItems.getCount() != 0) {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItems.get(0));

                // This should read the correct value.
                Boolean value = dataMapItem.getDataMap().getBoolean(Util.PREFERENCES_ACCELEROMETER);
                Log.d(TAG, "Acc = " + value);
            }

            dataItems.release();
        }
    });
    */


}

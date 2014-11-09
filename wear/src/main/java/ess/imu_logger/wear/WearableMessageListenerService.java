package ess.imu_logger.wear;

/**
 * Created by martin on 09.09.2014.
 */

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import ess.imu_logger.libs.Util;
import ess.imu_logger.libs.data_save.SensorDataSavingService;
import ess.imu_logger.libs.data_zip_upload.ZipUploadService;
import ess.imu_logger.libs.logging.LoggingService;


/**
 * Listens for a message telling it to start the Wearable MainActivity.
 */
public class WearableMessageListenerService extends WearableListenerService implements
        GoogleApiClient.OnConnectionFailedListener{



    // private AlarmManager alarmMgr;
    // private PendingIntent alarmIntent;
    SharedPreferences sharedPrefs;


    private static final String TAG = "ess.imu_logger.wear.WearableMessageListenerService";

    @Override
    public void onCreate(){
        super.onCreate();

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        /*

        // TODO reimplement


        Intent intent = new Intent(this, myReceiver.class);
        intent.setAction(ZipUploadService.ACTION_START_SERVICE);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        if(alarmMgr == null){
            Log.d(TAG, "AlarmManager was null");
            alarmMgr = (AlarmManager) this.getSystemService(this.ALARM_SERVICE);
        }
        else {
            Log.d(TAG, "AlarmManager was not null. Canceling alarmIntent");

            alarmMgr.cancel(alarmIntent);
        }


        alarmMgr.cancel(alarmIntent);

        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                1000,
                Util.ZIP_UPLOAD_SERVICE_FREQUENCY, alarmIntent);
        */


    }


    @Override
    public void onMessageReceived(MessageEvent event) {

        Log.d(TAG, "onMessageReceived");

        if (event.getPath().equals(Util.GAC_PATH_TEST_ACTIVITY)) {

            Toast.makeText(this, "Hello from Phone!", Toast.LENGTH_LONG).show();

        } else if (event.getPath().equals(Util.GAC_PATH_START_LOGGING)) {

            Log.d(TAG, "GAC Start Logging");
            if(!isLoggingServiceRunning() || !isSensorDataSavingServiceRunning())
                startBackgroundLogging();


        } else if (event.getPath().equals(Util.GAC_PATH_STOP_LOGGING)) {

            Log.d(TAG, "GAC Stop Logging");
            stopBackgroundLogging();

        } else if(event.getPath().equals(Util.GAC_PATH_UPLOAD_DATA)){

            Intent mServiceIntent = new Intent(this, ZipUploadService.class);
            mServiceIntent.setAction(ZipUploadService.ACTION_MANUAL_UPLOAD_DATA);
            this.startService(mServiceIntent);

        } else if(event.getPath().equals(Util.GAC_PATH_CONFIRM_FILE_RECEIVED)){

            Log.d(TAG, "Received GAC_PARH: " + Util.GAC_PATH_CONFIRM_FILE_RECEIVED+ " Message Contend was: " + event.getData().toString());


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
                if (eventUri.contains (Util.GAC_PATH_PREFERENCES)) {

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


                    String dataString = dataItem.getDataMap().getString(Util.PREFERENCES_SERVER_URL);
                    Log.d(TAG, "server url = " + dataString);
                    editor.putString(Util.PREFERENCES_SERVER_URL, dataString);

                    dataString = dataItem.getDataMap().getString(Util.PREFERENCES_SERVER_PORT);
                    Log.d(TAG, "port = " + dataString);
                    editor.putString(Util.PREFERENCES_SERVER_PORT, dataString);

                    dataString = dataItem.getDataMap().getString(Util.PREFERENCES_NAME);
                    Log.d(TAG, "name = " + dataString);
                    editor.putString(Util.PREFERENCES_NAME, dataString);

                    data = dataItem.getDataMap().getBoolean(Util.PREFERENCES_ANONYMIZE);
                    Log.d(TAG, "anonymize = " + dataString);
                    editor.putBoolean(Util.PREFERENCES_ANONYMIZE, data);

                    dataString = dataItem.getDataMap().getString(Util.PREFERENCES_SAMPLING_RATE);
                    Log.d(TAG, "sampling rate = " + dataString);
                    editor.putString(Util.PREFERENCES_SAMPLING_RATE, dataString);


                    editor.commit();

                }

            }

        }
    }

    private void startBackgroundLogging() {

        Log.d(TAG, "starting Background Logging ...");

        Intent loggingServiceIntent = new Intent(this, LoggingService.class);
        loggingServiceIntent.setAction(LoggingService.ACTION_START_LOGGING);
        this.startService(loggingServiceIntent);

        Intent sensorDataSavingServiceIntent = new Intent(this, SensorDataSavingService.class);
        sensorDataSavingServiceIntent.setAction(SensorDataSavingService.ACTION_START_SERVICE);
        this.startService(sensorDataSavingServiceIntent);

    }

    public void stopBackgroundLogging() {

        Log.d(TAG, "stopping Background Logging ...");

        Intent loggingServiceIntent = new Intent(this, LoggingService.class);
        loggingServiceIntent.setAction(LoggingService.ACTION_STOP_LOGGING);
        this.startService(loggingServiceIntent);

        Intent sensorDataSavingServiceIntent = new Intent(this, SensorDataSavingService.class);
        sensorDataSavingServiceIntent.setAction(SensorDataSavingService.ACTION_STOP_SERVICE);
        this.startService(sensorDataSavingServiceIntent);

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

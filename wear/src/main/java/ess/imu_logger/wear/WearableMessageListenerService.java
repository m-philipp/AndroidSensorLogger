package ess.imu_logger.wear;

/**
 * Created by martin on 09.09.2014.
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
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

    private GoogleApiClient mGoogleApiClient;


    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    SharedPreferences sharedPrefs;


    private static final String TAG = "ess.imu_logger.wear.WearableMessageListenerService";

    @Override
    public void onCreate(){
        super.onCreate();

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);


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


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy(){
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onMessageReceived(MessageEvent event) {

        if (event.getPath().equals(Util.GAC_PATH_TEST_ACTIVITY)) {

            Toast.makeText(this, "Hello from Phone!", Toast.LENGTH_LONG).show();

        } else if (event.getPath().equals(Util.GAC_PATH_ANNOTATE_SMOKING_ACTIVITY)) {

            Intent startIntent = new Intent(this, AnnotateSmoking.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);

        } else if (event.getPath().equals(Util.GAC_PATH_START_LOGGING)) {

            startBackgroundLogging();

        } else if (event.getPath().equals(Util.GAC_PATH_STOP_LOGGING)) {

            stopBackgroundLogging();

        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d(TAG, "DataItem deleted: " + event.getDataItem().getUri());
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                Log.d(TAG, "DataItem changed: " + event.getDataItem().getUri());

                String eventUri = event.getDataItem().getUri().toString();
                if (eventUri.contains (Util.GAC_PATH_PREFERENCES)) {

                    DataMapItem dataItem = DataMapItem.fromDataItem(event.getDataItem());
                    Boolean data = dataItem.getDataMap().getBoolean(Util.PREFERENCES_ACCELEROMETER);
                    Log.d(TAG, "Acc = " + data);
                    data = dataItem.getDataMap().getBoolean(Util.PREFERENCES_GYROSCOPE);
                    Log.d(TAG, "gyro = " + data);
                    data = dataItem.getDataMap().getBoolean(Util.PREFERENCES_MAGNETIC_FIELD);
                    Log.d(TAG, "mag = " + data);

                }
            }

        }
    }

    // TODO get preferences

    private void startBackgroundLogging() {

        Intent loggingServiceIntent = new Intent(this, LoggingService.class);
        loggingServiceIntent.setAction(LoggingService.ACTION_START_LOGGING);
        this.startService(loggingServiceIntent);

        Intent sensorDataSavingServiceIntent = new Intent(this, SensorDataSavingService.class);
        sensorDataSavingServiceIntent.setAction(SensorDataSavingService.ACTION_START_SERVICE);
        this.startService(sensorDataSavingServiceIntent);

    }

    public void stopBackgroundLogging() {

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

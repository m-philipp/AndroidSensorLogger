package de.smart_sense.tracker.wear;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FilenameFilter;

import de.smart_sense.tracker.libs.TransferDataAsAssets;
import de.smart_sense.tracker.libs.Util;
import de.smart_sense.tracker.libs.WearableMessageSenderService;
import de.smart_sense.tracker.libs.data_save.SensorDataSavingService;
import de.smart_sense.tracker.libs.data_zip_upload.ZipUploadService;

import de.smart_sense.tracker.wear.logging.WearLoggingService;
import static de.smart_sense.tracker.wear.WearUtil.*;
import static de.smart_sense.tracker.libs.Util.*;

public class DebugActivity extends Activity implements
        GoogleApiClient.OnConnectionFailedListener{

    SharedPreferences sharedPrefs;
    private GoogleApiClient mGoogleApiClient;

    private static final String TAG = "de.smart_sense.tracker.wear.Debug";
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false); // false ensures this is only executed once

        //sharedPrefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 100); // 0,1 second


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addOnConnectionFailedListener(this)
                .build();
    }



    public void onStartLiveScreen(View v) {
        Intent intent = new Intent(this, ImuLiveScreen.class);
        startActivity(intent);
    }

    public void onSendMessageToPhone(View v) {
        // TODO
        sendMessageToCompanion(Util.GAC_PATH_TEST_ACTIVITY);
    }

    public void sendImplicitIntent(View v) {
        Intent openIntent = new Intent();
        openIntent.setAction(Util.ACTION_ANNOTATE);
        openIntent.setType("text/plain");
        startActivity(openIntent);
    }

    private static final String COUNT_KEY = "/count";
    private int count = 0;
    public void onSendDataObjectToPhone(View v){
        Log.d(TAG, "sending Data Object");
        PutDataMapRequest dataMap = PutDataMapRequest.create("/count");
        dataMap.getDataMap().putInt(COUNT_KEY, count++);
        PutDataRequest request = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                .putDataItem(mGoogleApiClient, request);
    }

    public void onDeleteData(View v){

        Util.checkDirs();

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + File.separator + Util.fileDir);
        for(String filename : dir.list()) {

            Log.d(TAG, "original filename: " + filename);

            String absFileName = dir.getAbsolutePath() + File.separator + filename;

            Log.d(TAG, "deleting: " + absFileName);

            File f = new File(absFileName);
            f.setWritable(true);
            if (f.delete()) {
                Log.d(TAG, f.getName() + " is deleted!");
            } else {
                Log.d(TAG, f.getName() + " Delete operation is failed.");
            }
        }
    }


    public void onStartStopLoggingService(View v){
        Log.d(TAG, "onStartStopLoggingService");
        if(isLoggingServiceRunning(this)) {
            Intent loggingServiceIntent = new Intent(this, WearLoggingService.class);
            loggingServiceIntent.setAction(WearLoggingService.ACTION_STOP_LOGGING);
            this.startService(loggingServiceIntent);
        } else {
            Intent loggingServiceIntent = new Intent(this, WearLoggingService.class);
            loggingServiceIntent.setAction(WearLoggingService.ACTION_START_LOGGING);
            this.startService(loggingServiceIntent);
        }
    }

    public void onStartStopSensorDataSavingService(View v){
        Log.d(TAG, "onStartStopLoggingService");
        if(isSensorDataSavingServiceRunning(this)) {
            Intent sensorDataSavingServiceIntent = new Intent(this, SensorDataSavingService.class);
            sensorDataSavingServiceIntent.setAction(SensorDataSavingService.ACTION_STOP_SERVICE);
            this.startService(sensorDataSavingServiceIntent);
        } else {
            Intent sensorDataSavingServiceIntent = new Intent(this, SensorDataSavingService.class);
            sensorDataSavingServiceIntent.setAction(SensorDataSavingService.ACTION_START_SERVICE);
            this.startService(sensorDataSavingServiceIntent);
        }
    }

    public void onStartStopZipUploadService(View v){
        Log.d(TAG, "onStartStopLoggingService");
        if(!isSensorDataSavingServiceRunning(this)) {
            Intent mServiceIntent = new Intent(this, ZipUploadService.class);
            mServiceIntent.setAction(ZipUploadService.ACTION_START_ZIPPER_ONLY);
            startService(mServiceIntent);
        }
    }

    public void onStartStopMessageSenderService(View v){
        Log.d(TAG, "onStartStopMessageSenderService");
        if(isMessageSenderServiceRunning(this)) {
            Intent sensorDataSavingServiceIntent = new Intent(this, WearableMessageSenderService.class);
            sensorDataSavingServiceIntent.setAction(WearableMessageSenderService.ACTION_STOP_SERVICE);
            this.startService(sensorDataSavingServiceIntent);
        } else {
            Intent sensorDataSavingServiceIntent = new Intent(this, WearableMessageSenderService.class);
            sensorDataSavingServiceIntent.setAction(WearableMessageSenderService.ACTION_START_SERVICE);
            this.startService(sensorDataSavingServiceIntent);
        }
    }

    public void onStartStopTransferDataService(View v){
        Log.d(TAG, "onStartStopTransferDataService");
        if(isTransferDataAsAssetsServiceRunning(this)) {
            Intent transferDataAsAssetsServiceIntent = new Intent(this, TransferDataAsAssets.class);
            transferDataAsAssetsServiceIntent.setAction(TransferDataAsAssets.ACTION_TRANSFER);
            this.startService(transferDataAsAssetsServiceIntent);
        } else {
            Intent transferDataAsAssetsServiceIntent = new Intent(this, TransferDataAsAssets.class);
            transferDataAsAssetsServiceIntent.setAction(TransferDataAsAssets.ACTION_STOP_SERVICE);
            this.startService(transferDataAsAssetsServiceIntent);
        }
    }






    @Override
    protected void onResume() {
        super.onResume();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 100); // 0,1 second

        Log.d(TAG, "resuming");
    }

    @Override
    protected void onPause() {
        super.onPause();

        handler.removeCallbacks(sendUpdatesToUI);

        Log.d(TAG, "pausing");
    }

    @Override
    protected void onDestroy() {

        Log.d(TAG, "destroying");

        handler.removeCallbacks(sendUpdatesToUI);
        //sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener);
        //
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            // update Name
            uiUpdate();
            handler.postDelayed(this, 100); // 0,1 seconds
        }
    };


    private void uiUpdate() {
        // update logging status
        TextView t = (TextView) findViewById(R.id.logging_service_state);


        if (isLoggingServiceRunning(this)) {
            t.setText(getResources().getText(R.string.service_running));
            t.setTextColor(getResources().getColor(R.color.my_green));
        } else {
            t.setText(getResources().getText(R.string.service_stopped));
            t.setTextColor(getResources().getColor(R.color.my_red));
        }

        t = (TextView) findViewById(R.id.zipUpload_service_state);
        if (isZipUploadServiceRunning(this)) {
            t.setText(getResources().getText(R.string.service_running));
            t.setTextColor(getResources().getColor(R.color.my_green));
        } else {
            t.setText(getResources().getText(R.string.service_stopped));
            t.setTextColor(getResources().getColor(R.color.my_red));
        }

        t = (TextView) findViewById(R.id.sensorDataSaving_service_state);
        if (isSensorDataSavingServiceRunning(this)) {
            t.setText(getResources().getText(R.string.service_running));
            t.setTextColor(getResources().getColor(R.color.my_green));
        } else {
            t.setText(getResources().getText(R.string.service_stopped));
            t.setTextColor(getResources().getColor(R.color.my_red));
        }

        t = (TextView) findViewById(R.id.messageSender_service_state);
        if (isMessageSenderServiceRunning(this)) {
            t.setText(getResources().getText(R.string.service_running));
            t.setTextColor(getResources().getColor(R.color.my_green));
        } else {
            t.setText(getResources().getText(R.string.service_stopped));
            t.setTextColor(getResources().getColor(R.color.my_red));
        }


        String prefs = "";

        prefs += (PREFERENCES_NAME + ": " + sharedPrefs.getString(PREFERENCES_NAME, "") + "\n");
        prefs += (PREFERENCES_ANNOTATION_NAME + ": " + sharedPrefs.getString(PREFERENCES_ANNOTATION_NAME, "") + "\n");
        prefs += (PREFERENCES_ANONYMIZE + ": " + sharedPrefs.getBoolean(PREFERENCES_ANONYMIZE, false) + "\n");
        prefs += (PREFERENCES_LAST_ANNOTATION + ": " + sharedPrefs.getString(PREFERENCES_LAST_ANNOTATION, "") + "\n");
        prefs += (PREFERENCES_START_ON_BOOT + ": " + sharedPrefs.getBoolean(PREFERENCES_START_ON_BOOT, false) + "\n");
        prefs += (PREFERENCES_SENSOR_ACTIVATE + ": " + sharedPrefs.getBoolean(PREFERENCES_SENSOR_ACTIVATE, false) + "\n");
        prefs += (PREFERENCES_SAMPLING_RATE + ": " + sharedPrefs.getString(PREFERENCES_SAMPLING_RATE, "") + "\n");
        prefs += (PREFERENCES_WEAR_TEMP_LOGGING + ": " + sharedPrefs.getBoolean(PREFERENCES_WEAR_TEMP_LOGGING, false) + "\n");
        prefs += (PREFERENCES_WEAR_TEMP_LOGGING_DURATION + ": " + sharedPrefs.getString(PREFERENCES_WEAR_TEMP_LOGGING_DURATION, "") + "\n");

        prefs += (PREFERENCES_ACCELEROMETER + ": " + sharedPrefs.getBoolean(PREFERENCES_ACCELEROMETER, false) + "\n");
        prefs += (PREFERENCES_GYROSCOPE + ": " + sharedPrefs.getBoolean(PREFERENCES_GYROSCOPE, false) + "\n");
        prefs += (PREFERENCES_MAGNETIC_FIELD + ": " + sharedPrefs.getBoolean(PREFERENCES_MAGNETIC_FIELD, false) + "\n");
        prefs += (PREFERENCES_AMBIENT_LIGHT + ": " + sharedPrefs.getBoolean(PREFERENCES_AMBIENT_LIGHT, false) + "\n");
        prefs += (PREFERENCES_PROXIMITY + ": " + sharedPrefs.getBoolean(PREFERENCES_PROXIMITY, false) + "\n");
        prefs += (PREFERENCES_TEMPERATURE + ": " + sharedPrefs.getBoolean(PREFERENCES_TEMPERATURE, false) + "\n");
        prefs += (PREFERENCES_HUMIDITY + ": " + sharedPrefs.getBoolean(PREFERENCES_HUMIDITY, false) + "\n");
        prefs += (PREFERENCES_PRESSURE + ": " + sharedPrefs.getBoolean(PREFERENCES_PRESSURE, false) + "\n");
        prefs += (PREFERENCES_ROTATION + ": " + sharedPrefs.getBoolean(PREFERENCES_ROTATION, false) + "\n");
        prefs += (PREFERENCES_GRAVITY + ": " + sharedPrefs.getBoolean(PREFERENCES_GRAVITY, false) + "\n");
        prefs += (PREFERENCES_LINEAR_ACCELEROMETER + ": " + sharedPrefs.getBoolean(PREFERENCES_LINEAR_ACCELEROMETER, false) + "\n");
        prefs += (PREFERENCES_STEPS + ": " + sharedPrefs.getBoolean(PREFERENCES_STEPS, false) + "\n");

        t = (TextView) findViewById(R.id.textViewSettings);
        t.setText(prefs);


    }


    private void sendMessageToCompanion(final String path) {
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
                new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        for (final Node node : getConnectedNodesResult.getNodes()) {
                            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path,
                                    new byte[0]).setResultCallback(getSendMessageResultCallback());
                        }
                    }
                }
        );

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Failed to connect to Google Api Client");
    }



    private ResultCallback<MessageApi.SendMessageResult> getSendMessageResultCallback() {
        return new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                if (!sendMessageResult.getStatus().isSuccess()) {
                    Log.e(TAG, "Failed to connect to Google Api Client with status "
                            + sendMessageResult.getStatus());
                } else {
                    Log.d(TAG, "Successfully connected to Google Api Client.");
                }
            }
        };
    }
}

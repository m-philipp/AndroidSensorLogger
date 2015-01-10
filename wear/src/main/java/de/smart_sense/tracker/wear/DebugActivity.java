package de.smart_sense.tracker.wear;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

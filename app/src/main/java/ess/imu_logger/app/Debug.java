package ess.imu_logger.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import ess.imu_logger.app.bluetoothLogger.BluetoothScannerService;
import ess.imu_logger.app.markdownViewer.AboutScreen;
import ess.imu_logger.app.markdownViewer.HelpScreen;
import ess.imu_logger.app.markdownViewer.IntroductionScreen;
import ess.imu_logger.libs.Util;
import ess.imu_logger.libs.WearableMessageSenderService;
import ess.imu_logger.libs.data_save.SensorDataSavingService;
import ess.imu_logger.libs.data_zip_upload.ZipUploadService;
import ess.imu_logger.libs.logging.LoggingService;

public class Debug extends Activity implements
        GoogleApiClient.OnConnectionFailedListener{

    SharedPreferences sharedPrefs;
    private GoogleApiClient mGoogleApiClient;

    private static final String TAG = "ess.imu_logger.app.Debug";
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.debug, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, ApplicationSettings.class);
            startActivity(intent);
        } else if (id == R.id.action_help) {
            Intent intent = new Intent(this, HelpScreen.class);
            startActivity(intent);
        } else if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutScreen.class);
            startActivity(intent);
        } else if (id == R.id.action_introduction) {
            Intent intent = new Intent(this, IntroductionScreen.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void onStartLiveScreen(View v) {
        Intent intent = new Intent(this, ImuLiveScreen.class);
        startActivity(intent);
    }

    public void onSendMessageToWearable(View v) {
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
    public void onSendDataObjectToWearable(View v){
        Log.d(TAG, "sending Data Object");
        PutDataMapRequest dataMap = PutDataMapRequest.create("/count");
        dataMap.getDataMap().putInt(COUNT_KEY, count++);
        PutDataRequest request = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                .putDataItem(mGoogleApiClient, request);
    }



    public void onStartStopBluetoothService(View v){
        Log.d(TAG, "onStartStopBluetoothService");
        if(!isBluetoothServiceRunning()) {
            Intent loggingServiceIntent = new Intent(this, BluetoothScannerService.class);
            this.startService(loggingServiceIntent);
        } else {
            Intent loggingServiceIntent = new Intent(this, BluetoothScannerService.class);
            loggingServiceIntent.setAction(BluetoothScannerService.ACTION_STOP_SERVICE);
            this.startService(loggingServiceIntent);
        }
    }
    public void onStartStopLoggingService(View v){
        Log.d(TAG, "onStartStopLoggingService");
        if(isLoggingServiceRunning()) {
            Intent loggingServiceIntent = new Intent(this, LoggingService.class);
            loggingServiceIntent.setAction(LoggingService.ACTION_STOP_LOGGING);
            this.startService(loggingServiceIntent);
        } else {
            Intent loggingServiceIntent = new Intent(this, LoggingService.class);
            loggingServiceIntent.setAction(LoggingService.ACTION_START_LOGGING);
            this.startService(loggingServiceIntent);
        }
    }

    public void onStartStopSensorDataSavingService(View v){
        Log.d(TAG, "onStartStopLoggingService");
        if(isSensorDataSavingServiceRunning()) {
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
        if(!isSensorDataSavingServiceRunning()) {
            Intent mServiceIntent = new Intent(this, ZipUploadService.class);
            mServiceIntent.setAction(ZipUploadService.ACTION_START_SERVICE);
            startService(mServiceIntent);
        }
    }

    public void onStartStopMessageSenderService(View v){
        Log.d(TAG, "onStartStopMessageSenderService");
        if(isMessageSenderServiceRunning()) {
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


        if (isLoggingServiceRunning()) {
            t.setText(getResources().getText(R.string.service_running));
            t.setTextColor(getResources().getColor(R.color.my_green));
        } else {
            t.setText(getResources().getText(R.string.service_stopped));
            t.setTextColor(getResources().getColor(R.color.my_red));
        }

        t = (TextView) findViewById(R.id.zipUpload_service_state);
        if (isZipUploadServiceRunning()) {
            t.setText(getResources().getText(R.string.service_running));
            t.setTextColor(getResources().getColor(R.color.my_green));
        } else {
            t.setText(getResources().getText(R.string.service_stopped));
            t.setTextColor(getResources().getColor(R.color.my_red));
        }

        t = (TextView) findViewById(R.id.sensorDataSaving_service_state);
        if (isSensorDataSavingServiceRunning()) {
            t.setText(getResources().getText(R.string.service_running));
            t.setTextColor(getResources().getColor(R.color.my_green));
        } else {
            t.setText(getResources().getText(R.string.service_stopped));
            t.setTextColor(getResources().getColor(R.color.my_red));
        }

        t = (TextView) findViewById(R.id.lighterBluetooth_service_state);
        if (isBluetoothServiceRunning()) {
            t.setText(getResources().getText(R.string.service_running));
            t.setTextColor(getResources().getColor(R.color.my_green));
        } else {
            t.setText(getResources().getText(R.string.service_stopped));
            t.setTextColor(getResources().getColor(R.color.my_red));
        }

        t = (TextView) findViewById(R.id.messageSender_service_state);
        if (isMessageSenderServiceRunning()) {
            t.setText(getResources().getText(R.string.service_running));
            t.setTextColor(getResources().getColor(R.color.my_green));
        } else {
            t.setText(getResources().getText(R.string.service_stopped));
            t.setTextColor(getResources().getColor(R.color.my_red));
        }




    }

    private boolean isMessageSenderServiceRunning() {
        return isServiceRunning(WearableMessageSenderService.class.getName());
    }

    private boolean isLoggingServiceRunning() {
        return isServiceRunning(LoggingService.class.getName());
    }

    private boolean isSensorDataSavingServiceRunning() {
        return isServiceRunning(SensorDataSavingService.class.getName());
    }

    private boolean isBluetoothServiceRunning() {
        return isServiceRunning(BluetoothScannerService.class.getName());
    }

    private boolean isZipUploadServiceRunning() {
        return isServiceRunning(ZipUploadService.class.getName());
    }

    private boolean isServiceRunning(String classname) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (classname.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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

package ess.imu_logger.libs;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

import ess.imu_logger.R;
import ess.imu_logger.libs.Util;
import ess.imu_logger.libs.data_save.SensorDataSavingService;
import ess.imu_logger.libs.data_zip_upload.ZipUploadService;
import ess.imu_logger.libs.logging.LoggingService;

public abstract class StartActivity extends Activity implements
        GoogleApiClient.OnConnectionFailedListener{

    protected SharedPreferences sharedPrefs;
    private final Handler handler = new Handler();

    protected GoogleApiClient mGoogleApiClient;
    protected Long sensorEventNo = 0L;

    private static final String TAG = "ess.imu_logger.libs.StartActivity";

    public static final String BROADCAST_SENSOR_EVENT_NO = "ess.imu_logger.libs.sensorEventNo";
    public static final String EXTRA_SENSOR_EVENT_NO = "ess.imu_logger.libs.sensorEventNo.Extra";


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.d(TAG, "Broadcast Receiver received a Broadcast");

            // TODO check is the extra is really there

            if (intent != null) {
                if (action.equals(BROADCAST_SENSOR_EVENT_NO)) {

                    Long seo  = intent.getLongExtra(EXTRA_SENSOR_EVENT_NO, 0L);
                    sensorEventNo = seo / 1000;

                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false); // false ensures this is only executed once
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addOnConnectionFailedListener(this)
                .build();


        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 100); // 0,1 second

        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_SENSOR_EVENT_NO);
        registerReceiver(receiver, filter);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);


        List<Sensor> sensors;
        SensorManager mgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensors = mgr.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : sensors) {
            Log.d("Sensors", "" + sensor.getName());
        }
    }


    protected void sendMessageToCompanion(final String path) {

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

    public void triggerManualDataUpload(View v) {

        // TODO use it

        Intent mServiceIntent = new Intent(this, ZipUploadService.class);
        mServiceIntent.setAction(ZipUploadService.ACTION_MANUAL_UPLOAD_DATA);
        this.startService(mServiceIntent);

		/*
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(500);
		*/

    }

    public void annotateSmoking() {

        Log.i(TAG, "annotateSmoking called");

        Intent sendIntent = new Intent(SensorDataSavingService.BROADCAST_ANNOTATION);
        sendIntent.putExtra(SensorDataSavingService.EXTRA_ANNOTATION_NAME, "smoking");
        sendIntent.putExtra(SensorDataSavingService.EXTRA_ANNOTATION_VIA, "smartphone_ui");
        sendBroadcast(sendIntent);

    }

    protected void startBackgroundLogging() {

        Log.d(TAG, "starting Background Logging ...");

        Intent loggingServiceIntent = new Intent(this, LoggingService.class);
        loggingServiceIntent.setAction(LoggingService.ACTION_START_LOGGING);
        this.startService(loggingServiceIntent);

        Intent sensorDataSavingServiceIntent = new Intent(this, SensorDataSavingService.class);
        sensorDataSavingServiceIntent.setAction(SensorDataSavingService.ACTION_START_SERVICE);
        this.startService(sensorDataSavingServiceIntent);

    }

    protected void stopBackgroundLogging() {

        Log.d(TAG, "stopping Background Logging ...");

        Intent loggingServiceIntent = new Intent(this, LoggingService.class);
        loggingServiceIntent.setAction(LoggingService.ACTION_STOP_LOGGING);
        this.startService(loggingServiceIntent);

        Intent sensorDataSavingServiceIntent = new Intent(this, SensorDataSavingService.class);
        sensorDataSavingServiceIntent.setAction(SensorDataSavingService.ACTION_STOP_SERVICE);
        this.startService(sensorDataSavingServiceIntent);

    }


    @Override
    protected void onResume() {
        super.onResume();

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 100); // 0,1 second

        updateRequest = 0;
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

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        unregisterReceiver(receiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

        handler.removeCallbacks(sendUpdatesToUI);
        //sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener);


        super.onDestroy();
    }
    private int updateRequest = 0;
    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            // update Name
            uiUpdate();

            if(updateRequest == 0) {
                Log.d(TAG, "Update Folder Size");

                FolderSizeRetriever fsr = new FolderSizeRetriever();
                fsr.execute(""); // 1.6GB can take about 28 Seconds with small files.

                updateRequest = 1000; // 100 Seconds and on Start
            }
            updateRequest--;

            handler.postDelayed(this, 100); // 0,1 seconds
        }
    };

    protected abstract void uiUpdate();




    protected boolean isLoggingServiceRunning() {
        return isServiceRunning(LoggingService.class.getName());
    }

    protected boolean isSensorDataSavingServiceRunning() {
        return isServiceRunning(SensorDataSavingService.class.getName());
    }

    protected boolean isZipUploadServiceRunning() {
        return isServiceRunning(ZipUploadService.class.getName());
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

    private class FolderSizeRetriever extends AsyncTask<String, Void, String> {

        private final static String TAG = "ess.imu_logger.app.StartScreen.FolderSizeRetriever";

        @Override
        protected String doInBackground(String... params) {

            Long start = System.currentTimeMillis();

            String r = Util.getFolderSize().toString();

            Long duration = System.currentTimeMillis() - start;
            Log.d(TAG, "File Size Retrieving took: " + duration.toString());

            return r;
        }

        @Override
        protected void onPostExecute(String result) {
            Float f = Util.round((Float.parseFloat(result) / (1024 * 1024)), 2);
            Log.d(TAG, "-------->> retrieved File Size: " + f.toString());

            // TODO amount_of_logged_data
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString("amount_of_logged_data", f.toString());
            editor.commit();
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }




}

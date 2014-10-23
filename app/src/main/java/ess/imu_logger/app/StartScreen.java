package ess.imu_logger.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import ess.imu_logger.libs.DataCollectorService;
import ess.imu_logger.libs.Util;
import ess.imu_logger.libs.data_save.SensorDataSavingService;
import ess.imu_logger.libs.data_zip_upload.ZipUploadService;
import ess.imu_logger.libs.logging.LoggingService;

public class StartScreen extends Activity implements
        GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient mGoogleApiClient;

    SharedPreferences sharedPrefs;

    private static final String TAG = "ess.imu_logger.app.StartScreen";
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false); // false ensures this is only executed once

        //sharedPrefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.registerOnSharedPreferenceChangeListener(listener);



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
                Util.ZIP_UPLOAD_SERVICE_FREQUENCY, alarmIntent); // TODO make Values static finals


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addOnConnectionFailedListener(this)
                .build();



        if (sharedPrefs.getBoolean("sensor_activate", false))
            startBackgroundLogging();

        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 100); // 0,1 second

        TextView t = (TextView) findViewById(R.id.amaount_of_data_to_upload);
        t.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onStartDebug();
                return true;
            }
        });


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

        handler.removeCallbacks(sendUpdatesToUI);
        //sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener);

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        super.onDestroy();
    }

    private void onStartDebug() {
        Intent intent = new Intent(this, Debug.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start_screen, menu);
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
        }
        return super.onOptionsItemSelected(item);
    }



    private void startBackgroundLogging() {

        //startRecording();

        Intent sensorDataSavingServiceIntent = new Intent(this, SensorDataSavingService.class);
        sensorDataSavingServiceIntent.setAction(SensorDataSavingService.ACTION_START_SERVICE);
        this.startService(sensorDataSavingServiceIntent);

        Intent loggingServiceIntent = new Intent(this, LoggingService.class);
        loggingServiceIntent.setAction(LoggingService.ACTION_START_LOGGING);
        this.startService(loggingServiceIntent);

    }

    public void stopBackgroundLogging() {

        //endRecording();

        Intent sensorDataSavingServiceIntent = new Intent(this, SensorDataSavingService.class);
        sensorDataSavingServiceIntent.setAction(SensorDataSavingService.ACTION_STOP_SERVICE);
        this.startService(sensorDataSavingServiceIntent);

        Intent loggingServiceIntent = new Intent(this, LoggingService.class);
        loggingServiceIntent.setAction(LoggingService.ACTION_STOP_LOGGING);
        this.startService(loggingServiceIntent);



    }

    public void triggerManualDataUpload(View v) {

        Intent mServiceIntent = new Intent(this, ZipUploadService.class);
        mServiceIntent.setAction(ZipUploadService.ACTION_MANUAL_UPLOAD_DATA);
        this.startService(mServiceIntent);

		/*
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(500);
		*/

    }

    public void annotateSmoking(View v) {

        Log.i(TAG, "annotateSmoking called");

        Intent sendIntent = new Intent();
        sendIntent.setAction(SensorDataSavingService.BROADCAST_ANNOTATION);
        sendBroadcast(sendIntent);

    }

    public void sendPreferencesToWearable(){
        Log.d(TAG, "sending Data Object");

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Util.GAC_PATH_PREFERENCES);

        DataMap dataMap = putDataMapRequest.getDataMap();

        dataMap.putString(Util.PREFERENCES_NAME, sharedPrefs.getString(Util.PREFERENCES_NAME, "Eva Musterfrau"));
        dataMap.putBoolean(Util.PREFERENCES_ANONYMIZE, sharedPrefs.getBoolean(Util.PREFERENCES_ANONYMIZE, true));

        dataMap.putBoolean(Util.PREFERENCES_SENSOR_ACTIVATE, sharedPrefs.getBoolean(Util.PREFERENCES_SENSOR_ACTIVATE, false));
        dataMap.putString(Util.PREFERENCES_SAMPLING_RATE, sharedPrefs.getString(Util.PREFERENCES_SAMPLING_RATE, "3"));

        dataMap.putBoolean(Util.PREFERENCES_ACCELEROMETER, sharedPrefs.getBoolean(Util.PREFERENCES_ACCELEROMETER, false));
        dataMap.putBoolean(Util.PREFERENCES_GYROSCOPE, sharedPrefs.getBoolean(Util.PREFERENCES_GYROSCOPE, false));
        dataMap.putBoolean(Util.PREFERENCES_MAGNETIC_FIELD, sharedPrefs.getBoolean(Util.PREFERENCES_MAGNETIC_FIELD, false));
        dataMap.putBoolean(Util.PREFERENCES_AMBIENT_LIGHT, sharedPrefs.getBoolean(Util.PREFERENCES_AMBIENT_LIGHT, false));
        dataMap.putBoolean(Util.PREFERENCES_PROXIMITY, sharedPrefs.getBoolean(Util.PREFERENCES_PROXIMITY, false));
        dataMap.putBoolean(Util.PREFERENCES_TEMPERATURE, sharedPrefs.getBoolean(Util.PREFERENCES_TEMPERATURE, false));
        dataMap.putBoolean(Util.PREFERENCES_HUMIDITY, sharedPrefs.getBoolean(Util.PREFERENCES_HUMIDITY, false));
        dataMap.putBoolean(Util.PREFERENCES_PRESSURE, sharedPrefs.getBoolean(Util.PREFERENCES_PRESSURE, false));

        dataMap.putBoolean(Util.PREFERENCES_ROTATION, sharedPrefs.getBoolean(Util.PREFERENCES_ROTATION, false));
        dataMap.putBoolean(Util.PREFERENCES_GRAVITY, sharedPrefs.getBoolean(Util.PREFERENCES_GRAVITY, false));
        dataMap.putBoolean(Util.PREFERENCES_LINEAR_ACCELEROMETER, sharedPrefs.getBoolean(Util.PREFERENCES_LINEAR_ACCELEROMETER, false));
        dataMap.putBoolean(Util.PREFERENCES_STEPS, sharedPrefs.getBoolean(Util.PREFERENCES_STEPS, false));

        dataMap.putString(Util.PREFERENCES_SERVER_URL, sharedPrefs.getString(Util.PREFERENCES_SERVER_URL, "http://example.com"));
        dataMap.putString(Util.PREFERENCES_SERVER_PORT, sharedPrefs.getString(Util.PREFERENCES_SERVER_PORT, "8080"));
        dataMap.putString(Util.PREFERENCES_UPLOAD_FREQUENCY, sharedPrefs.getString(Util.PREFERENCES_UPLOAD_FREQUENCY, "0"));



        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                .putDataItem(mGoogleApiClient, request);
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

    SharedPreferences.OnSharedPreferenceChangeListener listener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    Log.d(TAG, "----------------- PREFS CHANGED !! ------------");
                    Log.d(TAG, key);

                    Log.d(TAG, "sensor_activate status: " + sharedPrefs.getBoolean("sensor_activate", false));


                    if(key.equals("sensor_activate")) {
                        // start/stop the Logging Service
                        if (sharedPrefs.getBoolean("sensor_activate", false)) {
                            startBackgroundLogging();
                            sendMessageToCompanion(Util.GAC_PATH_START_LOGGING);

                        } else {
                            stopBackgroundLogging();
                            sendMessageToCompanion(Util.GAC_PATH_STOP_LOGGING);

                        }
                    }
                    uiUpdate();
                    sendPreferencesToWearable();


                }
            };

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

    private void uiUpdate() {
        TextView t = (TextView) findViewById(R.id.welcome_name);
        t.setText("Hi, " + sharedPrefs.getString("name", "Kunibert"));

        t = (TextView) findViewById(R.id.amaount_of_data_to_upload);
        t.setText(sharedPrefs.getString("amount_of_logged_data", "0.0") +  " MB");

        // update logging status
        t = (TextView) findViewById(R.id.logging_service_state);

        if (isLoggingServiceRunning() && isSensorDataSavingServiceRunning()) {
            t.setText(getResources().getText(R.string.service_running));
            t.setTextColor(getResources().getColor(R.color.my_green));
        } else {
            t.setText(getResources().getText(R.string.service_stopped));
            t.setTextColor(getResources().getColor(R.color.my_red));
        }

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
    // ------------------------------------------------------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------------------------------
    /*
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            service = ((DataCollectorService.MyBinder) binder).getService();

        }

        public void onServiceDisconnected(ComponentName className) {
            service = null;
        }
    };

    private void startRecording() {
        if (!isLoggerServiceRunning()) {
            startService(new Intent(this, DataCollectorService.class));
            bindService(new Intent(this, DataCollectorService.class), mConnection, Context.BIND_AUTO_CREATE);
            serviceStopped = false;
        }
    }
    DataCollectorService service = null;
    private boolean serviceStopped = true;

    private void endRecording() {
        // stop annotations

        if (isLoggerServiceRunning()) {
            if (service != null) {
                unbindService(mConnection);
                service = null;
            }
            stopService(new Intent(this, DataCollectorService.class));

        }
    }
    private boolean isLoggerServiceRunning() {
        return isServiceRunning(DataCollectorService.class.getName());
    }

       */
    // ------------------------------------------------------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------------------------------
    // ------------------------------------------------------------------------------------------------------------------------------------

    private boolean isLoggingServiceRunning() {
        return isServiceRunning(LoggingService.class.getName());
    }

    private boolean isSensorDataSavingServiceRunning() {
        return isServiceRunning(SensorDataSavingService.class.getName());
    }

    private boolean isZipUploadServiceRunning() {
        return isServiceRunning(ZipUploadService.class.getName());
    }

    private boolean isServiceRunning(String classname) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (classname.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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

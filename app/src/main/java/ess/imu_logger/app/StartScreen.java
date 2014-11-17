package ess.imu_logger.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import ess.imu_logger.libs.StartActivity;
import ess.imu_logger.libs.Util;
import ess.imu_logger.libs.data_zip_upload.ZipUploadService;
import ess.imu_logger.libs.myReceiver;

public class StartScreen extends StartActivity {


    private static final String TAG = "ess.imu_logger.app.StartScreen";

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate");

        if(getIntent() != null && getIntent().getAction().equals(Util.ACTION_ANNOTATE_SMOKING)){
            Log.d(TAG, "annotate Smoke ACTION");
            annotateSmoking();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);


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


        sharedPrefs.registerOnSharedPreferenceChangeListener(listener);


        if (sharedPrefs.getBoolean(Util.PREFERENCES_SENSOR_ACTIVATE, false))
            startBackgroundLogging();

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

        if(getIntent() != null && getIntent().getAction().equals(Util.ACTION_ANNOTATE_SMOKING)){
            Log.d(TAG, "annotate Smoke ACTION");
            annotateSmoking();
        }

        Log.d(TAG, "resuming");
    }

    @Override
    protected void onPause() {
        super.onPause();


        Log.d(TAG, "pausing");
    }

    @Override
    protected void onDestroy() {

        Log.d(TAG, "destroying");

        //sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener);

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


    public void annotateSmoking(View v) {
        annotateSmoking();
    }

    public void triggerManualDataUpload(View v) {

        Intent mServiceIntent = new Intent(this, ZipUploadService.class);
        mServiceIntent.setAction(ZipUploadService.ACTION_MANUAL_UPLOAD_DATA);
        this.startService(mServiceIntent);


		/*
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(500);
		*/

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

    @Override
    protected void uiUpdate() {
        TextView t = (TextView) findViewById(R.id.welcome_name);
        t.setText("Hi, " + sharedPrefs.getString("name", "Kunibert"));

        t = (TextView) findViewById(R.id.amaount_of_data_to_upload);
        t.setText(sharedPrefs.getString("amount_of_logged_data", "0.0") +  " MB");

        t = (TextView) findViewById(R.id.id_sensor_event);
        t.setText(Long.toString(sensorEventNo) + " k");
        Log.d(TAG, "updating Sensor events to: " + sensorEventNo + " k");

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



}

package ess.imu_logger;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import ess.imu_logger.data_save.SensorDataSavingService;
import ess.imu_logger.data_zip_upload.ZipUploadService;

public class StartScreen extends Activity {


    SharedPreferences sharedPrefs;

    private static final String TAG = "ess.imu_logger.StartScreen";
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
                10000, alarmIntent); // TODO make Values static finals






        if (sharedPrefs.getBoolean("sensor_activate", false))
            startBackgroundLogging();

        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 100); // 0,1 second


    }

    @Override
    protected void onResume() {
        super.onResume();

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
        super.onDestroy();
        Log.d(TAG, "destroying");

        handler.removeCallbacks(sendUpdatesToUI);
        //sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener);
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

    public void onStartLiveScreen(View v) {
        Intent intent = new Intent(this, ImuLiveScreen.class);
        startActivity(intent);
    }

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

    SharedPreferences.OnSharedPreferenceChangeListener listener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                    Log.d(TAG, "----------------- PREFS CHANGED !! ------------");
                    Log.d(TAG, key);

                    Log.d(TAG, "sensor_activat status: " + sharedPrefs.getBoolean("sensor_activate", false));


                    if(key.equals("sensor_activate")) {
                        // start/stop the Logging Service
                        if (sharedPrefs.getBoolean("sensor_activate", false)) {
                            startBackgroundLogging();
                        } else {
                            stopBackgroundLogging();
                        }
                    }
                    uiUpdate();


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



        /*
        if( sharedPrefs.getBoolean("sensor_activate", false)){
            t.setText(getResources().getText(R.string.logging_service_running));
            t.setTextColor(getResources().getColor(R.color.my_green));
        }
        else{
            t.setText(getResources().getText(R.string.logging_service_stopped));
            t.setTextColor(getResources().getColor(R.color.my_red));
        }
        */
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




    }


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

        private final static String TAG = "ess.imu_logger.StartScreen.FolderSizeRetriever";

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

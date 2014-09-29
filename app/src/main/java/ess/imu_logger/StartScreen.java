package ess.imu_logger;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;

import ess.imu_logger.data_save.SensorDataSavingService;
import ess.imu_logger.data_zip_upload.ZipUploadService;

public class StartScreen extends Activity {


    SharedPreferences sharedPrefs;

    private static final String TAG = "ess.imu_logger.StartScreen";

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

        Intent mServiceIntent = new Intent(this, ZipUploadService.class);
        mServiceIntent.setAction(ZipUploadService.ACTION_START_SERVICE);
        this.startService(mServiceIntent);

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

                    // start/stop the Logging Service
                    if (sharedPrefs.getBoolean("sensor_activate", false)) {
                        startBackgroundLogging();
                    } else {
                        stopBackgroundLogging();
                    }

                    uiUpdate();


                }
            };

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            // update Name
            uiUpdate();
            handler.postDelayed(this, 1000); // 0,1 seconds
        }
    };

    private void uiUpdate() {
        TextView t = (TextView) findViewById(R.id.welcome_name);
        t.setText("Hi, " + sharedPrefs.getString("name", "Kunibert"));

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



        Long l = getFolderSize();
        Float f = Util.round((l.floatValue() / (1024 * 1024)), 2);
        Log.d(TAG, "-------->> " + f.toString());
        t = (TextView) findViewById(R.id.amaount_of_data_to_upload);
        t.setText(f.toString() + " MB");
    }


    public static long getFolderSize() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + File.separator + Util.fileDir);
        return getFolderSize(dir);
    }

    public static long getFolderSize(File dir) {

        if (Util.isExternalStorageReadable()) {
            long size = 0;
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    // System.out.println(file.getName() + " " + file.length());
                    size += file.length();
                } else
                    size += getFolderSize(file);
            }
            return size;
        } else {
            return 0L;
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


}

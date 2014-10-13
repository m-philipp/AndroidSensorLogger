package ess.imu_logger.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
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

import ess.imu_logger.app.R;
import ess.imu_logger.libs.data_save.SensorDataSavingService;
import ess.imu_logger.libs.data_zip_upload.ZipUploadService;
import ess.imu_logger.libs.logging.LoggingService;

public class Debug extends Activity {

    SharedPreferences sharedPrefs;

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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onStartLiveScreen(View v) {
        Intent intent = new Intent(this, ImuLiveScreen.class);
        startActivity(intent);
    }

    public void onSendMessageToWearable(View v) {
        // TODO
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
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (classname.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}

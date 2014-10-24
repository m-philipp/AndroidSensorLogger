package ess.imu_logger.wear;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import ess.imu_logger.libs.Util;
import ess.imu_logger.libs.data_save.SensorDataSavingService;
import ess.imu_logger.libs.data_zip_upload.ZipUploadService;
import ess.imu_logger.libs.logging.LoggingService;

public class StartActivity extends ess.imu_logger.libs.StartActivity {

    //private TextView mTextView;


    private static final String TAG = "ess.imu_logger.wear.StartScreen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);
        /*
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);


        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });
        */


    }

    public void onStartLiveScreen(View v) {
        Intent intent = new Intent(this, ImuLiveScreen.class);
        startActivity(intent);
    }

    public void onStartAnnotateSmoking(View v) {
        Intent intent = new Intent(this, AnnotateSmoking.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

        super.onDestroy();
    }

    @Override
    protected void uiUpdate() {

        TextView t = (TextView) findViewById(R.id.id_mb_to_upload);
        t.setText(sharedPrefs.getString("amount_of_logged_data", "0.0") +  " MB");

        // update logging status
        t = (TextView) findViewById(R.id.id_logging_running);

        if (isLoggingServiceRunning() && isSensorDataSavingServiceRunning()) {
            //Log.d(TAG, "uiUpdate said running");
            t.setText(getResources().getText(R.string.running));
            t.setTextColor(getResources().getColor(R.color.my_green));
        } else {
            //Log.d(TAG, "uiUpdate said not running");
            t.setText(getResources().getText(R.string.stopped));
            t.setTextColor(getResources().getColor(R.color.my_red));
        }

    }


}

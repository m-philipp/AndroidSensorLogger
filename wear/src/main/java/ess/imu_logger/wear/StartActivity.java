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

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

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

    public void sendObjectToWearable(){
            Log.d(TAG, "sending Data Object");
            PutDataMapRequest dataMap = PutDataMapRequest.create("/count");
            dataMap.getDataMap().putInt("/count", 42);
            PutDataRequest request = dataMap.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                    .putDataItem(mGoogleApiClient, request);
        }

    public void onStartLiveScreen_ORIGINAL(View v) {

        Intent intent = new Intent(this, ImuLiveScreen.class);
        startActivity(intent);


    }

    private static final String COUNT_KEY = "/count";
    private int count = 0;
    public void onStartLiveScreen(View v){

        Log.d(TAG, "sending Data Object");

        Intent mServiceIntent = new Intent(this, TransferDataAsAssets.class);
        mServiceIntent.setAction(TransferDataAsAssets.ACTION_TRANSFER);
        this.startService(mServiceIntent);
        /*
        PutDataMapRequest dataMap = PutDataMapRequest.create("/count");
        dataMap.getDataMap().putInt(COUNT_KEY, count++);
        PutDataRequest request = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                .putDataItem(mGoogleApiClient, request);
         */

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

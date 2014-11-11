package ess.imu_logger.wear;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import ess.imu_logger.libs.*;
import ess.imu_logger.libs.data_zip_upload.ZipUploadService;

public class StartActivity extends ess.imu_logger.libs.StartActivity {

    //private TextView mTextView;

    private AlarmManager alarmMgr;
    private PendingIntent transferDataAlarmIntent;
    private PendingIntent zipDataAlarmIntent;

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


        Intent transferDataIntent = new Intent(this, ess.imu_logger.libs.myReceiver.class);
        transferDataIntent.setAction(TransferDataAsAssets.ACTION_TRANSFER);
        transferDataAlarmIntent = PendingIntent.getBroadcast(this, 0, transferDataIntent, 0);

        Intent zipDataIntent = new Intent(this, ess.imu_logger.libs.myReceiver.class);
        zipDataIntent.setAction(ZipUploadService.ACTION_START_ZIPPER_ONLY);
        zipDataAlarmIntent = PendingIntent.getBroadcast(this, 0, zipDataIntent, 0);

        if(alarmMgr == null){
            Log.d(TAG, "AlarmManager was null");
            alarmMgr = (AlarmManager) this.getSystemService(this.ALARM_SERVICE);
        }


        alarmMgr.cancel(transferDataAlarmIntent);
        alarmMgr.cancel(zipDataAlarmIntent);

        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                1000,
                Util.ZIP_UPLOAD_SERVICE_FREQUENCY, transferDataAlarmIntent);

        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                1000,
                Util.ZIP_UPLOAD_SERVICE_FREQUENCY, zipDataAlarmIntent);




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


    public void onStartLiveScreen(View v){

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

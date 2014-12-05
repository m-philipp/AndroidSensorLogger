package ess.imu_logger.wear;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import ess.imu_logger.libs.*;
import ess.imu_logger.libs.data_zip_upload.ZipUploadService;
import ess.imu_logger.wear.logging.WearLoggingService;

public class WearStartActivity extends StartActivity {

    //private TextView mTextView;

    private AlarmManager alarmMgr;
    private PendingIntent transferDataAlarmIntent;
    private PendingIntent zipDataAlarmIntent;

    private static final String TAG = "ess.imu_logger.wear.StartScreen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);



        Intent transferDataIntent = new Intent(this, myReceiver.class);
        transferDataIntent.setAction(TransferDataAsAssets.ACTION_TRANSFER);
        transferDataAlarmIntent = PendingIntent.getBroadcast(this, 0, transferDataIntent, 0);

        Intent zipDataIntent = new Intent(this, myReceiver.class);
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




    public void onStartLiveScreen(View v){

        Intent intent = new Intent(this, ImuLiveScreen.class);
        startActivity(intent);
    }

    public void onStartAnnotate(View v) {
        Intent intent = new Intent(this, Annotate.class);
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
        if(sharedPrefs != null)
            t.setText(sharedPrefs.getString("amount_of_logged_data", "0.0") +  " MB"); // TODO check (SONY ?) Null Pointer Exception

        t = (TextView) findViewById(R.id.id_sensor_event);
        t.setText(Long.toString(sharedPrefs.getLong("sensor_events_logged", 0L) / 1000) + " k");

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


    protected boolean isLoggingServiceRunning() {
        return isServiceRunning(WearLoggingService.class.getName());
    }

}

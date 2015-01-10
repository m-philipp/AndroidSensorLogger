package de.smart_sense.tracker.wear;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import de.smart_sense.tracker.libs.*;
import de.smart_sense.tracker.libs.data_zip_upload.ZipUploadService;

import static de.smart_sense.tracker.libs.Util.isSensorDataSavingServiceRunning;
import static de.smart_sense.tracker.wear.WearUtil.*;

public class WearStartActivity extends StartActivity {

    //private TextView mTextView;

    private AlarmManager alarmMgr;
    private PendingIntent transferDataAlarmIntent, zipDataAlarmIntent, periodicAlarmIntent;


    WatchViewStub wvs;

    private static final String TAG = "de.smart_sense.tracker.wear.StartScreen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_start);


        alarmManagerSetup();

        updateLoggingState(this, sharedPrefs);


        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                wvs = stub;


                TextView t = (TextView) wvs.findViewById(R.id.id_mb_to_upload);
                t.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        onStartDebug();
                        return true;
                    }
                });

                uiUpdate();



            }

        });



    }

    private void alarmManagerSetup() {
        Intent transferDataIntent = new Intent(this, WearReceiver.class);
        transferDataIntent.setAction(TransferDataAsAssets.ACTION_TRANSFER);
        transferDataAlarmIntent = PendingIntent.getBroadcast(this, 0, transferDataIntent, 0);

        Intent zipDataIntent = new Intent(this, WearReceiver.class);
        zipDataIntent.setAction(ZipUploadService.ACTION_START_ZIPPER_ONLY);
        zipDataAlarmIntent = PendingIntent.getBroadcast(this, 0, zipDataIntent, 0);

        Intent periodicIntent = new Intent(this, WearReceiver.class);
        periodicIntent.setAction(Util.ACTION_PERIODIC_ALARM);
        periodicAlarmIntent = PendingIntent.getBroadcast(this, 0, periodicIntent, 0);

        if(alarmMgr == null){
            Log.d(TAG, "AlarmManager was null");
            alarmMgr = (AlarmManager) this.getSystemService(this.ALARM_SERVICE);
        }


        alarmMgr.cancel(transferDataAlarmIntent);
        alarmMgr.cancel(zipDataAlarmIntent);
        alarmMgr.cancel(periodicAlarmIntent);

        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                1000,
                Util.TRANSFER_SERVICE_FREQUENCY, transferDataAlarmIntent);

        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                1000,
                Util.ZIP_UPLOAD_SERVICE_FREQUENCY, zipDataAlarmIntent);

        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                1000,
                Util.PERIODIC_ALARM_FREQUENCY, periodicAlarmIntent);
    }

    private void onStartDebug() {
        Intent intent = new Intent(this, DebugActivity.class);
        startActivity(intent);
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

        if(sharedPrefs == null || wvs == null)
            return;

        TextView t = (TextView) wvs.findViewById(R.id.id_mb_to_upload);
        t.setText(sharedPrefs.getString("amount_of_logged_data", "0.0") +  " MB"); // TODO check (SONY ?) Null Pointer Exception

        t = (TextView) wvs.findViewById(R.id.id_sensor_event);
        t.setText(Long.toString(sharedPrefs.getLong("sensor_events_logged", 0L) / 1000) + " k");

        // update logging status
        t = (TextView) wvs.findViewById(R.id.id_logging_running);

        if (isLoggingServiceRunning(this) && isSensorDataSavingServiceRunning(this)) {
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

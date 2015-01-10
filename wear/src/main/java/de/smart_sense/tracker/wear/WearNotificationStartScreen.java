package de.smart_sense.tracker.wear;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import de.smart_sense.tracker.libs.data_save.SensorDataSavingService;
import de.smart_sense.tracker.libs.logging.LoggingService;
import de.smart_sense.tracker.wear.logging.WearLoggingService;

import static de.smart_sense.tracker.libs.Util.isSensorDataSavingServiceRunning;
import static de.smart_sense.tracker.wear.WearUtil.isLoggingServiceRunning;

public class WearNotificationStartScreen extends Activity {

    private static final String TAG = WearNotificationStartScreen.class.getSimpleName();
    public static final String EXTRA_TITLE = "title";


    SharedPreferences sharedPrefs;
    WatchViewStub wvs;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_start_screen);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);


        /*
        Intent intent = getIntent();
        if (intent != null) {
            intent.getAction();
        }
        */

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                wvs = stub;
                TextView mTextView = (TextView) stub.findViewById(R.id.id_title);
                mTextView.setText("Smart Sense Tracker");

                uiUpdate();



            }

        });


        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 100); // 0,1 second
    }

    public void onResume(){
        super.onResume();

        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 100); // 0,1 second


        uiUpdate();
    }


    private void uiUpdate(){

        if(wvs == null) // TODO check that
            return;

        TextView mTextView = (TextView) wvs.findViewById(R.id.id_sensor_event);
        mTextView.setText("Sensor Event: " + Long.toString(sharedPrefs.getLong("sensor_events_logged", 0L) / 1000) + " k");

        mTextView = (TextView) wvs.findViewById(R.id.id_logging_running);
        if(isLoggingServiceRunning(this) && isSensorDataSavingServiceRunning(this)) {
            mTextView.setText("läuft.");
            mTextView.setTextColor(getResources().getColor(R.color.my_green));
        } else {
            mTextView.setText("angehalten.");
            mTextView.setTextColor(getResources().getColor(R.color.my_red));
        }

    }



    private int updateRequest = 0;
    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            // update Name
            uiUpdate();


            handler.postDelayed(this, 100); // 0,1 seconds
        }
    };

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
        super.onDestroy();
    }
}

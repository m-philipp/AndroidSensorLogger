package de.smart_sense.tracker.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import de.smart_sense.tracker.app.markdownViewer.AboutScreen;
import de.smart_sense.tracker.app.markdownViewer.HelpScreen;
import de.smart_sense.tracker.app.markdownViewer.IntroductionScreen;
import de.smart_sense.tracker.libs.StartActivity;
import de.smart_sense.tracker.libs.Util;
import de.smart_sense.tracker.libs.WearableMessageSenderService;
import de.smart_sense.tracker.libs.data_save.SensorDataSavingService;
import de.smart_sense.tracker.libs.data_zip_upload.ZipUploadService;

import static de.smart_sense.tracker.app.PhoneUtil.*;
import static de.smart_sense.tracker.libs.Util.isSensorDataSavingServiceRunning;

public class StartScreen extends StartActivity implements MyDialogFragment.NoticeDialogListener {


    private static final String TAG = "de.smart_sense.tracker.app.StartScreen";

    private AlarmManager alarmMgr;


    private static final int DIALOG_TOGGLE = 0;
    private static final int DIALOG_ANNOTATE = 1;

    private Context c = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate");

        checkIntent(getIntent());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);


        alarmManagerSetup();


        sharedPrefs.registerOnSharedPreferenceChangeListener(listener);


        if (sharedPrefs.getBoolean(Util.PREFERENCES_SENSOR_ACTIVATE, false))
            startBackgroundLogging(this, sharedPrefs);

        TextView t = (TextView) findViewById(R.id.amaount_of_data_to_upload);
        t.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onStartDebug();
                return true;
            }
        });



    }

    private void alarmManagerSetup() {
        Intent zipUploadAlarmIntent = new Intent(this, PhoneReceiver.class);
        zipUploadAlarmIntent.setAction(ZipUploadService.ACTION_START_SERVICE);
        PendingIntent zipUploadAlarmPendingIntent = PendingIntent.getBroadcast(this, 0, zipUploadAlarmIntent, 0);

        Intent periodicAlarmIntent = new Intent(this, PhoneReceiver.class);
        periodicAlarmIntent.setAction(Util.ACTION_PERIODIC_ALARM);
        PendingIntent periodicAlarmPendingIntent = PendingIntent.getBroadcast(this, 0, periodicAlarmIntent, 0);

        if(alarmMgr == null){
            Log.d(TAG, "AlarmManager was null");
            alarmMgr = (AlarmManager) this.getSystemService(this.ALARM_SERVICE);
        }
        else {
            Log.d(TAG, "AlarmManager was not null.");
        }

        alarmMgr.cancel(zipUploadAlarmPendingIntent);
        alarmMgr.cancel(periodicAlarmPendingIntent);

        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                1000,
                Util.ZIP_UPLOAD_SERVICE_FREQUENCY, zipUploadAlarmPendingIntent);

        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                1000,
                Util.PERIODIC_ALARM_FREQUENCY, periodicAlarmPendingIntent);

    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkIntent(intent);
    }

    private void checkIntent(Intent i){
        if(i != null &&
                i.getAction() != null &&
                i.getAction().equals(Util.ACTION_ANNOTATE)){

            //Toast.makeText(this, "Annotate from Intent", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "annotate ACTION");
            annotate(null);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        checkIntent(getIntent());

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
        } else if (id == R.id.action_help) {
            Intent intent = new Intent(this, HelpScreen.class);
            startActivity(intent);
        } else if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutScreen.class);
            startActivity(intent);
        } else if (id == R.id.action_introduction) {
            Intent intent = new Intent(this, IntroductionScreen.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }


    public void annotate() {

        Log.i(TAG, "annotate called");

        Intent sendIntent = new Intent(SensorDataSavingService.BROADCAST_ANNOTATION);
        sendIntent.putExtra(SensorDataSavingService.EXTRA_ANNOTATION_NAME, sharedPrefs.getString(Util.PREFERENCES_ANNOTATION_NAME, "smoking"));
        sendIntent.putExtra(SensorDataSavingService.EXTRA_ANNOTATION_VIA, "smartphone_ui");
        sendBroadcast(sendIntent);

    }



    public void annotate(View v) {
        Bundle b = new Bundle();
        b.putString(MyDialogFragment.ARG_MESSAGE, getString(R.string.dialog_sure_annotate));
        b.putString(MyDialogFragment.ARG_CONFIRM, getString(R.string.confirm_annotate));
        b.putString(MyDialogFragment.ARG_CANCEL, getString(R.string.cancel));
        b.putInt(MyDialogFragment.ARG_ID, DIALOG_ANNOTATE);

        MyDialogFragment mdf = new MyDialogFragment();
        mdf.setArguments(b);
        mdf.show(getFragmentManager(), TAG);
    }



    public void onDialogPositiveClick(int i){
        if(i == DIALOG_ANNOTATE)
            annotate();
        else if(i == DIALOG_TOGGLE)
            confirmedToggleClick();
    };



    public void triggerManualDataUpload(View v) {

        Intent mServiceIntent = new Intent(this, ZipUploadService.class);
        mServiceIntent.setAction(ZipUploadService.ACTION_MANUAL_UPLOAD_DATA);
        this.startService(mServiceIntent);


		/*
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(500);
		*/

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

                            startBackgroundLogging(c, sharedPrefs);

                        } else {

                            stopBackgroundLogging(c);

                        }
                    }
                    uiUpdate();

                    sendPreferencesToCompanion();


                }
            };

    @Override
    protected void uiUpdate() {
        TextView t = (TextView) findViewById(R.id.welcome_name);
        t.setText("Hi, " + sharedPrefs.getString("name", "Kunibert"));

        t = (TextView) findViewById(R.id.annotate_smoking);
        t.setText(getString(R.string.annotate) + " " + sharedPrefs.getString(Util.PREFERENCES_ANNOTATION_NAME, "smoking"));

        t = (TextView) findViewById(R.id.amaount_of_data_to_upload);
        t.setText(sharedPrefs.getString("amount_of_logged_data", "0.0") +  " MB");

        t = (TextView) findViewById(R.id.id_sensor_event);
        t.setText(Long.toString(sharedPrefs.getLong("sensor_events_logged", 0L) / 1000) + " k");
        // Log.d(TAG, "updating Sensor events to: " + sensorEventNo + " k");


        if (isLoggingServiceRunning(c) && isSensorDataSavingServiceRunning(c)) {
            ToggleButton tb = (ToggleButton) findViewById(R.id.toggleLogging);
            tb.setEnabled(false);
            tb.setChecked(true);

            Button b = (Button) findViewById(R.id.annotate_smoking);
            b.setEnabled(true);
        } else {
            ToggleButton tb = (ToggleButton) findViewById(R.id.toggleLogging);
            tb.setEnabled(false);
            tb.setChecked(false);

            Button b = (Button) findViewById(R.id.annotate_smoking);
            b.setEnabled(false);
        }

        t = (TextView) findViewById(R.id.num_uploaded);
        t.setText( sharedPrefs.getInt("uploadedData", 0) +  " Dateien");


    }



    public void onToggleClicked(View view) {
        Bundle b = new Bundle();
        b.putString(MyDialogFragment.ARG_MESSAGE, getString(R.string.dialog_sure_toggle));
        b.putString(MyDialogFragment.ARG_CONFIRM, getString(R.string.confirm_toggle));
        b.putString(MyDialogFragment.ARG_CANCEL, getString(R.string.cancel));
        b.putInt(MyDialogFragment.ARG_ID, DIALOG_TOGGLE);

        MyDialogFragment mdf = new MyDialogFragment();
        mdf.setArguments(b);
        mdf.show(getFragmentManager(), TAG);

    }
    public void confirmedToggleClick() {

        if (!sharedPrefs.getBoolean(Util.PREFERENCES_SENSOR_ACTIVATE, false)) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean(Util.PREFERENCES_SENSOR_ACTIVATE, true);
            editor.commit();
        } else {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putBoolean(Util.PREFERENCES_SENSOR_ACTIVATE, false);
            editor.commit();
        }
    }



    protected void sendPreferencesToCompanion() {

        Log.d(TAG, "starting Message Sender ...");

        Intent messageSenderIntent = new Intent(this, WearableMessageSenderService.class);
        messageSenderIntent.setAction(WearableMessageSenderService.ACTION_SEND_PREFERENCES);
        this.startService(messageSenderIntent);

    }





}

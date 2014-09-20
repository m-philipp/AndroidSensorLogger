package ess.imu_logger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.File;

import ess.imu_logger.data_zip_upload.ZipUploadService;

public class StartScreen extends Activity {


    SharedPreferences sharedPrefs;

	private static final String TAG = "ess.imu_logger.StartScreen";


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

	    updateSettingsOnStartScreen();

    }

	@Override
	protected void onResume() {
		super.onResume();
		updateSettingsOnStartScreen();
		Log.d(TAG, "resuming");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "pausing");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "destroying");
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

	public void onStartLiveScreen(View v){
        Intent intent = new Intent(this, ImuLiveScreen.class);
        startActivity(intent);
    }

	private void startBackgroundLogging(){
        Intent mServiceIntent = new Intent(this, LoggingService.class);
        mServiceIntent.setAction("ess.imu_logger.action.startLogging");
        this.startService(mServiceIntent);
    }

    public void stopBackgroundLogging() {
        Intent mServiceIntent = new Intent(this, LoggingService.class);
        mServiceIntent.setAction("ess.imu_logger.action.stopLogging");
        this.startService(mServiceIntent);
    }

	public void triggerManualDataUpload(View v){

		Intent mServiceIntent = new Intent(this, ZipUploadService.class);
		mServiceIntent.setAction(ZipUploadService.ACTION_MANUAL_UPLOAD_DATA);
		this.startService(mServiceIntent);

		/*
		((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(500);
		*/

	}

	public void annotateSmoking(View v){

		Log.i(TAG, "annotateSmoking called");

		Intent sendIntent = new Intent();
		sendIntent.setAction("ess.imu_logger.annotateSmoking");
		sendBroadcast(sendIntent);

	}

	SharedPreferences.OnSharedPreferenceChangeListener listener =
			new SharedPreferences.OnSharedPreferenceChangeListener() {
				public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
					Log.d(TAG, "----------------- PREFS CHANGED !! ------------");


					updateSettingsOnStartScreen();


				}
			};

	private void updateSettingsOnStartScreen() {
			// update Name
			TextView t = (TextView) findViewById(R.id.welcome_name);
			t.setText("Hi, " + sharedPrefs.getString("name", "Kunibert"));

			// update logging status
			t = (TextView) findViewById(R.id.logging_service_state);
			if( sharedPrefs.getBoolean("sensor_activate", false)){
				t.setText(getResources().getText(R.string.logging_service_running));
				t.setTextColor(getResources().getColor(R.color.my_green));
			}
			else{
				t.setText(getResources().getText(R.string.logging_service_stopped));
				t.setTextColor(getResources().getColor(R.color.my_red));
			}

			// start/stop the Logging Service
			if( sharedPrefs.getBoolean("sensor_activate", false)){
				startBackgroundLogging();
			}
			else{
				stopBackgroundLogging();
			}

			Long l = getFolderSize();
			Float f = Util.round( (l.floatValue() / (1024 * 1024)), 2);
			Log.d(TAG, "-------->> " + f.toString());
			t = (TextView) findViewById(R.id.amaount_of_data_to_upload);
			t.setText(f.toString() + " MB");

	}



	public static long getFolderSize(){
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


}

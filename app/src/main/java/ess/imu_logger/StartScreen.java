package ess.imu_logger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.SharedPreferences.Editor;
import android.widget.TextView;

import org.apache.http.protocol.HTTP;

import ess.imu_logger.data_export.SensorDataSavingService;

public class StartScreen extends Activity {


    SharedPreferences sharedPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

	    PreferenceManager.setDefaultValues(this, R.xml.preferences, false); // false ensures this is only executed once

	    //sharedPrefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
	    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	    sharedPrefs.registerOnSharedPreferenceChangeListener(listener);



	    Intent mServiceIntent = new Intent(this, SensorDataSavingService.class);
	    mServiceIntent.setAction(SensorDataSavingService.ACTION_START_SERVICE);
	    this.startService(mServiceIntent);

	    //Intent mServiceIntent = new Intent(this, SensorDataSavingService.class);
	    //this.startService(mServiceIntent);

	    // register broadcast receiver

    }

	@Override
	protected void onResume() {
		super.onResume();
		System.out.println("resuming");
	}

	@Override
	protected void onPause() {
		super.onPause();
		System.out.println("pausing");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.out.println("destroying");
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

    public void onStartBackgroundLogging(View v){

        Intent mServiceIntent = new Intent(this, LoggingService.class);

        mServiceIntent.setAction("ess.imu_logger.action.startLogging");

        this.startService(mServiceIntent);
    }

    public void onStopBackgroundLogging(View v) {
        Intent mServiceIntent = new Intent(this, LoggingService.class);

        mServiceIntent.setAction("ess.imu_logger.action.stopLogging");

        this.startService(mServiceIntent);
    }

	public void crunchSomeData(View v){
		((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(500);


		Intent sendIntent = new Intent();
		sendIntent.setAction(SensorDataSavingService.ACTION_SAVE_DATA);
		sendIntent.putExtra(SensorDataSavingService.EXTRA_SENSOR_DATA, "magic miracle\n");
		//sendIntent.setType(HTTP.PLAIN_TEXT_TYPE); // "text/plain" MIME type
		sendBroadcast(sendIntent);

		// Verify that the intent will resolve to an activity
		//if (sendIntent.resolveActivity(getPackageManager()) != null) {
		//	startActivity(sendIntent);
		//}

	}

	public void foobar(View v){
		Intent sendIntent = new Intent();
		sendIntent.setAction("ess.imu_logger.foobar");
		sendBroadcast(sendIntent);
	}

	SharedPreferences.OnSharedPreferenceChangeListener listener =
			new SharedPreferences.OnSharedPreferenceChangeListener() {
				public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
					System.out.println("----------------- PREFS CHANGED !! ------------");

					if(key.equals("name")){
						TextView t = (TextView) findViewById(R.id.textView);
						t.setText(sharedPrefs.getString("name", ""));
					}

				}
			};

}

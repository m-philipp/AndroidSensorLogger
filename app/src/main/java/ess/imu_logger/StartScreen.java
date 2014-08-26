package ess.imu_logger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.SharedPreferences.Editor;
import android.widget.TextView;

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

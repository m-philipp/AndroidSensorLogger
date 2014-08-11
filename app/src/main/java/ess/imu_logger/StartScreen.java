package ess.imu_logger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class StartScreen extends Activity {


    SharedPreferences sharedPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        sharedPrefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        if (!sharedPrefs.getBoolean("initialized", false)) {
            setDefaultPreferences();
        }
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
            return true;
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
        mServiceIntent.putExtra("ess.imu_logger.extra.GYRO", true);
        mServiceIntent.putExtra("ess.imu_logger.extra.ACC", true);
        mServiceIntent.putExtra("ess.imu_logger.extra.MAG", true);


        this.startService(mServiceIntent);
    }

    public void onStopBackgroundLogging(View v) {
        Intent mServiceIntent = new Intent(this, LoggingService.class);

        mServiceIntent.setAction("ess.imu_logger.action.stopLogging");

        this.startService(mServiceIntent);
    }

    private void setDefaultPreferences() {
        Editor editor = sharedPrefs.edit();

        // set default sensors
        editor.putBoolean("accelerometer", true);
        editor.putBoolean("ambientLight", true);
        editor.putBoolean("proximity", true);

        editor.commit();
    }
}

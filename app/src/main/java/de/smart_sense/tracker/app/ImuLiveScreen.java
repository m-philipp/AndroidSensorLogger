package de.smart_sense.tracker.app;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.LinkedList;

import de.smart_sense.tracker.app.markdownViewer.AboutScreen;
import de.smart_sense.tracker.app.markdownViewer.HelpScreen;
import de.smart_sense.tracker.app.markdownViewer.IntroductionScreen;
import de.smart_sense.tracker.app.markdownViewer.MarkdownViewerActivity;

public class ImuLiveScreen extends Activity implements SensorEventListener {
    private SensorManager mSensorManager;


    private LinkedList<Long> accTimestamps;
    private LinkedList<Long> gyroTimestamps;
    private LinkedList<Long> magTimestamps;
    private LinkedList<Long> proxTimestamps;
    private LinkedList<Long> lightTimestamps;

    public ImuLiveScreen() {

    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),
                SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                SensorManager.SENSOR_DELAY_GAME);

    }
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            drawSensor(event, (TextView) findViewById(R.id.acc_value), (TextView) findViewById(R.id.acc_hz), this.accTimestamps);

        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            drawSensor(event, (TextView) findViewById(R.id.gyro_value), (TextView) findViewById(R.id.gyro_hz), this.gyroTimestamps);

        }  else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

            drawSensor(event, (TextView) findViewById(R.id.mag_value), (TextView) findViewById(R.id.mag_hz), this.magTimestamps);

        } else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {

            drawSensor(event, (TextView) findViewById(R.id.prox_value), (TextView) findViewById(R.id.prox_hz), this.proxTimestamps);

        } else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {

            drawSensor(event, (TextView) findViewById(R.id.light_value), (TextView) findViewById(R.id.light_hz), this.lightTimestamps);

        }

    }

    private void drawSensor(SensorEvent event, TextView xyz, TextView hz, LinkedList<Long> timestamps) {
        if(timestamps == null)
            timestamps = new LinkedList<Long>();

        // System.nanoTime(); // time since last boot (use timemillies for UTC)
        if (timestamps.size() > 100) {
            timestamps.removeLast();
        }
        timestamps.push(event.timestamp);

        xyz.setText((float) Math.round(event.values[0] * 100) / 100 + " "
                + (float) Math.round(event.values[1] * 100) / 100 + " "
                + (float) Math.round(event.values[2] * 100) / 100 + " ");

        double fps = 0;
        if (timestamps.size() > 99) {
            fps = 100000000000D / (timestamps.getFirst() - timestamps.getLast());
        }
        hz.setText((float) Math.round(fps * 100) / 100 + " Hz");
    }

    // --------------------


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imu_live_screen);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        /*
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        */

        this.accTimestamps = new LinkedList<Long>();
        this.proxTimestamps = new LinkedList<Long>();
        this.lightTimestamps = new LinkedList<Long>();
        this.gyroTimestamps = new LinkedList<Long>();
        this.magTimestamps = new LinkedList<Long>();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.imu_live_screen, menu);
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
}

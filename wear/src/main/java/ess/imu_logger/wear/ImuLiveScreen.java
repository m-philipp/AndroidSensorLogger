package ess.imu_logger.wear;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

import java.util.LinkedList;

public class ImuLiveScreen extends Activity implements SensorEventListener {

	private LinkedList<Long> accTimestamps;
	private LinkedList<Long> gyroTimestamps;
	private LinkedList<Long> magTimestamps;
	private SensorManager mSensorManager;

	private WatchViewStub stub;


	public ImuLiveScreen() {

	}

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imu_live_screen);

		stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
		/*
		final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });

       */
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		this.accTimestamps = new LinkedList<Long>();
		this.gyroTimestamps = new LinkedList<Long>();
		this.magTimestamps = new LinkedList<Long>();
    }

	protected void onResume() {
		super.onResume();

		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
				SensorManager.SENSOR_DELAY_FASTEST);
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);
		mSensorManager.registerListener(this,
				mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_FASTEST);


	}
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void onSensorChanged(SensorEvent event) {

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

			drawSensor(event, (TextView)  stub.findViewById(R.id.acc_value), (TextView)  stub.findViewById(R.id.acc_hz), this.accTimestamps);

		} else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

			drawSensor(event, (TextView)  stub.findViewById(R.id.gyro_value), (TextView)  stub.findViewById(R.id.gyro_hz), this.gyroTimestamps);

		}  else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

			drawSensor(event, (TextView)  stub.findViewById(R.id.mag_value), (TextView)  stub.findViewById(R.id.mag_hz), this.magTimestamps);

		}

	}

	private void drawSensor(SensorEvent event, TextView xyz, TextView hz, LinkedList<Long> timestamps) {

		if(xyz == null || hz == null)
			return;

		// System.nanoTime(); // time since last boot (use timemillies for UTC)
		if (timestamps.size() > 100) {
			timestamps.removeLast();
		}
		timestamps.push(event.timestamp);

		xyz.setText((float) Math.round(event.values[0] * 100) / 100 + " "
				+ (float) Math.round(event.values[1] * 100) / 100 + " "
				+ (float) Math.round(event.values[2] * 100) / 100 + " ");

		double fps = 0;
		if (timestamps != null && timestamps.size() > 99) {
			fps = 100000000000D / (timestamps.getFirst() - timestamps.getLast());
		}
		hz.setText((float) Math.round(fps * 100) / 100 + " Hz");
	}
}

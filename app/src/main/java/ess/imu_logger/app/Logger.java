package ess.imu_logger.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.app.Service;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import java.util.ArrayList;

/**
 * Created by martin on 11.08.14.
 */
public class Logger extends Handler implements SensorEventListener{

    // ?? private static final String TAG = "BasicLogger";
	private static final int QUEUE_MAX = 3000;
    public static final int MESSAGE_START = 1;
    public static final int MESSAGE_STOP = 0;

    private SensorManager mSensorManager;
	private Sensor gyroscopeSensor, accelerometerSensor, magneticFieldSensor, rotationSensor, linearAccelerometerSensor, gravitySensor, ambientLightSensor, proximitySensor, temperatureSensor, humiditySensor, pressureSensor;
	private ArrayList<String> sensorValueQueue = new ArrayList<String>();
	private Integer sensorQueueLength = 0;
	private SharedPreferences sharedPrefs;

	private int logging_frequency; //SensorManager.SENSOR_DELAY_FASTEST;

    public Logger(Looper looper, Service context) {
        super(looper);

	    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);


	    logging_frequency = (int) Integer.parseInt(sharedPrefs.getString("sampling_rate", "0"));

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        gyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticFieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        rotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        linearAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        ambientLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        proximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        temperatureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        humiditySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        pressureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

    }


    @Override
    public void handleMessage(Message msg) {
        if (msg.what == MESSAGE_START) {
            System.out.println("Logger started");
            registerListeners(msg);
        }
        else if(msg.what == MESSAGE_STOP){
            this.removeMessages(0);
            mSensorManager.unregisterListener(this);
        }
    }

    private void registerListeners(Message msg){
        // evtl. check if getDefaultSensor() == null
        Intent i = (Intent) msg.obj;

        if(gyroscopeSensor != null && i.getBooleanExtra("gyroscope", false)) mSensorManager.registerListener(this, gyroscopeSensor, logging_frequency);
        if(accelerometerSensor != null && i.getBooleanExtra("accelerometer", false)) mSensorManager.registerListener(this, accelerometerSensor, logging_frequency);
        if(magneticFieldSensor != null && i.getBooleanExtra("magneticField", false)) mSensorManager.registerListener(this, magneticFieldSensor, logging_frequency);
        if(rotationSensor != null && i.getBooleanExtra("rotation", false)) mSensorManager.registerListener(this, rotationSensor, logging_frequency);
        if(linearAccelerometerSensor != null && i.getBooleanExtra("linearAccelerometer", false)) mSensorManager.registerListener(this, linearAccelerometerSensor, logging_frequency);
        if(gravitySensor != null && i.getBooleanExtra("gravity", false)) mSensorManager.registerListener(this, gravitySensor, logging_frequency);
        if(ambientLightSensor != null && i.getBooleanExtra("ambientLight", false)) mSensorManager.registerListener(this, ambientLightSensor, logging_frequency);
        if(proximitySensor != null && i.getBooleanExtra("proximity", false)) mSensorManager.registerListener(this, proximitySensor, logging_frequency);
        if(temperatureSensor != null && i.getBooleanExtra("temperature", false)) mSensorManager.registerListener(this, temperatureSensor, logging_frequency);
        if(humiditySensor != null && i.getBooleanExtra("humidity", false)) mSensorManager.registerListener(this, humiditySensor, logging_frequency);
        if(pressureSensor != null && i.getBooleanExtra("pressure", false)) mSensorManager.registerListener(this, pressureSensor, logging_frequency);



    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onSensorChanged(SensorEvent event) {

	    if(sensorQueueLength > QUEUE_MAX){

		    // Todo change this to some more intelligent code
		    // maybe handle a bunch of queues an get an Broadcast Notification when to drop a specific queue
		    // hashmap queues
		    // String actualQueue


		    new AsyncWriteFile().execute((ArrayList<String>) new ArrayList<String>(this.sensorValueQueue));
		    this.sensorQueueLength = 0;
		    this.sensorValueQueue = new ArrayList<String>();
	    }
	    this.sensorQueueLength++;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

	        this.sensorValueQueue.add(getString(event));
	        printSensor(event, "acc");

        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {

            // printSensor(event, "gyro");

        }  else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

            // printSensor(event, "mag");

        } else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {

            // printSensor(event, "prox");

        } else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {

            // printSensor(event, "light");

        }

    }

    private void printSensor(SensorEvent event, String sensorName) {
        // System.nanoTime(); // time since last boot (use timemillies for UTC)

        System.out.println(sensorName + " " + (float) Math.round(event.values[0] * 100) / 100 + " "
                + (float) Math.round(event.values[1] * 100) / 100 + " "
                + (float) Math.round(event.values[2] * 100) / 100 + " ");
    }


	private String getString(SensorEvent event) {

		StringBuilder dataString = new StringBuilder();

		dataString.append(System.currentTimeMillis());
		dataString.append(" ");
		dataString.append(SystemClock.elapsedRealtime());
		dataString.append(" ");
		dataString.append(event.timestamp);
		dataString.append(" ");

		switch (event.sensor.getType()){
			case Sensor.TYPE_ACCELEROMETER:
				dataString.append(Sensor.TYPE_ACCELEROMETER);
				dataString.append(" ");
				dataString.append(event.values[0]);
				dataString.append(" ");
				dataString.append(event.values[1]);
				dataString.append(" ");
				dataString.append(event.values[2]);
				dataString.append(" ");
				break;
			default:
				dataString.append("unrecognized Sensorevent ");
		}
		dataString.append("\n");

		return dataString.toString();


	}
}

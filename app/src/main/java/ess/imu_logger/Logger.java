package ess.imu_logger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.app.Service;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import ess.imu_logger.data_save.SensorDataSavingService;

/**
 * Created by martin on 11.08.14.
 */
public class Logger extends Handler implements SensorEventListener{

    // ?? private static final String TAG = "BasicLogger";
    public static final int MESSAGE_START = 1;
    public static final int MESSAGE_STOP = 0;

    private SensorManager mSensorManager;
	private Sensor gyroscopeSensor,stepCountSensor, accelerometerSensor, magneticFieldSensor, rotationSensor, linearAccelerometerSensor, gravitySensor, ambientLightSensor, proximitySensor, temperatureSensor, humiditySensor, pressureSensor;

	private SharedPreferences sharedPrefs;

	private static final String TAG = "ess.imu_logger.app.logger";

	private int logging_frequency; //SensorManager.SENSOR_DELAY_FASTEST;

	private Context context;


    public Logger(Looper looper, Service context) {
        super(looper);

	    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
	    this.context = context;


	    logging_frequency = (int) Integer.parseInt(sharedPrefs.getString("sampling_rate", "0"));


	    mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

	    accelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    gyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
	    magneticFieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        ambientLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        proximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        temperatureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        humiditySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        pressureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

	    rotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
	    gravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
	    linearAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
	    stepCountSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);





    }


    @Override
    public void handleMessage(Message msg) {
        if (msg.what == MESSAGE_START) {
	        Log.i(TAG, "Logger started");

	        Toast.makeText(context, "register Sensor Listener.", Toast.LENGTH_SHORT).show();

	        logging_frequency = (int) Integer.parseInt(sharedPrefs.getString("sampling_rate", "0"));
	        registerListeners(msg);
        }
        else if(msg.what == MESSAGE_STOP){
	        Log.i(TAG, "Logger stopped");
            this.removeMessages(0);

	        Toast.makeText(context, "unregister Sensor Listener.", Toast.LENGTH_SHORT).show();

	        mSensorManager.unregisterListener(this);



        }
    }

    private void registerListeners(Message msg){

	    Log.i(TAG, "registerListeners");

	    // evtl. check if getDefaultSensor() == null
        Intent i = (Intent) msg.obj;
	    logging_frequency = (int) Integer.parseInt(sharedPrefs.getString("sampling_rate", "0"));


	    if(accelerometerSensor != null && sharedPrefs.getBoolean("accelerometer", false))
		    mSensorManager.registerListener(this, accelerometerSensor, logging_frequency);
        if(gyroscopeSensor != null && sharedPrefs.getBoolean("gyroscope", false))
	        mSensorManager.registerListener(this, gyroscopeSensor, logging_frequency);
        if(magneticFieldSensor != null && sharedPrefs.getBoolean("magneticField", false))
	        mSensorManager.registerListener(this, magneticFieldSensor, logging_frequency);
	    if(ambientLightSensor != null && sharedPrefs.getBoolean("ambientLight", false))
		    mSensorManager.registerListener(this, ambientLightSensor, logging_frequency);
	    if(proximitySensor != null && sharedPrefs.getBoolean("proximity", false))
		    mSensorManager.registerListener(this, proximitySensor, logging_frequency);
	    if(temperatureSensor != null && sharedPrefs.getBoolean("temperature", false))
		    mSensorManager.registerListener(this, temperatureSensor, logging_frequency);
	    if(humiditySensor != null && sharedPrefs.getBoolean("humidity", false))
		    mSensorManager.registerListener(this, humiditySensor, logging_frequency);
	    if(pressureSensor != null && sharedPrefs.getBoolean("pressure", false))
		    mSensorManager.registerListener(this, pressureSensor, logging_frequency);

        if(rotationSensor != null && sharedPrefs.getBoolean("rotation", false))
	        mSensorManager.registerListener(this, rotationSensor, logging_frequency);
	    if(gravitySensor != null && sharedPrefs.getBoolean("gravity", false))
		    mSensorManager.registerListener(this, gravitySensor, logging_frequency);
	    if(linearAccelerometerSensor != null && sharedPrefs.getBoolean("linearAccelerometer", false))
		    mSensorManager.registerListener(this, linearAccelerometerSensor, logging_frequency);
	    if(stepCountSensor != null && sharedPrefs.getBoolean("steps", false))
		    mSensorManager.registerListener(this, stepCountSensor, logging_frequency);

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

	private int i = 0;

    public void onSensorChanged(SensorEvent event) {

	    i++;

	    if(event == null){
		    if( i % 500 == 0)
                Log.d(TAG, "SensorEvent without binding");
		        // Toast.makeText(context, "SensorEvent without binding.", Toast.LENGTH_SHORT).show();
		    return;

	    }

	    if( i % 500 == 0)
            Log.d(TAG, "sensorEvent " + i);
	        // Toast.makeText(context, "sensorEvent" + i, Toast.LENGTH_SHORT).show();

		//Log.i(TAG, getString(event));

        //mSaver.saveData(getString(event));

        Intent intent = new Intent(SensorDataSavingService.BROADCAST_SENSOR_DATA);
        // You can also include some extra data.
        intent.putExtra(SensorDataSavingService.EXTRA_SENSOR_DATA, getString(event));
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

    }



	private String getString(SensorEvent event) {

		StringBuilder dataString = new StringBuilder();

		dataString.append(System.currentTimeMillis());
		dataString.append(" ");
		dataString.append(SystemClock.elapsedRealtime());
		dataString.append(" ");
		dataString.append(event.timestamp);
		dataString.append(" ");

		// TODO Save Sensor Data....

		switch (event.sensor.getType()){
			case Sensor.TYPE_ACCELEROMETER:
				dataString.append(Sensor.TYPE_ACCELEROMETER);
				dataString.append(" ");
				dataString.append(event.values[0]);
				dataString.append(" ");
				dataString.append(event.values[1]);
				dataString.append(" ");
				dataString.append(event.values[2]);
				break;
			case Sensor.TYPE_GYROSCOPE:
				dataString.append(Sensor.TYPE_GYROSCOPE);
				dataString.append(" ");
				dataString.append(event.values[0]);
				dataString.append(" ");
				dataString.append(event.values[1]);
				dataString.append(" ");
				dataString.append(event.values[2]);
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				dataString.append(Sensor.TYPE_MAGNETIC_FIELD);
				dataString.append(" ");
				dataString.append(event.values[0]);
				dataString.append(" ");
				dataString.append(event.values[1]);
				dataString.append(" ");
				dataString.append(event.values[2]);
				break;
			case Sensor.TYPE_LIGHT:
				dataString.append(Sensor.TYPE_LIGHT);
				dataString.append(" ");
				dataString.append(event.values[0]);
				break;
			case Sensor.TYPE_PROXIMITY:
				dataString.append(Sensor.TYPE_PROXIMITY);
				dataString.append(" ");
				dataString.append(event.values[0]);
				break;
			case Sensor.TYPE_AMBIENT_TEMPERATURE:
				dataString.append(Sensor.TYPE_AMBIENT_TEMPERATURE);
				dataString.append(" ");
				dataString.append(event.values[0]);
				break;
			case Sensor.TYPE_RELATIVE_HUMIDITY:
				dataString.append(Sensor.TYPE_RELATIVE_HUMIDITY);
				dataString.append(" ");
				dataString.append(event.values[0]);
				break;
			case Sensor.TYPE_PRESSURE:
				dataString.append(Sensor.TYPE_PRESSURE);
				dataString.append(" ");
				dataString.append(event.values[0]);
				break;

			// Virtual Sensors
			case Sensor.TYPE_ROTATION_VECTOR:
				dataString.append(Sensor.TYPE_ROTATION_VECTOR);
				dataString.append(" ");
				dataString.append(event.values[0]);
				dataString.append(" ");
				dataString.append(event.values[1]);
				dataString.append(" ");
				dataString.append(event.values[2]);
				break;
			case Sensor.TYPE_GRAVITY:
				dataString.append(Sensor.TYPE_GRAVITY);
				dataString.append(" ");
				dataString.append(event.values[0]);
				dataString.append(" ");
				dataString.append(event.values[1]);
				dataString.append(" ");
				dataString.append(event.values[2]);
				break;
			case Sensor.TYPE_LINEAR_ACCELERATION:
				dataString.append(Sensor.TYPE_LINEAR_ACCELERATION);
				dataString.append(" ");
				dataString.append(event.values[0]);
				dataString.append(" ");
				dataString.append(event.values[1]);
				dataString.append(" ");
				dataString.append(event.values[2]);
				break;
			case Sensor.TYPE_STEP_COUNTER:
				dataString.append(Sensor.TYPE_STEP_COUNTER);
				dataString.append(" ");
				dataString.append(event.values[0]);
				break;
			default:
				dataString.append("unrecognized Sensorevent! Type: ");
				dataString.append(event.sensor.getType());
				dataString.append(" Name: ");
				dataString.append(event.sensor.getName());
		}
		dataString.append("\n");

		return dataString.toString();


	}


}

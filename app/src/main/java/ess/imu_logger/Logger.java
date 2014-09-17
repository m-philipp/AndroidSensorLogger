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
import android.util.Log;

import ess.imu_logger.data_save.SensorDataSavingService;
import ess.imu_logger.data_save.SensorDataSavingService.LocalBinder;

/**
 * Created by martin on 11.08.14.
 */
public class Logger extends Handler implements SensorEventListener{

    // ?? private static final String TAG = "BasicLogger";
	public static final int MESSAGE_START = 1;
    public static final int MESSAGE_STOP = 0;

    private SensorManager mSensorManager;
	private Sensor gyroscopeSensor, accelerometerSensor, magneticFieldSensor, rotationSensor, linearAccelerometerSensor, gravitySensor, ambientLightSensor, proximitySensor, temperatureSensor, humiditySensor, pressureSensor;

	private SharedPreferences sharedPrefs;

	private static final String TAG = "ess.imu_logger.app.logger";

	private int logging_frequency; //SensorManager.SENSOR_DELAY_FASTEST;

	private SensorDataSavingService mSaver;
	private boolean mBound = false;
	private Context context;


    public Logger(Looper looper, Service context) {
        super(looper);

	    sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
	    this.context = context;


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
	        Log.i(TAG, "Logger started");

	        // Bind to LocalService
	        Intent intent = new Intent(context, SensorDataSavingService.class);
	        this.context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

            registerListeners(msg);
        }
        else if(msg.what == MESSAGE_STOP){
	        Log.i(TAG, "Logger stopped");
            this.removeMessages(0);
            mSensorManager.unregisterListener(this);

	        // UnBind from LocalService
	        if (mBound) {
		        this.context.unbindService(mConnection);
		        mBound = false;
	        }

        }
    }

    private void registerListeners(Message msg){

	    Log.i(TAG, "registerListeners");

	    // evtl. check if getDefaultSensor() == null
        Intent i = (Intent) msg.obj;
	    logging_frequency = (int) Integer.parseInt(sharedPrefs.getString("sampling_rate", "0"));


        if(gyroscopeSensor != null && sharedPrefs.getBoolean("gyroscope", false))
	        mSensorManager.registerListener(this, gyroscopeSensor, logging_frequency);
        if(accelerometerSensor != null && sharedPrefs.getBoolean("accelerometer", false))
	        mSensorManager.registerListener(this, accelerometerSensor, logging_frequency);
        if(magneticFieldSensor != null && sharedPrefs.getBoolean("magneticField", false))
	        mSensorManager.registerListener(this, magneticFieldSensor, logging_frequency);
        if(rotationSensor != null && sharedPrefs.getBoolean("rotation", false))
	        mSensorManager.registerListener(this, rotationSensor, logging_frequency);
        if(linearAccelerometerSensor != null && sharedPrefs.getBoolean("linearAccelerometer", false))
	        mSensorManager.registerListener(this, linearAccelerometerSensor, logging_frequency);
        if(gravitySensor != null && sharedPrefs.getBoolean("gravity", false))
	        mSensorManager.registerListener(this, gravitySensor, logging_frequency);
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



    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onSensorChanged(SensorEvent event) {


	    if(event == null || !mBound) return;

		//Log.i(TAG, getString(event));

	    mSaver.saveData(getString(event));

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

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			LocalBinder binder = (LocalBinder) service;
			mSaver = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};
}

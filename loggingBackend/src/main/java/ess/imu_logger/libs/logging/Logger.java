package ess.imu_logger.libs.logging;

import android.app.Service;
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
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import ess.imu_logger.libs.StartActivity;
import ess.imu_logger.libs.Util;
import ess.imu_logger.libs.data_save.SensorDataSavingService;

/**
 * Created by martin on 11.08.14.
 */
public class Logger extends Handler implements SensorEventListener {

    // ?? private static final String TAG = "BasicLogger";
    public static final int MESSAGE_START = 1;
    public static final int MESSAGE_STOP = 0;

    private SensorManager mSensorManager;
    private Sensor gyroscopeSensor, stepCountSensor, accelerometerSensor, magneticFieldSensor, rotationSensor, linearAccelerometerSensor, gravitySensor, ambientLightSensor, proximitySensor, temperatureSensor, humiditySensor, pressureSensor;

    private SharedPreferences sharedPrefs;

    private static final String TAG = "ess.imu_logger.libs.logging.logger";

    private int logging_frequency; //SensorManager.SENSOR_DELAY_FASTEST;

    private Context context;


    public Logger(Looper looper, Service context) {
        super(looper);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;


        logging_frequency = Integer.parseInt(sharedPrefs.getString(Util.PREFERENCES_SAMPLING_RATE, "0"));


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

            //Toast.makeText(context, "register Sensor Listener.", Toast.LENGTH_SHORT).show();

            logging_frequency = Integer.parseInt(sharedPrefs.getString(Util.PREFERENCES_SAMPLING_RATE, "0"));
            registerListeners();
        } else if (msg.what == MESSAGE_STOP) {
            Log.i(TAG, "Logger stopped");
            this.removeMessages(0);

            //Toast.makeText(context, "unregister Sensor Listener.", Toast.LENGTH_SHORT).show();

            mSensorManager.unregisterListener(this);


        }
    }

    private void registerListeners() {

        Log.i(TAG, "registerListeners");

        // TODO evtl. check if getDefaultSensor() == null

        logging_frequency = Integer.parseInt(sharedPrefs.getString(Util.PREFERENCES_SAMPLING_RATE, "0"));

        int maxBatchReportLatency = 1000;

        if (accelerometerSensor != null && sharedPrefs.getBoolean(Util.PREFERENCES_ACCELEROMETER, false))
            mSensorManager.registerListener(this, accelerometerSensor, logging_frequency, maxBatchReportLatency, this);
        if (gyroscopeSensor != null && sharedPrefs.getBoolean(Util.PREFERENCES_GYROSCOPE, false))
            mSensorManager.registerListener(this, gyroscopeSensor, logging_frequency, maxBatchReportLatency, this);
        if (magneticFieldSensor != null && sharedPrefs.getBoolean(Util.PREFERENCES_MAGNETIC_FIELD, false))
            mSensorManager.registerListener(this, magneticFieldSensor, logging_frequency, maxBatchReportLatency, this);
        if (ambientLightSensor != null && sharedPrefs.getBoolean(Util.PREFERENCES_AMBIENT_LIGHT, false))
            mSensorManager.registerListener(this, ambientLightSensor, logging_frequency, maxBatchReportLatency, this);
        if (proximitySensor != null && sharedPrefs.getBoolean(Util.PREFERENCES_PROXIMITY, false))
            mSensorManager.registerListener(this, proximitySensor, logging_frequency, maxBatchReportLatency, this);
        if (temperatureSensor != null && sharedPrefs.getBoolean(Util.PREFERENCES_TEMPERATURE, false))
            mSensorManager.registerListener(this, temperatureSensor, logging_frequency, maxBatchReportLatency, this);
        if (humiditySensor != null && sharedPrefs.getBoolean(Util.PREFERENCES_HUMIDITY, false))
            mSensorManager.registerListener(this, humiditySensor, logging_frequency, maxBatchReportLatency, this);
        if (pressureSensor != null && sharedPrefs.getBoolean(Util.PREFERENCES_PRESSURE, false))
            mSensorManager.registerListener(this, pressureSensor, 30000, maxBatchReportLatency, this); // static freq to every 30 Sek cause of some Android Isssues

        if (rotationSensor != null && sharedPrefs.getBoolean(Util.PREFERENCES_ROTATION, false))
            mSensorManager.registerListener(this, rotationSensor, logging_frequency, maxBatchReportLatency, this);
        if (gravitySensor != null && sharedPrefs.getBoolean(Util.PREFERENCES_GRAVITY, false))
            mSensorManager.registerListener(this, gravitySensor, logging_frequency, maxBatchReportLatency, this);
        if (linearAccelerometerSensor != null && sharedPrefs.getBoolean(Util.PREFERENCES_LINEAR_ACCELEROMETER, false))
            mSensorManager.registerListener(this, linearAccelerometerSensor, logging_frequency, maxBatchReportLatency, this);
        if (stepCountSensor != null && sharedPrefs.getBoolean(Util.PREFERENCES_STEPS, false))
            mSensorManager.registerListener(this, stepCountSensor, logging_frequency, maxBatchReportLatency, this);

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private Long i = 0L;

    public void onSensorChanged(SensorEvent event) {

        if (event == null) {
            if (i % 2000 == 0)
                Log.d(TAG, "SensorEvent without binding");
            // Toast.makeText(context, "SensorEvent without binding.", Toast.LENGTH_SHORT).show();
            return;

        }

        if (i % 1000 == 0) {
            Log.d(TAG, "sensorEvent " + i);
            // Toast.makeText(context, "SensorEvent: " + i, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(StartActivity.BROADCAST_SENSOR_EVENT_NO);
            intent.putExtra(StartActivity.EXTRA_SENSOR_EVENT_NO, i);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        }

        i++;


        // TODO: THIS IS NOT BACKGROUNDING!! --> DONE registerListener gets Handler now!
        // SystemClock.sleep(200);

        Intent intent = new Intent(SensorDataSavingService.BROADCAST_SENSOR_DATA);
        intent.putExtra(SensorDataSavingService.EXTRA_SENSOR_DATA, getString(event));
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);


    }

    /* Saved String format:
     * System.currentTimeMillis() SystemClock.elapsedRealtime() event.timestamp Sensor.Type Sensor.Value0 Sensor.Value1 Sensor.Value2
     */

    public static String getString(SensorEvent event) {

        StringBuilder dataString = new StringBuilder();

        dataString.append(System.currentTimeMillis());
        dataString.append(" ");
        dataString.append(SystemClock.elapsedRealtime());
        dataString.append(" ");
        dataString.append(event.timestamp);
        dataString.append(" ");

        // TODO this Code is awful ...

        switch (event.sensor.getType()) {
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

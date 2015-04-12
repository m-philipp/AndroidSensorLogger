package de.smart_sense.tracker.libs.logging;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.HashMap;

import de.smart_sense.tracker.libs.StartActivity;
import de.smart_sense.tracker.libs.Util;
import de.smart_sense.tracker.libs.data_save.SensorDataSavingService;

/**
 * Created by martin on 11.08.14.
 */
public class Logger extends Thread implements SensorEventListener {

    // ?? private static final String TAG = "BasicLogger";
    public static final String MESSAGE_TYPE_ACTION = "de.smart_sense.tracker.libs.logging.logger.MESSAGE_TYPE_ACTION";
    public static final int MESSAGE_START = 0;
    public static final int MESSAGE_STOP = 1;

    private SensorManager mSensorManager;
    private Sensor gyroscopeSensor, stepCountSensor, accelerometerSensor, magneticFieldSensor, rotationSensor, linearAccelerometerSensor, gravitySensor, ambientLightSensor, proximitySensor, temperatureSensor, humiditySensor, pressureSensor;

    private SharedPreferences sharedPrefs;

    private static final String TAG = "de.smart_sense.tracker.libs.logging.logger";

    private int logging_frequency; //SensorManager.SENSOR_DELAY_FASTEST;

    private LoggingService context;
    private Logger logger;
    private Handler inHandler;

    public Logger(LoggingService context) {
        super();
        this.context = context;
        this.logger = this;
    }


    private Handler getHandler() {
        while (inHandler == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                //Ignore and try again.
            }
        }
        return inHandler;
    }

    // This method is allowed to be called from any thread
    public synchronized void requestStop() {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Thread loop quitting by request");
                Looper.myLooper().quit();
            }
        });
    }

    public void run() {
        try {
            Looper.prepare();
            synchronized (this) {
                inHandler = new Handler() {
                    public void handleMessage(Message msg) {
                        if (msg.getData().getInt(MESSAGE_TYPE_ACTION) == MESSAGE_START) {

                            Log.d(TAG, "Message Start received");

                            setSensors();
                            registerListeners();

                        } else if (msg.getData().getInt(MESSAGE_TYPE_ACTION) == MESSAGE_STOP) {

                            Log.d(TAG, "Message Stop received");

                            mSensorManager.unregisterListener(logger);
                            mSensorManager = null;
                            inHandler.removeMessages(0);
                            context.loggerStopped();
                            Looper.myLooper().quit();

                        }
                    }
                };
                notifyAll();
            }

            Looper.loop();

            Log.i(TAG, "Thread exiting gracefully");
        } catch (Throwable t) {
            Log.e(TAG, "Thread halted due to an error", t);
        }
    }

    public void setSensors() {

        this.mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        HashMap<String, Boolean> activeSensors = getSensors(mSensorManager);
        logging_frequency = Integer.parseInt(sharedPrefs.getString(Util.PREFERENCES_SAMPLING_RATE, "0"));


        if (sharedPrefs.getBoolean(Util.PREFERENCES_ACCELEROMETER, false) && activeSensors.get(Util.PREFERENCES_ACCELEROMETER))
            accelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sharedPrefs.getBoolean(Util.PREFERENCES_GYROSCOPE, false) && activeSensors.get(Util.PREFERENCES_GYROSCOPE))
            gyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (sharedPrefs.getBoolean(Util.PREFERENCES_MAGNETIC_FIELD, false) && activeSensors.get(Util.PREFERENCES_MAGNETIC_FIELD))
            magneticFieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (sharedPrefs.getBoolean(Util.PREFERENCES_AMBIENT_LIGHT, false) && activeSensors.get(Util.PREFERENCES_AMBIENT_LIGHT))
            ambientLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (sharedPrefs.getBoolean(Util.PREFERENCES_PROXIMITY, false) && activeSensors.get(Util.PREFERENCES_PROXIMITY))
            proximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (sharedPrefs.getBoolean(Util.PREFERENCES_TEMPERATURE, false) && activeSensors.get(Util.PREFERENCES_TEMPERATURE))
            temperatureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if (sharedPrefs.getBoolean(Util.PREFERENCES_HUMIDITY, false) && activeSensors.get(Util.PREFERENCES_HUMIDITY))
            humiditySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        if (sharedPrefs.getBoolean(Util.PREFERENCES_PRESSURE, false) && activeSensors.get(Util.PREFERENCES_PRESSURE))
            pressureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        if (sharedPrefs.getBoolean(Util.PREFERENCES_ROTATION, false) && activeSensors.get(Util.PREFERENCES_ROTATION))
            rotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (sharedPrefs.getBoolean(Util.PREFERENCES_GRAVITY, false) && activeSensors.get(Util.PREFERENCES_GRAVITY))
            gravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if (sharedPrefs.getBoolean(Util.PREFERENCES_LINEAR_ACCELEROMETER, false) && activeSensors.get(Util.PREFERENCES_LINEAR_ACCELEROMETER))
            linearAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if (sharedPrefs.getBoolean(Util.PREFERENCES_STEPS, false) && activeSensors.get(Util.PREFERENCES_STEPS))
            stepCountSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

    }


    public synchronized void startLogging() {

        Log.i(TAG, "startLogging called");

        Message msg = new Message();
        Bundle b = new Bundle();
        b.putInt(MESSAGE_TYPE_ACTION, MESSAGE_START);
        msg.setData(b);

        // could be a runnable when calling post instead of sendMessage
        getHandler().sendMessage(msg);

    }

    public synchronized void stopLogging() {
        Log.i(TAG, "stopLogging called");

        Message msg = new Message();
        Bundle b = new Bundle();
        b.putInt(MESSAGE_TYPE_ACTION, MESSAGE_STOP);
        msg.setData(b);

        // could be a runnable when calling post instead of sendMessage
        getHandler().removeMessages(0);
        getHandler().sendMessage(msg);
    }


    private void registerListeners() {

        Log.i(TAG, "registerListeners");

        HashMap<String, Boolean> activeSensors = getSensors(mSensorManager);
        logging_frequency = Integer.parseInt(sharedPrefs.getString(Util.PREFERENCES_SAMPLING_RATE, "2"));
        int maxBatchReportLatency = 1000;
        context.loggingStarted = true;

        Log.d(TAG, "logging_frequency: " + logging_frequency);


        if (accelerometerSensor != null &&
                activeSensors.get(Util.PREFERENCES_ACCELEROMETER) &&
                sharedPrefs.getBoolean(Util.PREFERENCES_ACCELEROMETER, false))
            mSensorManager.registerListener(this, accelerometerSensor, logging_frequency, maxBatchReportLatency, inHandler);

        if (gyroscopeSensor != null &&
                activeSensors.get(Util.PREFERENCES_GYROSCOPE) &&
                sharedPrefs.getBoolean(Util.PREFERENCES_GYROSCOPE, false))
            mSensorManager.registerListener(this, gyroscopeSensor, logging_frequency, maxBatchReportLatency, inHandler);

        if (magneticFieldSensor != null &&
                activeSensors.get(Util.PREFERENCES_MAGNETIC_FIELD) &&
                sharedPrefs.getBoolean(Util.PREFERENCES_MAGNETIC_FIELD, false))
            mSensorManager.registerListener(this, magneticFieldSensor, logging_frequency, maxBatchReportLatency, inHandler);

        if (ambientLightSensor != null &&
                activeSensors.get(Util.PREFERENCES_AMBIENT_LIGHT) &&
                sharedPrefs.getBoolean(Util.PREFERENCES_AMBIENT_LIGHT, false))
            mSensorManager.registerListener(this, ambientLightSensor, logging_frequency, maxBatchReportLatency, inHandler);

        if (proximitySensor != null &&
                activeSensors.get(Util.PREFERENCES_PROXIMITY) &&
                sharedPrefs.getBoolean(Util.PREFERENCES_PROXIMITY, false))
            mSensorManager.registerListener(this, proximitySensor, logging_frequency, maxBatchReportLatency, inHandler);

        if (temperatureSensor != null &&
                activeSensors.get(Util.PREFERENCES_TEMPERATURE) &&
                sharedPrefs.getBoolean(Util.PREFERENCES_TEMPERATURE, false))
            mSensorManager.registerListener(this, temperatureSensor, logging_frequency, maxBatchReportLatency, inHandler);

        if (humiditySensor != null &&
                activeSensors.get(Util.PREFERENCES_HUMIDITY) &&
                sharedPrefs.getBoolean(Util.PREFERENCES_HUMIDITY, false))
            mSensorManager.registerListener(this, humiditySensor, logging_frequency, maxBatchReportLatency, inHandler);

        if (pressureSensor != null &&
                activeSensors.get(Util.PREFERENCES_PRESSURE) &&
                sharedPrefs.getBoolean(Util.PREFERENCES_PRESSURE, false))
            mSensorManager.registerListener(this, pressureSensor, 30000, maxBatchReportLatency, inHandler); // static freq to every 30 Sek cause of some Android Isssues


        if (rotationSensor != null &&
                activeSensors.get(Util.PREFERENCES_ROTATION) &&
                sharedPrefs.getBoolean(Util.PREFERENCES_ROTATION, false))
            mSensorManager.registerListener(this, rotationSensor, logging_frequency, maxBatchReportLatency, inHandler);

        if (gravitySensor != null &&
                activeSensors.get(Util.PREFERENCES_GRAVITY) &&
                sharedPrefs.getBoolean(Util.PREFERENCES_GRAVITY, false))
            mSensorManager.registerListener(this, gravitySensor, logging_frequency, maxBatchReportLatency, inHandler);

        if (linearAccelerometerSensor != null &&
                activeSensors.get(Util.PREFERENCES_LINEAR_ACCELEROMETER) &&
                sharedPrefs.getBoolean(Util.PREFERENCES_LINEAR_ACCELEROMETER, false))
            mSensorManager.registerListener(this, linearAccelerometerSensor, logging_frequency, maxBatchReportLatency, inHandler);

        if (stepCountSensor != null &&
                activeSensors.get(Util.PREFERENCES_STEPS) &&
                sharedPrefs.getBoolean(Util.PREFERENCES_STEPS, false))
            mSensorManager.registerListener(this, stepCountSensor, logging_frequency, maxBatchReportLatency, inHandler);

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private Long i = 0L;

    public void onSensorChanged(SensorEvent event) {

        if (event == null) {
            return;
        }


        if (i % 1000 == 0) {
            Log.d(TAG, "sensorEvent " + i);
            // Toast.makeText(context, "SensorEvent: " + i, Toast.LENGTH_SHORT).show();
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putLong("sensor_events_logged", i);
            editor.commit();
        }

        i++;


        //Log.v(TAG, "Sensor: " + event.sensor.getName());
        //Log.v(TAG, getString(event));

        if (SensorDataSavingService.sensorEvents.size() > SensorDataSavingService.QUEUE_MAX)
            return;

        SensorDataSavingService.sensorEvents.add(getString(event));

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
                dataString.append(" ");
                dataString.append(event.values[3]);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    dataString.append(" ");
                    dataString.append(event.values[4]);
                }
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


    private HashMap<String, Boolean> getSensors(SensorManager mSensorManager) {

        HashMap<String, Boolean> foundSensors = new HashMap<String, Boolean>();

        Sensor s = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (s == null)
            foundSensors.put(Util.PREFERENCES_ACCELEROMETER, false);
        else {
            foundSensors.put(Util.PREFERENCES_ACCELEROMETER, true);
        }

        s = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (s == null)
            foundSensors.put(Util.PREFERENCES_GYROSCOPE, false);
        else {
            foundSensors.put(Util.PREFERENCES_GYROSCOPE, true);
        }

        s = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (s == null)
            foundSensors.put(Util.PREFERENCES_MAGNETIC_FIELD, false);
        else {
            foundSensors.put(Util.PREFERENCES_MAGNETIC_FIELD, true);
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (s == null)
            foundSensors.put(Util.PREFERENCES_AMBIENT_LIGHT, false);
        else{
            foundSensors.put(Util.PREFERENCES_AMBIENT_LIGHT, true);
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (s == null)
            foundSensors.put(Util.PREFERENCES_PROXIMITY, false);
        else{
            foundSensors.put(Util.PREFERENCES_PROXIMITY, true);
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        if (s == null)
            foundSensors.put(Util.PREFERENCES_TEMPERATURE, false);
        else{
            foundSensors.put(Util.PREFERENCES_TEMPERATURE, true);
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        if (s == null)
            foundSensors.put(Util.PREFERENCES_HUMIDITY, false);
        else{
            foundSensors.put(Util.PREFERENCES_HUMIDITY, true);
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (s == null)
            foundSensors.put(Util.PREFERENCES_PRESSURE, false);
        else{
            foundSensors.put(Util.PREFERENCES_PRESSURE, true);
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (s == null)
            foundSensors.put(Util.PREFERENCES_ROTATION, false);
        else{
            foundSensors.put(Util.PREFERENCES_ROTATION, true);
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if (s == null)
            foundSensors.put(Util.PREFERENCES_GRAVITY, false);
        else{
            foundSensors.put(Util.PREFERENCES_GRAVITY, true);
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if (s == null)
            foundSensors.put(Util.PREFERENCES_LINEAR_ACCELEROMETER, false);
        else{
            foundSensors.put(Util.PREFERENCES_LINEAR_ACCELEROMETER, true);
        }
        s = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (s == null)
            foundSensors.put(Util.PREFERENCES_STEPS, false);
        else
        {
            foundSensors.put(Util.PREFERENCES_STEPS, true);
        }

        return foundSensors;
    }
}

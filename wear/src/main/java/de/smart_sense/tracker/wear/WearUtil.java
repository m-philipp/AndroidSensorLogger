package de.smart_sense.tracker.wear;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import de.smart_sense.tracker.libs.Util;
import de.smart_sense.tracker.libs.data_save.SensorDataSavingService;
import de.smart_sense.tracker.libs.logging.LoggingService;
import de.smart_sense.tracker.wear.logging.WearLoggingService;

import static de.smart_sense.tracker.libs.Util.isServiceRunning;

/**
 * Created by martin on 09.01.2015.
 */
public class WearUtil {

    private static final String TAG = "de.smart_sense.tracker.wear.WearUtil";



    public static void updateLoggingState(Context context , SharedPreferences sharedPrefs) {

        Log.d(TAG, "update Logging State");

        // is the smartphone logging?
        if (sharedPrefs.getBoolean(Util.PREFERENCES_SENSOR_ACTIVATE, false)) {
            Long lastAnnotation = Long.parseLong(sharedPrefs.getString(Util.PREFERENCES_LAST_ANNOTATION, "0"));
            Long loggingDuration =  Long.parseLong(sharedPrefs.getString(Util.PREFERENCES_WEAR_TEMP_LOGGING_DURATION, "0"));

            // currently in temp_logging timespan?
            if (System.currentTimeMillis() - lastAnnotation < loggingDuration * 1000) {

                startBackgroundLogging(context);
                return;

            }
            // temp logging off?
            else if (!sharedPrefs.getBoolean(Util.PREFERENCES_WEAR_TEMP_LOGGING, false)) {

                startBackgroundLogging(context);
                return;

            }

        }

        stopBackgroundLogging(context);

    }


    public static void startBackgroundLogging(Context context) {

        Log.d(TAG, "starting Background Logging ...");

        Intent loggingServiceIntent = new Intent(context, WearLoggingService.class);
        loggingServiceIntent.setAction(LoggingService.ACTION_START_LOGGING);
        context.startService(loggingServiceIntent);

        Intent sensorDataSavingServiceIntent = new Intent(context, SensorDataSavingService.class);
        sensorDataSavingServiceIntent.setAction(SensorDataSavingService.ACTION_START_SERVICE);
        context.startService(sensorDataSavingServiceIntent);

    }

    public static void stopBackgroundLogging(Context context) {

        Log.d(TAG, "stopping Background Logging ...");

        Intent loggingServiceIntent = new Intent(context, WearLoggingService.class);
        loggingServiceIntent.setAction(LoggingService.ACTION_STOP_LOGGING);
        context.startService(loggingServiceIntent);

        Intent sensorDataSavingServiceIntent = new Intent(context, SensorDataSavingService.class);
        sensorDataSavingServiceIntent.setAction(SensorDataSavingService.ACTION_STOP_SERVICE);
        context.startService(sensorDataSavingServiceIntent);

    }

    public static boolean isLoggingServiceRunning(Context context) {
        return isServiceRunning(context, WearLoggingService.class.getName());
    }
}

package ess.imu_logger.wear;

/**
 * Created by martin on 09.09.2014.
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import ess.imu_logger.libs.Util;
import ess.imu_logger.libs.data_save.SensorDataSavingService;
import ess.imu_logger.libs.data_zip_upload.ZipUploadService;
import ess.imu_logger.libs.logging.LoggingService;


/**
 * Listens for a message telling it to start the Wearable MainActivity.
 */
public class WearableMessageListenerService extends WearableListenerService {


    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    private static final String TAG = "ess.imu_logger.wear.WearableMessageListenerService";


    @Override
    public void onMessageReceived(MessageEvent event) {

        if (event.getPath().equals(Util.GAC_PATH_TEST_ACTIVITY)) {
            Toast.makeText(this, "Hello from Phone!", Toast.LENGTH_LONG).show();

        } else if (event.getPath().equals(Util.GAC_PATH_ANNOTATE_SMOKING_ACTIVITY)) {

            Intent startIntent = new Intent(this, AnnotateSmoking.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);

        } else if (event.getPath().equals(Util.GAC_PATH_START_LOGGING)) {

            startBackgroundLogging();

        } else if (event.getPath().equals(Util.GAC_PATH_STOP_LOGGING)) {

            stopBackgroundLogging();

        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d(TAG, "DataItem deleted: " + event.getDataItem().getUri());
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                Log.d(TAG, "DataItem changed: " + event.getDataItem().getUri());
            }

        }
    }

    // TODO get preferences

    private void startBackgroundLogging() {



        Intent intent = new Intent(this, myReceiver.class);
        intent.setAction(ZipUploadService.ACTION_START_SERVICE);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        if(alarmMgr == null){
            Log.d(TAG, "AlarmManager was null");
            alarmMgr = (AlarmManager) this.getSystemService(this.ALARM_SERVICE);
        }
        else {
            Log.d(TAG, "AlarmManager was not null. Canceling alarmIntent");

            alarmMgr.cancel(alarmIntent);
        }


        alarmMgr.cancel(alarmIntent);

        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                1000,
                10000, alarmIntent); // TODO make Values static finals


        Intent loggingServiceIntent = new Intent(this, LoggingService.class);
        loggingServiceIntent.setAction(LoggingService.ACTION_START_LOGGING);
        this.startService(loggingServiceIntent);

        Intent sensorDataSavingServiceIntent = new Intent(this, SensorDataSavingService.class);
        sensorDataSavingServiceIntent.setAction(SensorDataSavingService.ACTION_START_SERVICE);
        this.startService(sensorDataSavingServiceIntent);
    }

    public void stopBackgroundLogging() {
        Intent loggingServiceIntent = new Intent(this, LoggingService.class);
        loggingServiceIntent.setAction(LoggingService.ACTION_STOP_LOGGING);
        this.startService(loggingServiceIntent);

        Intent sensorDataSavingServiceIntent = new Intent(this, SensorDataSavingService.class);
        sensorDataSavingServiceIntent.setAction(SensorDataSavingService.ACTION_STOP_SERVICE);
        this.startService(sensorDataSavingServiceIntent);
    }

}

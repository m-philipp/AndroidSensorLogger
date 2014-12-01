package ess.imu_logger.libs.data_save;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;

import ess.imu_logger.libs.Util;

public class SensorDataSavingService extends Service {


    public static final String ACTION_SAVE_DATA = "ess.imu_logger.libs.data_save.action.saveData";
    public static final String ACTION_START_SERVICE = "ess.imu_logger.libs.data_save.action.startLogging";
    public static final String ACTION_STOP_SERVICE = "ess.imu_logger.libs.data_save.action.stopLogging";


    public static final String BROADCAST_ANNOTATION = "ess.imu_logger.libs.data_save.annotate";
    public static final String BROADCAST_BLE_RSSI = "ess.imu_logger.libs.data_save.bleRssi";
    public static final String BROADCAST_SENSOR_DATA = "ess.imu_logger.libs.data_save.sensorData";


    public static final String EXTRA_SENSOR_DATA = "ess.imu_logger.libs.data_save.extra.sensorData";
    public static final String EXTRA_BLE_RSSI = "ess.imu_logger.libs.data_save.extra.bleRssi";
    public static final String EXTRA_BLE_DEVICE_NAME = "ess.imu_logger.libs.data_save.extra.bleDeviceName";
    public static final String EXTRA_BLE_DEVICE_ADDRESS = "ess.imu_logger.libs.data_save.extra.bleDeviceAddress";
    public static final String EXTRA_ANNOTATION_NAME = "ess.imu_logger.libs.data_save.extra.annotationName";
    public static final String EXTRA_ANNOTATION_VIA = "ess.imu_logger.libs.data_save.extra.annotationVia";

    private static final String TAG = "ess.imu_logger.libs.data_save.SensorDataSavingService";
    public static ConcurrentLinkedQueue<String> sensorEvents = new ConcurrentLinkedQueue<String>();

    private PlainFileWriter plainFileWriter;
    private boolean pfwRunning = false;


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.i(TAG, "Broadcast Receiver received a Broadcast");

            // TODO check is the extra is really there


            // System.currentTimeMillis() SystemClock.elapsedRealtime() event.timestamp Sensor.Type Sensor.Value0 Sensor.Value1 Sensor.Value2

            if (intent != null) {
                if (action.equals(BROADCAST_SENSOR_DATA)) {

                    //SystemClock.sleep(200); // TODO FIX THIS! DOne ?

                    saveData(intent);


                } else if (action.equals(BROADCAST_ANNOTATION)) {
                    Toast.makeText(context, "Annotation erhalten", Toast.LENGTH_SHORT).show();

                    String dataString = Util.formatLogString("Annotation",
                            intent.getExtras().getString(EXTRA_ANNOTATION_NAME), intent.getExtras().getString(EXTRA_ANNOTATION_VIA));

                    plainFileWriter.saveString(dataString);

                } else if (action.equals(BROADCAST_BLE_RSSI)) {
                    Log.d(TAG, "received BLE RSSI Broadcast. With RSSI: " + intent.getExtras().getInt(EXTRA_BLE_RSSI));

                    String dataString = Util.formatLogString("BLE_RSSI",
                            intent.getExtras().getString(EXTRA_BLE_DEVICE_NAME),
                            intent.getExtras().getString(EXTRA_BLE_DEVICE_ADDRESS),
                            String.valueOf(intent.getExtras().getInt(EXTRA_BLE_RSSI)));

                    plainFileWriter.saveString(dataString);

                } else if (action.equals(Util.ILITIT_ANNOTATE)) {
                    Toast.makeText(context, "Annotation erhalten", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "received iLitit Annotation Broadcast.");

                    Long timestamp = getTimestamp(intent);
                    Double latitude = intent.getExtras().getDouble(Util.ILITIT_EXTRA_LAT, 0.0);
                    Double longitude = intent.getExtras().getDouble(Util.ILITIT_EXTRA_LON, 0.0);
                    String via = intent.getExtras().getString(Util.ILITIT_EXTRA_VIA, "");

                    String dataString = "";
                    if(via.equals("lighter"))
                        dataString = Util.formatLogString(timestamp, "Annotation", "lighter", String.valueOf(latitude), String.valueOf(longitude));
                    else
                        dataString = Util.formatLogString(timestamp, "Annotation", "ui", String.valueOf(latitude), String.valueOf(longitude));


                    plainFileWriter.saveString(dataString);

                } else if (action.equals(Util.ILITIT_ANNOTATE_REMOVE)) {
                    Toast.makeText(context, "Annotation erhalten", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "received iLitit Annotation Broadcast.");

                    Long timestamp = getTimestamp(intent);
                    String dataString = Util.formatLogString(timestamp, "Annotation", "remove");

                    plainFileWriter.saveString(dataString);

                }
            }
        }
    };

    private Long getTimestamp(Intent intent) {
        Long timestamp = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            Date d = sdf.parse(intent.getExtras().getString(Util.ILITIT_EXTRA_TIMESTAMP, "1970-01-01 00:00:00.000+0000"));
            timestamp = d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public int onStartCommand(Intent intent, int flags, int startId) {

        // TODO check pfw lifecycle

        Log.i(TAG, "onStartCommand called.");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SAVE_DATA.equals(action)) {
                Log.d(TAG, "ACTION_SAVE_DATA");
                saveData(intent);
                // send message to the handler with the current message handler

            } else if (ACTION_START_SERVICE.equals(action)) {
                Log.d(TAG, "Called onStartCommand. Given Action: " + intent.getAction());
                if (!pfwRunning && !plainFileWriter.isAlive()) {
                    plainFileWriter.start();
                    pfwRunning = true;
                    plainFileWriter.startPolling();
                }
            } else if (ACTION_STOP_SERVICE.equals(action)) {
                Log.d(TAG, "Called onStartCommand. Given Action: " + intent.getAction());

                plainFileWriter.requestStop();
                pfwRunning = false;
                stopSelf();
            }
        }
        return START_STICKY;
    }

    public void onCreate() {
        Log.d(TAG, "SensorDataSavingService onCreate ...");


        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_SENSOR_DATA);
        filter.addAction(BROADCAST_ANNOTATION);
        filter.addAction(BROADCAST_BLE_RSSI);

        filter.addAction(Util.ILITIT_ANNOTATE);
        filter.addAction(Util.ILITIT_ANNOTATE_REMOVE);

        registerReceiver(receiver, filter);

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        plainFileWriter = new PlainFileWriter();


    }

    public void onDestroy() {
        // plainFileWriter.requestStop();
        unregisterReceiver(receiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

    }


    private void saveData(Intent intent) {
        //Log.v(TAG, "Saving Data: " + intent.getExtras().getString(EXTRA_SENSOR_DATA));
        plainFileWriter.saveString(intent.getExtras().getString(EXTRA_SENSOR_DATA));
    }

    public void saveData(String save) {
        //Log.v(TAG, "Saving Data: " + save);
        plainFileWriter.saveString(save);
    }


}

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

public class SensorDataSavingService extends Service {


    public static final String ACTION_SAVE_DATA = "ess.imu_logger.libs.data_save.action.saveData";
    public static final String ACTION_START_SERVICE = "ess.imu_logger.libs.data_save.action.startLogging";
    public static final String ACTION_STOP_SERVICE = "ess.imu_logger.libs.data_save.action.stopLogging";


    public static final String BROADCAST_LIGHTER = "ess.imu_logger.libs.data_save.annotateLighter";
    public static final String BROADCAST_ANNOTATION = "ess.imu_logger.libs.data_save.annotateSmoking";
    public static final String BROADCAST_BLE_RSSI = "ess.imu_logger.libs.data_save.bleRssi";
    public static final String BROADCAST_SENSOR_DATA = "ess.imu_logger.libs.data_save.sensorData";


    public static final String EXTRA_SENSOR_DATA = "ess.imu_logger.libs.data_save.extra.sensorData";
    public static final String EXTRA_BLE_RSSI = "ess.imu_logger.libs.data_save.extra.bleRssi";
    public static final String EXTRA_BLE_DEVICE_NAME = "ess.imu_logger.libs.data_save.extra.bleDeviceName";
    public static final String EXTRA_ANNOTATION_NAME = "ess.imu_logger.libs.data_save.extra.annotationName";

    private static final String TAG = "ess.imu_logger.libs.data_save.SensorDataSavingService";

    private PlainFileWriter plainFileWriter;
    private boolean pfwRunning = false;



	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			//Log.i(TAG, "Broadcast Receiver received a Broadcast");

			// TODO check is the extra is really there


            // System.currentTimeMillis() SystemClock.elapsedRealtime() event.timestamp Sensor.Type Sensor.Value0 Sensor.Value1 Sensor.Value2

			if (intent != null) {
				if (action.equals(BROADCAST_SENSOR_DATA)) {



                     /*
                        * send (Local ?) broadcasts on SensorDataChanged
                        * start Service from StartScreen
                        * ...
                     */
                    // Log.d(TAG, "save SensorData from Broadcast");
					saveData(intent);



				} else if (action.equals(BROADCAST_ANNOTATION)) {
					Toast.makeText(context, "Raucher Annotation erhalten", Toast.LENGTH_SHORT).show();

					StringBuilder dataString = new StringBuilder();
                    dataString.append(System.currentTimeMillis());
					dataString.append(" ");
					dataString.append(SystemClock.elapsedRealtime());
					dataString.append(" 0 Annotation ");
                    dataString.append(intent.getExtras().getString(EXTRA_ANNOTATION_NAME));
					dataString.append("\n");

					plainFileWriter.saveString(dataString.toString());

				} else if (action.equals(BROADCAST_LIGHTER)) {
                    Toast.makeText(context, "Feuerzeug Annotation erhalten", Toast.LENGTH_SHORT).show();

                    StringBuilder dataString = new StringBuilder();
                    dataString.append(System.currentTimeMillis());
                    dataString.append(" ");
                    dataString.append(SystemClock.elapsedRealtime());
                    dataString.append(" 0 Annotation ");
                    dataString.append(BROADCAST_LIGHTER);
                    dataString.append("\n");

                    plainFileWriter.saveString(dataString.toString());

                } else if (action.equals(BROADCAST_BLE_RSSI)) {
                    //Toast.makeText(context, "BLE RSSI Annotation erhalten", Toast.LENGTH_SHORT).show();

                    StringBuilder dataString = new StringBuilder();
                    dataString.append(System.currentTimeMillis());
                    dataString.append(" ");
                    dataString.append(SystemClock.elapsedRealtime());
                    dataString.append(" 0 Annotation ");
                    dataString.append(BROADCAST_BLE_RSSI);
                    dataString.append(" ");
                    dataString.append(intent.getExtras().getString(EXTRA_BLE_DEVICE_NAME));
                    dataString.append(" ");
                    dataString.append(intent.getExtras().getString(EXTRA_BLE_RSSI));
                    dataString.append("\n");


                    plainFileWriter.saveString(dataString.toString());

                }
			}
		}
	};


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand called.");
		if (intent != null) {
			final String action = intent.getAction();
			if (ACTION_SAVE_DATA.equals(action)) {
				Log.d(TAG, "ACTION_SAVE_DATA");
				saveData(intent);
				// send message to the handler with the current message handler

			} else if (ACTION_START_SERVICE.equals(action)) {
                Log.d(TAG, "Called onStartCommand. Given Action: " + intent.getAction());
                if(!pfwRunning && !plainFileWriter.isAlive()) {
                    plainFileWriter.start();
                    pfwRunning = true;
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
        filter.addAction(BROADCAST_LIGHTER);

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

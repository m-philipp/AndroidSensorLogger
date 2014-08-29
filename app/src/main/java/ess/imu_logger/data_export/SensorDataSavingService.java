package ess.imu_logger.data_export;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.preference.PreferenceManager;

import android.os.Handler;
import android.util.Log;

public class SensorDataSavingService extends Service {


	public static final String ACTION_SAVE_DATA = "ess.imu_logger.data_export.action.saveData";
	public static final String ACTION_UPLOAD_DATA = "ess.imu_logger.data_export.action.uploadData";
	public static final String ACTION_COMPRESS_DATA = "ess.imu_logger.data_export.action.compressData";
	public static final String ACTION_START_SERVICE = "ess.imu_logger.data_export.action.startLogging";

	public static final String EXTRA_SENSOR_DATA = "ess.imu_logger.data_export.extra.sensorData";


	private SharedPreferences sharedPrefs;
	private PlainFileWriter background;

	Handler inHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			if(msg.getData().getString("action").equals("save finished")){
				Log.i("SensorDataSavingService", "finished some Data saving");
			} else if(msg.getData().getString("action").equals("upload finished")){
				Log.i("SensorDataSavingService", "finished some Data Upload");
			}
			//txt.setText(txt.getText() + "Item " + key +System.getProperty("line.separator"));
		}
	};



	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (intent != null) {
				if (action.equals(ACTION_SAVE_DATA)) {
					saveData(intent);
				} else if (action.equals(ACTION_UPLOAD_DATA)) {
					uploadData();
				} else if (action.equals(ACTION_COMPRESS_DATA)) {
					compressData();
				}
			}
		}
	};


	public SensorDataSavingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

	public int onStartCommand (Intent intent, int flags, int startId){
		System.out.println("onStartCommand called in SensorDataSavingService ...");
		if (intent != null) {
			final String action = intent.getAction();
			if (ACTION_SAVE_DATA.equals(action)) {
				Log.d("INFO","ACTION_SAVE_DATA");
				saveData(intent);
				// send message to the handler with the current message handler

			} else if (ACTION_UPLOAD_DATA.equals(action)) {
				Log.d("INFO","ACTION_UPLOAD_DATA");
				uploadData();
			} else if (ACTION_COMPRESS_DATA.equals(action)) {
				Log.d("INFO","ACTION_COMPRESS_DATA");
				compressData();
			} else if (ACTION_START_SERVICE.equals(action)) {
				System.out.println("Called onStartCommand. Given Action: " + intent.getAction());
			}
		}
		return START_STICKY;
	}

	public void onCreate() {
		System.out.println("SensorDataSavingService created...");

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		// register broadcast receiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_SAVE_DATA);
		filter.addAction(ACTION_UPLOAD_DATA);
		filter.addAction(ACTION_COMPRESS_DATA);

		registerReceiver(receiver, filter);

		background = new PlainFileWriter(inHandler);
		background.start();

	}



	private void saveData(Intent intent) {
		System.out.println("Saving Data: " + intent.getExtras().getString(EXTRA_SENSOR_DATA));
		background.saveString(intent.getExtras().getString(EXTRA_SENSOR_DATA));
	}

	private void uploadData() {
	}

	private void compressData() {
	}


}

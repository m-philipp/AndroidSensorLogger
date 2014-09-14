package ess.imu_logger.app.data_export;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;

import android.os.Handler;
import android.util.Log;

public class SensorDataSavingService extends Service {


	public static final String ACTION_SAVE_DATA = "ess.imu_logger.data_export.action.saveData";
	public static final String ACTION_UPLOAD_DATA = "ess.imu_logger.data_export.action.uploadData";
	public static final String ACTION_COMPRESS_DATA = "ess.imu_logger.data_export.action.compressData";
	public static final String ACTION_START_SERVICE = "ess.imu_logger.data_export.action.startLogging";

	public static final String EXTRA_SENSOR_DATA = "ess.imu_logger.data_export.extra.sensorData";

	private static final String TAG = "ess.imu_logger.data_export.SensorDataSavingService";

	private SharedPreferences sharedPrefs;
	private PlainFileWriter background;

	Handler inHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			if(msg.getData().getString("action").equals("save finished")){
				Log.i(TAG, "finished some Data saving");
			} else if(msg.getData().getString("action").equals("upload finished")){
				Log.i(TAG, "finished some Data Upload");
			}
			//txt.setText(txt.getText() + "Item " + key +System.getProperty("line.separator"));
		}
	};


	private final IBinder mBinder = new LocalBinder();
	public class LocalBinder extends Binder {
		SensorDataSavingService getService() {
			// Return this instance of LocalService so clients can call public methods
			return SensorDataSavingService.this;
		}
	}



	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			Log.i(TAG, "Broaccast Reciever recieved a Broadcast");

			// TODO check is the extra is really there

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
		return mBinder;
	}


	public int onStartCommand (Intent intent, int flags, int startId){
		Log.i(TAG, "onStartCommand called in SensorDataSavingService ...");
		if (intent != null) {
			final String action = intent.getAction();
			if (ACTION_SAVE_DATA.equals(action)) {
				Log.d(TAG,"ACTION_SAVE_DATA");
				saveData(intent);
				// send message to the handler with the current message handler

			} else if (ACTION_UPLOAD_DATA.equals(action)) {
				Log.d(TAG,"ACTION_UPLOAD_DATA");
				uploadData();
			} else if (ACTION_COMPRESS_DATA.equals(action)) {
				Log.d(TAG,"ACTION_COMPRESS_DATA");
				compressData();
			} else if (ACTION_START_SERVICE.equals(action)) {
				Log.d(TAG, "Called onStartCommand. Given Action: " + intent.getAction());
			}
		}
		return START_STICKY;
	}

	public void onCreate() {
		Log.d(TAG, "SensorDataSavingService onCreate ...");

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
		Log.d(TAG, "saveData called");
		System.out.println("Saving Data: " + intent.getExtras().getString(EXTRA_SENSOR_DATA));
		background.saveString(intent.getExtras().getString(EXTRA_SENSOR_DATA));
	}

	private void uploadData() {
	}

	private void compressData() {
	}


}

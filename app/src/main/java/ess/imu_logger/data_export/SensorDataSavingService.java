package ess.imu_logger.data_export;

import android.app.Service;
import android.content.Intent;
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


	public static final String ACTION_SAVE_DATA = "ess.imu_logger.data_export.action.startLogging";
	public static final String ACTION_UPLOAD_DATA = "ess.imu_logger.data_export.action.startLogging";
	public static final String ACTION_COMPRESS_DATA = "ess.imu_logger.data_export.action.startLogging";


	SharedPreferences sharedPrefs;
	PlainFileWriter background;

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

	private Handler outHandler;


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
				background.test2("Trauben");
				// send message to the handler with the current message handler

			} else if (ACTION_UPLOAD_DATA.equals(action)) {
				Log.d("INFO","ACTION_UPLOAD_DATA");
			} else if (ACTION_COMPRESS_DATA.equals(action)) {
				Log.d("INFO","ACTION_COMPRESS_DATA");
			}
		}
		return START_STICKY;
	}

	public void onCreate() {
		System.out.println("service created...");

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		background = new PlainFileWriter(inHandler);
		background.start();

	}
}

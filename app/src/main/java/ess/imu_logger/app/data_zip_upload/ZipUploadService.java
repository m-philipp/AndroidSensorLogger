package ess.imu_logger.app.data_zip_upload;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import ess.imu_logger.app.data_save.PlainFileWriter;

public class ZipUploadService extends Service {


	public static final String ACTION_UPLOAD_DATA = "ess.imu_logger.data_zip_upload.action.uploadData";
	public static final String ACTION_COMPRESS_DATA = "ess.imu_logger.data_zip_upload.action.compressData";
	public static final String ACTION_START_SERVICE = "ess.imu_logger.data_zip_upload.action.startService";

	// public static final String EXTRA_SENSOR_DATA = "ess.imu_logger.data_zip_upload.extra.sensorData";

	private static final String TAG = "ess.imu_logger.data_zip_upload.ZipUploadService";

	private SharedPreferences sharedPrefs;
	private Zipper zipper;
	private Uploader uploader;



	public ZipUploadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

	public void onCreate() {
		Log.d(TAG, "onCreate ...");

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);


		zipper = new Zipper();
		zipper.start();

		uploader = new Uploader();
		uploader.start();



	}

	public int onStartCommand (Intent intent, int flags, int startId){
		Log.i(TAG, "onStartCommand called ...");
		if (intent != null) {
			final String action = intent.getAction();
			if (ACTION_START_SERVICE.equals(action)) {
				Log.d(TAG, "Called onStartCommand. Given Action: " + intent.getAction());
				compressData();
				uploadData();
			}
		}
		return START_STICKY;
	}


	private void uploadData() {
		Log.d(TAG, "uploadData called");
		uploader.up();
	}


	private void compressData() {
		Log.d(TAG, "zipData called");
		zipper.zip();
	}


}

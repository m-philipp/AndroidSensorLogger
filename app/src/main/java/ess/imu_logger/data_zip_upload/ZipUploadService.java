package ess.imu_logger.data_zip_upload;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class ZipUploadService extends Service {


	public static final String ACTION_MANUAL_UPLOAD_DATA = "ess.imu_logger.data_zip_upload.action.manUploadData";
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
		zipper.zip();

		uploader = new Uploader(this);
		uploader.start();
		uploader.up();



	}

	public int onStartCommand (Intent intent, int flags, int startId){
		Log.i(TAG, "onStartCommand called ...");

		if(intent.getAction().equals(ACTION_MANUAL_UPLOAD_DATA)){
			uploader.up();
		}

		return START_STICKY;
	}




}

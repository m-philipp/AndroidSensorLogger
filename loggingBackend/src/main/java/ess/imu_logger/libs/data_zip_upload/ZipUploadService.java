package ess.imu_logger.libs.data_zip_upload;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

//import ess.imu_logger.myReceiver;

public class ZipUploadService extends Service {


    public static final String ACTION_MANUAL_UPLOAD_DATA = "ess.imu_logger.libs.data_zip_upload.action.manUploadData";
    public static final String ACTION_START_SERVICE = "ess.imu_logger.libs.data_zip_upload.action.startService";

    // public static final String EXTRA_SENSOR_DATA = "ess.imu_logger.libs.data_zip_upload.extra.sensorData";

    private static final String TAG = "ess.imu_logger.libs.data_zip_upload.ZipUploadService";

    private SharedPreferences sharedPrefs;
    private Zipper zipper;
    private Uploader uploader;

    private Boolean zipperRunning = false;
    private Boolean uploaderRunning = false;


    public ZipUploadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        Log.d(TAG, "onCreate ...");

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand called ...");

/*
        if(intent == null){
			return START_STICKY;
		} //Caused by: java.lang.NullPointerException at ess.imu_logger.libs.data_zip_upload.ZipUploadService.onStartCommand(ZipUploadService.java:52) // HERE
*/

        if (intent == null || intent.getAction().equals(ACTION_START_SERVICE) ||
                intent.getAction().equals(ACTION_MANUAL_UPLOAD_DATA)) {

            if (!zipperRunning) {
                Log.d(TAG, "onStartCommand with: " + ACTION_START_SERVICE + " called");
                zipper = new Zipper(this);
                zipper.start();
                zipper.zip();
                zipperRunning = true;
                //zipper.requestStop();

            }
            // TODO check upload cycle

            Long freq = Long.parseLong(sharedPrefs.getString("upload_frequency", "0"));
            Long last = Long.parseLong(sharedPrefs.getString("last_upload", "0"));
            Long now = System.currentTimeMillis();

            if (!(intent == null) && intent.getAction().equals(ACTION_MANUAL_UPLOAD_DATA)
                    || (freq != 0 && (now - last) > freq)) {
                if (!uploaderRunning) {
                    uploader = new Uploader(this);
                    uploader.start();
                    uploader.up();
                    uploaderRunning = true;
                    //uploader.requestStop();

                    Editor editor = sharedPrefs.edit();
                    editor.putString("last_upload", ((Long) System.currentTimeMillis()).toString());
                    editor.commit();
                }
            }
        }


        // TODO: wait for thread finishing / working
        // stopSelf();

        return START_STICKY;
    }

    public void onDestroy() {
        Log.i(TAG, "onDestroy called ...");
    }


    public void zipperStopped() {
        zipperRunning = false;

        if (!zipperRunning && !uploaderRunning) {
            stopSelf();
        }
    }

    public void uploaderStopped() {
        uploaderRunning = false;

        if (!zipperRunning && !uploaderRunning) {
            stopSelf();
        }
    }

}

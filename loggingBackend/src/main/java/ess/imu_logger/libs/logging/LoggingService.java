package ess.imu_logger.libs.logging;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * An {@link Service} subclass for handling asynchronous task requests.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class LoggingService extends Service {

    private Logger serviceHandler;
    private HandlerThread thread;
    private Looper serviceLooper;


    private boolean loggingStarted = false;
    private SharedPreferences sharedPrefs;


    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_START_LOGGING = "ess.imu_logger.action.startLogging";
    public static final String ACTION_STOP_LOGGING = "ess.imu_logger.action.stopLogging";

    private static final String TAG = "ess.imu_logger.libs.logging.LoggingService";

    // TODO: Rename parameters
    // private static final String EXTRA_GYRO = "ess.imu_logger.extra.GYRO";


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "on onStartCommand called.");

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_LOGGING.equals(action)) {

                Log.d(TAG, "Called onStartCommand. Given Action: " + intent.getAction());
                startRecording();

            } else if (ACTION_STOP_LOGGING.equals(action)) {

                Log.d(TAG, "Called onStartCommand. Given Action: " + intent.getAction());
                stopRecording();

            }
        } else {

            startRecording();

        }
        return START_STICKY;
    }

    public IBinder onBind(Intent i) {

        return null;

    }

    public void onDestroy() {

        Log.d(TAG, "on Destroy called.");

        stopRecording();

        thread.quit();
    }


    public void onCreate() {

        Log.d(TAG, "on onCreate called.");

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_FOREGROUND);
        thread.start();
        serviceLooper = thread.getLooper();
        serviceHandler = new Logger(serviceLooper, this);

    }


    private synchronized void startRecording() {

		/*
        if (!loggingStarted) {
			// send message to Logger
			Message msg = serviceHandler.obtainMessage();
			msg.what = Logger.MESSAGE_START;
			msg.obj = new Intent();
			serviceHandler.sendMessage(msg);
			loggingStarted = true;
		}
        */

        serviceHandler.sendEmptyMessage(Logger.MESSAGE_START);
        loggingStarted = true;
    }


    private synchronized void stopRecording() {

        //if (loggingStarted) {

        serviceHandler.sendEmptyMessage(Logger.MESSAGE_STOP);
        loggingStarted = false;

        this.stopSelf();
        //}
    }

}

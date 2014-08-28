package ess.imu_logger;

import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.Process;
import android.preference.PreferenceManager;

/**
 * An {@link Service} subclass for handling asynchronous task requests.
 * <p>
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
    private static final String ACTION_START_LOGGING = "ess.imu_logger.action.startLogging";
    private static final String ACTION_STOP_LOGGING = "ess.imu_logger.action.stopLogging";
    private static final String ACTION_UPLOAD_DATA = "ess.imu_logger.action.uploadData";

    // TODO: Rename parameters
    // private static final String EXTRA_GYRO = "ess.imu_logger.extra.GYRO";



    public LoggingService(){

    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see Service

    // TODO: Customize helper method
    public static void startActionStartLogging(Context context, Boolean acc, Boolean gyro, Boolean mag) {
        Intent intent = new Intent(context, LoggingService.class);
        intent.setAction(ACTION_START_LOGGING);
        intent.putExtra(EXTRA_ACC, acc);
        intent.putExtra(EXTRA_GYRO, gyro);
        intent.putExtra(EXTRA_MAG, mag);
        context.startService(intent);
    }*/

    @Override
    public int onStartCommand (Intent intent, int flags, int startId){

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_LOGGING.equals(action)) {

                System.out.println("Called onStartCommand. Given Action: " + intent.getAction());
                startRecording();
                //return handleStartLogging(acc, gyro, mag);
            } else if (ACTION_STOP_LOGGING.equals(action)) {

                stopRecording();

            } else if (ACTION_UPLOAD_DATA.equals(action)) {
                //return handleUploadData(url, user);
            }
        }
        return START_STICKY;
    }

    public IBinder onBind(Intent i){
        return null;
    }

    public void onDestroy() { }

    public void onCreate() {
        System.out.println("service created...");

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_FOREGROUND);
        thread.start();
        serviceLooper = thread.getLooper();
        serviceHandler = new Logger(serviceLooper, this);

    }



    private synchronized void startRecording(){

        if(!loggingStarted){

            // add sensor preferences
	        Intent intent = new Intent();

            intent.putExtra("gyroscope", sharedPrefs.getBoolean("gyroscope", false));
            intent.putExtra("accelerometer", sharedPrefs.getBoolean("accelerometer", true));
            intent.putExtra("magneticField", sharedPrefs.getBoolean("magneticField", false));
            intent.putExtra("rotation", sharedPrefs.getBoolean("rotation", false));
            intent.putExtra("linearAccelerometer", sharedPrefs.getBoolean("linearAccelerometer", false));
            intent.putExtra("gravity", sharedPrefs.getBoolean("gravity", false));
            intent.putExtra("ambientLight", sharedPrefs.getBoolean("ambientLight", false));
            intent.putExtra("proximity", sharedPrefs.getBoolean("proximity", false));
            intent.putExtra("temperature", sharedPrefs.getBoolean("temperature", false));
            intent.putExtra("humidity", sharedPrefs.getBoolean("humidity", false));
            intent.putExtra("pressure", sharedPrefs.getBoolean("pressure", false));
            intent.putExtra("time", SystemClock.elapsedRealtime() + 1000 * 86400 * 14);

            // send message to BasicLogger
            Message msg = serviceHandler.obtainMessage();
            msg.what = Logger.MESSAGE_START;
            msg.obj = intent;
            serviceHandler.sendMessage(msg);

            loggingStarted = true;
        }

    }


    private synchronized void stopRecording() {
        serviceHandler.sendEmptyMessage(Logger.MESSAGE_STOP);
        loggingStarted = false;
    }

}

package de.smart_sense.tracker.libs.logging;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.util.Log;

import de.smart_sense.tracker.R;
import de.smart_sense.tracker.libs.StartActivity;
import de.smart_sense.tracker.libs.Util;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat.WearableExtender;

/**
 * An {@link Service} subclass for handling asynchronous task requests.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public abstract class LoggingService extends Service {

    private Logger serviceHandler;
    private HandlerThread thread;
    private Looper serviceLooper;


    private boolean loggingStarted = false;

    public static final String ACTION_START_LOGGING = "de.smart_sense.tracker.action.startLogging";
    public static final String ACTION_STOP_LOGGING = "de.smart_sense.tracker.action.stopLogging";

    private static final String TAG = "de.smart_sense.tracker.libs.logging.LoggingService";

    private WakeLock wl;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand called.");

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_LOGGING.equals(action)) {

                Log.d(TAG, "Called onStartCommand. Given Action: " + intent.getAction());
                startRecording();

            } else if (ACTION_STOP_LOGGING.equals(action)) {

                Log.d(TAG, "Called onStartCommand. Given Action: " + intent.getAction());
                stopRecording();
                return START_NOT_STICKY;

            }
        } else {

            Log.d(TAG, "Called onStartCommand. intent == null");
            startRecording();

        }
        return START_STICKY;
    }

    public IBinder onBind(Intent i) {

        return null;

    }

    public void onDestroy() {

        Log.d(TAG, "on Destroy called.");
        stopForeground(true);
        wl.release();

        //stopRecording();

        thread.quit();
    }


    public void onCreate() {

        Log.d(TAG, "on onCreate called.");

        Notification n = getNotificationIntent();


        startForeground(1337,  n);


        PowerManager pm = (PowerManager)getApplicationContext().getSystemService(
                getApplicationContext().POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wl.acquire();

        // TODO
        thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_FOREGROUND);
        //thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_MORE_FAVORABLE)m;



        thread.start();
        serviceLooper = thread.getLooper();
        serviceHandler = new Logger(serviceLooper, this);

    }

    protected abstract Notification getNotificationIntent();


    private synchronized void startRecording() {

        if(!loggingStarted) {
            serviceHandler.sendEmptyMessage(Logger.MESSAGE_START);
            loggingStarted = true;
        }
    }


    private synchronized void stopRecording() {

        if (loggingStarted) {
            serviceHandler.sendEmptyMessage(Logger.MESSAGE_STOP);
            loggingStarted = false;
        }

        stopSelf();
    }

}

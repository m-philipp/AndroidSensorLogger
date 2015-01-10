package de.smart_sense.tracker.libs;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

public class WearableMessageSenderService extends Service implements
        GoogleApiClient.ConnectionCallbacks,  GoogleApiClient.OnConnectionFailedListener {


    private static final String TAG = "de.smart_sense.tracker.app.WearableMessageSenderService";


    public static final String ACTION_SEND_MESSAGE = "de.smart_sense.tracker.libs.wearableMessageSenderService.sendMessage";
    public static final String ACTION_SEND_PREFERENCES = "de.smart_sense.tracker.libs.wearableMessageSenderService.sendPrefs";
    public static final String ACTION_START_SERVICE = "de.smart_sense.tracker.libs.wearableMessageSenderService.startLogging";
    public static final String ACTION_STOP_SERVICE = "de.smart_sense.tracker.libs.wearableMessageSenderService.stopLogging";


    public static final String EXTRA_PATH = "de.smart_sense.tracker.libs.extra.messagePath";
    public static final String EXTRA_MESSAGE_CONTENT_STRING = "de.smart_sense.tracker.libs.extra.messageContentString";


    private GoogleApiClient mGoogleApiClient;
    protected SharedPreferences sharedPrefs;

    private SendMessageThread smt;
    private Boolean smtRunning = false;


    @Override
    public void onCreate() {

        super.onCreate();

        Log.d(TAG, "creating new MessageSenderService");

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();

        mGoogleApiClient.connect();


        smt = new SendMessageThread();


    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand called ...");

        if (mGoogleApiClient.isConnected())
            onConnected(null);
        else
            mGoogleApiClient.connect();

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SEND_MESSAGE.equals(action)) {

                Log.d(TAG, ACTION_SEND_MESSAGE);
                startService();

                smt.sendMessage(intent.getStringExtra(EXTRA_PATH),
                        intent.getStringExtra(EXTRA_MESSAGE_CONTENT_STRING));

            } else if (ACTION_SEND_PREFERENCES.equals(action)) {

                Log.d(TAG, ACTION_SEND_PREFERENCES);
                startService();
                smt.sendPreferences();

            } else if (ACTION_START_SERVICE.equals(action)) {

                Log.d(TAG, ACTION_START_SERVICE);
                startService();

            } else if (ACTION_STOP_SERVICE.equals(action)) {

                Log.d(TAG, ACTION_STOP_SERVICE);
                if (smtRunning) {
                    smt.requestStop();
                }
                stopSelf();
                return START_NOT_STICKY;

            }
        }

        return START_STICKY;
    }

    public static void sendMessage(Context context, String path, String content) {

        Log.d(TAG, "starting Message Sender ...");

        Intent messageSenderIntent = new Intent(context, WearableMessageSenderService.class);
        messageSenderIntent.setAction(WearableMessageSenderService.ACTION_SEND_MESSAGE);
        messageSenderIntent.putExtra(WearableMessageSenderService.EXTRA_PATH, path);
        messageSenderIntent.putExtra(WearableMessageSenderService.EXTRA_MESSAGE_CONTENT_STRING, content);

        context.startService(messageSenderIntent);

    }


    private void startService() {
        if (!smtRunning && !smt.isAlive()) {
            smt.start();
            smtRunning = true;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onDestroy() {
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();

        super.onDestroy();
    }


    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "onConnected called ...");
    }
    // Placeholders for required connection callbacks
    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Failed to connect to Google Api Client");
    }


    class SendMessageThread extends Thread {
        private Handler inHandler;
        public static final String MESSAGE_TYPE_ACTION = "de.smart_sense.tracker.libs.data_save.MESSAGE_TYPE_ACTION";
        public static final String MESSAGE_PATH = "de.smart_sense.tracker.libs.data_save.MESSAGE_PATH";
        public static final String MESSAGE_CONTENT = "de.smart_sense.tracker.libs.data_save.MESSAGE_CONTENT";
        public static final int MESSAGE_ACTION_SEND_MESSAGE = 0;
        public static final int MESSAGE_ACTION_TRANSFER_PREFERENCES = 1;


        // Constructor for sending data objects to the data layer
        SendMessageThread() {
        }

        public void run() {
            Log.d(TAG, "SendMessageThread run");
            try {
                // preparing a looper on current thread
                // the current thread is being detected implicitly
                Looper.prepare();
                synchronized (this) {
                    // now, the handler will automatically bind to the
                    // Looper that is attached to the current thread
                    // You don't need to specify the Looper explicitly
                    inHandler = new Handler() {
                        public void handleMessage(Message msg) {


                            if(!mGoogleApiClient.isConnected()){
                                ConnectionResult result = mGoogleApiClient.blockingConnect(1000, TimeUnit.MILLISECONDS);
                                if (!result.isSuccess()) {
                                    Log.d(TAG, "GoogleClientConnect was false");
                                    return;
                                }
                            }

                            if (msg.getData().getInt(MESSAGE_TYPE_ACTION) == MESSAGE_ACTION_SEND_MESSAGE) {

                                // TODO check if the mGoogleApiClient is still connected
                                final String path = msg.getData().getString(MESSAGE_PATH, "/nowhere");
                                final String content = msg.getData().getString(MESSAGE_CONTENT, "");


                                Log.d(TAG, "sending path: " + path + " with content: " + content);

                                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
                                        new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                                            @Override
                                            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                                                for (final Node node : getConnectedNodesResult.getNodes()) {
                                                    Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path,
                                                            content.getBytes()).setResultCallback(getSendMessageResultCallback());
                                                }
                                            }
                                        }
                                );

                            } else if (msg.getData().getInt(MESSAGE_TYPE_ACTION) == MESSAGE_ACTION_TRANSFER_PREFERENCES) {

                                Log.d(TAG, "transfering Prefs");

                                PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Util.GAC_PATH_PREFERENCES);

                                DataMap dataMap = putDataMapRequest.getDataMap();

                                dataMap.putBoolean(Util.PREFERENCES_SENSOR_ACTIVATE, sharedPrefs.getBoolean(Util.PREFERENCES_SENSOR_ACTIVATE, true));
                                dataMap.putBoolean(Util.PREFERENCES_WEAR_TEMP_LOGGING, sharedPrefs.getBoolean(Util.PREFERENCES_WEAR_TEMP_LOGGING, true));
                                dataMap.putString(Util.PREFERENCES_WEAR_TEMP_LOGGING_DURATION, sharedPrefs.getString(Util.PREFERENCES_WEAR_TEMP_LOGGING_DURATION, "0"));
                                dataMap.putString(Util.PREFERENCES_LAST_ANNOTATION, sharedPrefs.getString(Util.PREFERENCES_LAST_ANNOTATION, "0"));

                                dataMap.putBoolean(Util.PREFERENCES_START_ON_BOOT, sharedPrefs.getBoolean(Util.PREFERENCES_START_ON_BOOT, true));

                                dataMap.putString(Util.PREFERENCES_NAME, sharedPrefs.getString(Util.PREFERENCES_NAME, "Eva Musterfrau"));
                                dataMap.putString(Util.PREFERENCES_ANNOTATION_NAME, sharedPrefs.getString(Util.PREFERENCES_ANNOTATION_NAME, "smoking"));
                                dataMap.putBoolean(Util.PREFERENCES_ANONYMIZE, sharedPrefs.getBoolean(Util.PREFERENCES_ANONYMIZE, true));

                                dataMap.putBoolean(Util.PREFERENCES_SENSOR_ACTIVATE, sharedPrefs.getBoolean(Util.PREFERENCES_SENSOR_ACTIVATE, false));
                                dataMap.putString(Util.PREFERENCES_SAMPLING_RATE, sharedPrefs.getString(Util.PREFERENCES_SAMPLING_RATE, "3"));

                                dataMap.putBoolean(Util.PREFERENCES_ACCELEROMETER, sharedPrefs.getBoolean(Util.PREFERENCES_ACCELEROMETER, false));
                                dataMap.putBoolean(Util.PREFERENCES_GYROSCOPE, sharedPrefs.getBoolean(Util.PREFERENCES_GYROSCOPE, false));
                                dataMap.putBoolean(Util.PREFERENCES_MAGNETIC_FIELD, sharedPrefs.getBoolean(Util.PREFERENCES_MAGNETIC_FIELD, false));
                                dataMap.putBoolean(Util.PREFERENCES_AMBIENT_LIGHT, sharedPrefs.getBoolean(Util.PREFERENCES_AMBIENT_LIGHT, false));
                                dataMap.putBoolean(Util.PREFERENCES_PROXIMITY, sharedPrefs.getBoolean(Util.PREFERENCES_PROXIMITY, false));
                                dataMap.putBoolean(Util.PREFERENCES_TEMPERATURE, sharedPrefs.getBoolean(Util.PREFERENCES_TEMPERATURE, false));
                                dataMap.putBoolean(Util.PREFERENCES_HUMIDITY, sharedPrefs.getBoolean(Util.PREFERENCES_HUMIDITY, false));
                                dataMap.putBoolean(Util.PREFERENCES_PRESSURE, sharedPrefs.getBoolean(Util.PREFERENCES_PRESSURE, false));

                                dataMap.putBoolean(Util.PREFERENCES_ROTATION, sharedPrefs.getBoolean(Util.PREFERENCES_ROTATION, false));
                                dataMap.putBoolean(Util.PREFERENCES_GRAVITY, sharedPrefs.getBoolean(Util.PREFERENCES_GRAVITY, false));
                                dataMap.putBoolean(Util.PREFERENCES_LINEAR_ACCELEROMETER, sharedPrefs.getBoolean(Util.PREFERENCES_LINEAR_ACCELEROMETER, false));
                                dataMap.putBoolean(Util.PREFERENCES_STEPS, sharedPrefs.getBoolean(Util.PREFERENCES_STEPS, false));

                                dataMap.putString(Util.SENT_TIMESTAMP, String.valueOf(System.currentTimeMillis()));

                                PutDataRequest request = putDataMapRequest.asPutDataRequest();
                                PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                                        .putDataItem(mGoogleApiClient, request);

                            }


                        }
                    };
                    notifyAll();
                }


                Looper.loop();

                Log.i(TAG, "Thread exiting gracefully");
            } catch (Throwable t) {
                Log.e(TAG, "Thread halted due to an error", t);
            }


        }

        public synchronized void sendMessage(String path, String content) {
            Message msg = new Message();
            Bundle b = new Bundle();
            b.putInt(MESSAGE_TYPE_ACTION, MESSAGE_ACTION_SEND_MESSAGE);
            b.putString(MESSAGE_PATH, path);
            b.putString(MESSAGE_CONTENT, content);
            msg.setData(b);

            // could be a runnable when calling post instead of sendMessage
            getHandler().sendMessage(msg);
        }

        public synchronized void sendPreferences() {
            Message msg = new Message();
            Bundle b = new Bundle();
            b.putInt(MESSAGE_TYPE_ACTION, MESSAGE_ACTION_TRANSFER_PREFERENCES);
            msg.setData(b);

            // could be a runnable when calling post instead of sendMessage
            getHandler().sendMessage(msg);
        }

        // This method is allowed to be called from any thread
        public synchronized void requestStop() {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "Thread loop quitting by request");
                    Looper.myLooper().quit();
                }
            });
        }

        private Handler getHandler() {
            while (inHandler == null) {
                try {
                    wait();     // TODO might hold the UI Thread; So this is bad :)
                } catch (InterruptedException e) {
                    //Ignore and try again.
                }
            }
            return inHandler;
        }


        private ResultCallback<MessageApi.SendMessageResult> getSendMessageResultCallback() {
            return new ResultCallback<MessageApi.SendMessageResult>() {
                @Override
                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                    if (!sendMessageResult.getStatus().isSuccess()) {
                        Log.e(TAG, "Failed to connect to Google Api Client with status "
                                + sendMessageResult.getStatus());
                    } else {
                        Log.d(TAG, "Successfully connected to Google Api Client.");
                    }
                }
            };
        }

    }


}

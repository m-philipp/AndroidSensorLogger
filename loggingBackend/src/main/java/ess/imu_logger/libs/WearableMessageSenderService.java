package ess.imu_logger.libs;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
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

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

public class WearableMessageSenderService extends Service implements
        GoogleApiClient.OnConnectionFailedListener {


    private static final String TAG = "ess.imu_logger.app.WearableMessageSenderService";


    public static final String ACTION_SEND_MESSAGE = "ess.imu_logger.libs.wearableMessageSenderService.sendMessage";
    public static final String ACTION_START_SERVICE = "ess.imu_logger.libs.wearableMessageSenderService.startLogging";
    public static final String ACTION_STOP_SERVICE = "ess.imu_logger.libs.wearableMessageSenderService.stopLogging";


    public static final String EXTRA_PATH = "ess.imu_logger.libs.extra.messagePath";
    public static final String EXTRA_MESSAGE_CONTENT_STRING = "ess.imu_logger.libs.extra.messageContentString";


    private GoogleApiClient mGoogleApiClient;

    private SendMessageThread smt;
    private Boolean smtRunning = false;

    public WearableMessageSenderService() {
    }

    @Override
    public void onCreate() {

        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();


        smt = new SendMessageThread();


    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand called ...");

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SEND_MESSAGE.equals(action)) {
                Log.d(TAG, ACTION_SEND_MESSAGE);
                if(!smtRunning && !smt.isAlive()) {
                    smt.start();
                    smtRunning = true;
                }
                smt.sendMessage(intent.getStringExtra(EXTRA_PATH),
                        intent.getStringExtra(EXTRA_MESSAGE_CONTENT_STRING));
            } else if (ACTION_START_SERVICE.equals(action)) {
                Log.d(TAG, ACTION_START_SERVICE);
                if(!smtRunning && !smt.isAlive()) {
                    smt.start();
                    smtRunning = true;
                }
            } else if (ACTION_STOP_SERVICE.equals(action)) {
                Log.d(TAG, ACTION_STOP_SERVICE);
                if(smtRunning || smt.isAlive()) {
                    smt.requestStop();
                    smtRunning = false;
                    stopSelf();
                }
                return START_NOT_STICKY;
            }
        }

        return START_STICKY;
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

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Failed to connect to Google Api Client");
    }


    class SendMessageThread extends Thread {
        private Handler inHandler;
        public static final String MESSAGE_TYPE_ACTION = "ess.imu_logger.libs.data_save.MESSAGE_TYPE_ACTION";
        public static final String MESSAGE_PATH = "ess.imu_logger.libs.data_save.MESSAGE_PATH";
        public static final String MESSAGE_CONTENT = "ess.imu_logger.libs.data_save.MESSAGE_CONTENT";
        public static final int MESSAGE_ACTION_SEND_MESSAGE = 0;


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


                            if (msg.getData().getInt(MESSAGE_TYPE_ACTION) == MESSAGE_ACTION_SEND_MESSAGE){

                                // TODO Magic
                                final String  path = msg.getData().getString(MESSAGE_PATH, "/nowhere");
                                final String  content = msg.getData().getString(MESSAGE_CONTENT, "");


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
                    wait();
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

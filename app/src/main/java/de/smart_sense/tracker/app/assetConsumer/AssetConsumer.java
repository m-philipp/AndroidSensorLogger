package de.smart_sense.tracker.app.assetConsumer;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import de.smart_sense.tracker.libs.Util;
import de.smart_sense.tracker.libs.WearableMessageSenderService;


public class AssetConsumer extends Service {

    public static final String ACTION_START_SERVICE = "de.smart_sense.tracker.app.assetConsumer.action.startService";
    private static final String TAG = "de.smart_sense.tracker.app.AssetConsumer";

    private SharedPreferences sharedPrefs;

    private Boolean assetThreadRunning = false;
    private AssetConsumer ac;
    private AssetSaveThread ast;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onCreate() {
        Log.d(TAG, "onCreate ...");

        ac = this;

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand called ...");

/*
        if(intent == null){
			return START_STICKY;
		} //Caused by: java.lang.NullPointerException at de.smart_sense.tracker.libs.data_zip_upload.ZipUploadService.onStartCommand(ZipUploadService.java:52) // HERE
*/

        if (intent == null || intent.getAction().equals(ACTION_START_SERVICE)) {
            Log.d(TAG, "onStartCommand with: " + ACTION_START_SERVICE + " called");

            if (!assetThreadRunning) {
                Log.d(TAG, "start Asset Consumer Thread");
                ast = new AssetSaveThread();
                ast.start();
                ast.consume();
                assetThreadRunning = true;
            }

        }

        // TODO: wait for thread finishing / working
        // stopSelf();

        return START_STICKY;
    }

    public void onDestroy() {
        Log.i(TAG, "onDestroy called ...");
    }

    public void assetThreadStopped() {
        assetThreadRunning = false;
        stopSelf();
    }

    public class AssetSaveThread extends Thread implements
            GoogleApiClient.OnConnectionFailedListener {

        private static final String TAG = "de.smart_sense.tracker.app.AssetConsumer.AssetSaveThread";

        public static final String MESSAGE_TYPE_ACTION = "de.smart_sense.tracker.app.AssetConsumer.AssetSaveThread.MESSAGE_TYPE_ACTION";
        public static final int MESSAGE_ACTION_CONSUME = 0;


        private Handler inHandler;
        private GoogleApiClient mGoogleApiClient;

        Boolean assetSaved = false;

        AssetSaveThread() {

            Log.d(TAG, "created AssetSaveThread");

            mGoogleApiClient = new GoogleApiClient.Builder(ac)
                    .addApi(Wearable.API)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        public void run() {
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
                            // Act on the message received from my UI thread doing my stuff
                            ConnectionResult conResult =
                                    mGoogleApiClient.blockingConnect(1, TimeUnit.SECONDS);
                            if (!conResult.isSuccess()) {
                                Log.d(TAG, "GoogleClientConnect was false");
                                return;
                            }

                            assetSaved = false;

                            Log.d(TAG, "trying to extract asset");
                            Uri.Builder uri = new Uri.Builder()
                                    .scheme(PutDataRequest.WEAR_URI_SCHEME)
                                    .authority(Util.getRemoteNodeId(mGoogleApiClient))
                                    .path(Util.GAC_PATH_SENSOR_DATA);
                            PendingResult pr = Wearable.DataApi.getDataItem(mGoogleApiClient, uri.build());

                            Result result = pr.await(3, TimeUnit.SECONDS);

                            if (result == null)
                                return;

                            DataApi.DataItemResult r = (DataApi.DataItemResult) result;
                            DataItem di = r.getDataItem();
                            if (di != null) {

                                DataMapItem dataMapItem = DataMapItem.fromDataItem(di);

                                String fileName = dataMapItem.getDataMap().getString(Util.SENSOR_FILE_NAME);
                                Asset profileAsset = dataMapItem.getDataMap().getAsset(Util.SENSOR_FILE);


                                assetSaved = saveFileFromAsset(fileName, profileAsset);  // TODO error handling


                                Log.d(TAG, "SensorDataFileName: " + fileName);

                                if (assetSaved) {

                                    Wearable.DataApi.deleteDataItems(mGoogleApiClient, uri.build()).await(3, TimeUnit.SECONDS).getNumDeleted();
                                    WearableMessageSenderService.sendMessage(ac, Util.GAC_PATH_CONFIRM_FILE_RECEIVED, fileName);

                                }

                                mGoogleApiClient.disconnect();

                                assetThreadStopped();
                                Looper.myLooper().quit();
                            }
                            }
                        }

                        ;

                        notifyAll();
                    }


                    // After the following line the thread will start
                    // running the message loop and will not normally
                    // exit the loop unless a problem happens or you
                    // quit() the looper (see below)
                    Looper.loop();

                    Log.i(TAG, "Zipper Thread exiting gracefully");
                }catch(
                        Throwable t
                )

                {
                    Log.e(TAG, "Zipper Thread halted due to an error", t);
                }finally

                {
                    assetThreadStopped();
                }

            }

        private Boolean logFileExists(String fileName) {

            Log.d(TAG, "logFileExists");


            if (!Util.isExternalStorageWritable())
                return false; // TODO cry for some help...


            Util.checkDirs();

            // CHECK IF FILE ALREADY EXISTED! IF SO BREAK.
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + Util.fileDir, "wear_" + fileName);
            if (file.exists())
                return true; // TODO: true! leads to file removal on watch


            return false;


        }

        private Boolean saveFileFromAsset(String fileName, Asset asset) {

            Log.d(TAG, "trying to Save File form Asset");

            if (asset == null) {
                throw new IllegalArgumentException("Asset must be non-null");
            }
            if (!mGoogleApiClient.isConnected())
                return false;

            // convert asset into a file descriptor and block until it's ready
            InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                    mGoogleApiClient, asset).await().getInputStream();
            mGoogleApiClient.disconnect();

            if (assetInputStream == null) {
                Log.w(TAG, "Requested an unknown Asset.");
                return false;
            }

            if (!Util.isExternalStorageWritable())
                return false; // TODO cry for some help...

            Util.checkDirs();
            try {

                // CHECK IF FILE ALREADY EXISTED! IF SO BREAK.
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + Util.fileDir, "wear_" + fileName);
                if (file.exists())
                    return true; // TODO: true! leads to file removal on watch

                OutputStream out = new FileOutputStream(file, true);

                byte[] buffer = new byte[1024];
                int bytesRead;
                //read from is to buffer
                while ((bytesRead = assetInputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                assetInputStream.close();
                //flush OutputStream to write any buffered data to file
                out.flush();
                out.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;


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


        public synchronized void consume() {

            Log.d(TAG, "called zip");

            Message msg = new Message();
            Bundle b = new Bundle();
            b.putInt(MESSAGE_TYPE_ACTION, MESSAGE_ACTION_CONSUME);
            msg.setData(b);

            // could be a runnable when calling post instead of sendMessage
            getHandler().sendMessage(msg);
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.e(TAG, "Failed to connect to Google Api Client");
        }
    }

}

package de.smart_sense.tracker.libs;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
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
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

public class TransferDataAsAssets extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "de.smart_sense.tracker.libs.TransferDataAsAssets";
    public static final String ACTION_TRANSFER = "de.smart_sense.tracker.libs.TransferDataAsAssets.transfer";


    protected GoogleApiClient mGoogleApiClient;
    private SendToDataLayerThread stdlt;
    private Boolean stdltRunning = false;



    public TransferDataAsAssets() {
    }

    public void onCreate() {

        super.onCreate();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();

        stdlt = new SendToDataLayerThread();
    }



    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand called ...");

        if (mGoogleApiClient.isConnected())
            onConnected(null);
        else
            mGoogleApiClient.connect();


        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_TRANSFER.equals(action)) {

                Log.d(TAG, ACTION_TRANSFER);

                if(!stdlt.isAlive()) {
                // if(!stdltRunning && !stdlt.isAlive()) { // TODO check that logic
                    //stdltRunning = true;
                    stdlt = new SendToDataLayerThread();
                    stdlt.start();
                }
                stdlt.queueDataTransfer();
            }
        }


        return START_STICKY;
    }



    public void transferFinished() {
        Log.d(TAG, "transfer Finished notice from Thread to Service");
        stdltRunning = false;
        stopSelf();
    }


    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "onConnected called ...");
    }


    // Placeholders for required connection callbacks
    @Override
    public void onConnectionSuspended(int cause) {
    }




    @Override
    public void onDestroy() {

        Log.d(TAG, "onDestory");

        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Failed to connect to Google Api Client");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
        // TODO: Return the communication channel to the service.
        // throw new UnsupportedOperationException("Not yet implemented");
    }


    class SendToDataLayerThread extends Thread {
        private Handler inHandler;
        public static final String MESSAGE_TYPE_ACTION = "de.smart_sense.tracker.libs.data_save.MESSAGE_TYPE_ACTION";
        public static final int MESSAGE_ACTION_TRANSFER_DATA = 0;

        private boolean transferedAsset = false;


        // Constructor for sending data objects to the data layer
        SendToDataLayerThread() {
        }

        public void run() {
            Log.d(TAG, "SendToDataLayerThread run");
            try {
                Looper.prepare();
                synchronized (this) {


                    inHandler = new Handler() {
                        public void handleMessage(Message msg) {

                            Log.d(TAG, "SendToDataLayerThread run -> handle message");

                            if (msg.getData().getInt(MESSAGE_TYPE_ACTION) == MESSAGE_ACTION_TRANSFER_DATA) {

                                Log.d(TAG, "trying to transfer data to phone");

                                String fileName = getFilenameToUpload(false);
                                if (fileName == null) {
                                    Log.d(TAG, "there's no file to transfer");
                                    return;
                                }

                                transferedAsset = true;

                                Uri.Builder uri = new Uri.Builder().scheme(PutDataRequest.WEAR_URI_SCHEME).path(Util.GAC_PATH_SENSOR_DATA);
                                Wearable.DataApi.deleteDataItems(mGoogleApiClient, uri.build()).await();

                                Asset asset = createAssetFromFile(getFilenameToUpload());

                                PutDataMapRequest dataMap = PutDataMapRequest.create(Util.GAC_PATH_SENSOR_DATA);
                                dataMap.getDataMap().putString(Util.SENSOR_FILE_NAME, fileName);
                                dataMap.getDataMap().putString(Util.SENT_TIMESTAMP, "" + System.currentTimeMillis());
                                dataMap.getDataMap().putAsset(Util.SENSOR_FILE, asset);

                                //dataMap.getDataMap().putInt(COUNT_KEY, count++);
                                PutDataRequest request = dataMap.asPutDataRequest();
                                PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                                        .putDataItem(mGoogleApiClient, request);

                                // TODO check that on not connected it isn't called every sec
                                // TODO check that there's no out of memory (closed streams)



                            }

                            transferFinished();
                            Looper.myLooper().quit();

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

        private Asset createAssetFromFile(String fileName) {
            Log.d(TAG, "asset Creation from File");
            byte[] r;
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            try {

                //bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
                FileInputStream fileInputStream = new FileInputStream(new File(fileName));
                int maxBufferSize = 1 * 1024 * 1024;
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {

                    byteStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }
                r = byteStream.toByteArray();
                fileInputStream.close();
                byteStream.close();
                byteStream = null;
                fileInputStream = null;

            } catch (IOException e) {
                Log.d(TAG, "asset Creation from File failed: " + e.getMessage());
                r = new byte[0];
            }

            return Asset.createFromBytes(r);
        }

        public synchronized void queueDataTransfer() {

            Log.d(TAG, "called queueDataTransfer");

            if(transferedAsset)
                    return;

            Message msg = new Message();
            Bundle b = new Bundle();
            b.putInt(MESSAGE_TYPE_ACTION, MESSAGE_ACTION_TRANSFER_DATA);
            msg.setData(b);
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


        private String getFilenameToUpload() {
            return getFilenameToUpload(true);
        }

        private String getFilenameToUpload(Boolean full) {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + File.separator + Util.fileDir);
            String[] listOfFiles = dir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    Log.v(TAG, "filename: " + filename);
                    return filename.endsWith(".zip");
                }
            });

            Arrays.sort(listOfFiles);

            if (listOfFiles.length > 1) {
                if (full)
                    return dir.getAbsolutePath() + File.separator + listOfFiles[0]; // TODO check if we didn't take the newest file.
                return listOfFiles[0];
            }

            return null;

        }
    }
}

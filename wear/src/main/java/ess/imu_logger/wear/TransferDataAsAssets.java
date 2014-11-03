package ess.imu_logger.wear;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

import ess.imu_logger.libs.Util;

public class TransferDataAsAssets extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "ess.imu_logger.wear.TransferDataAsAssets";
    public static final String ACTION_TRANSFER = "transfer";

    protected GoogleApiClient mGoogleApiClient;


    public TransferDataAsAssets() {
    }

    public void onCreate() {

        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();

    }
    private static final String COUNT_KEY = "/count";
    private int count = 0;
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand called ...");

        if (mGoogleApiClient.isConnected())
            onConnected(null);
        else
            mGoogleApiClient.connect();

        /*
        PutDataMapRequest dataMap = PutDataMapRequest.create("/count");
        dataMap.getDataMap().putInt(COUNT_KEY, count++);
        PutDataRequest request = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                .putDataItem(mGoogleApiClient, request);
        */

        new SendToDataLayerThread().start();



        return START_STICKY;
    }

    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "onConnected called ...");
/*
        String fileName = getFilenameToUpload();
        //Asset asset = createAssetFromFile(fileName);

        //PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Util.GAC_PATH_SENSOR_DATA);
        //DataMap dataMap = putDataMapRequest.getDataMap();

        DataMap dataMap = new DataMap();

        //dataMap.putAsset(Util.SENSOR_FILE, asset);
        dataMap.putString(Util.SENSOR_FILE_NAME, fileName);

        //PutDataRequest request = putDataMapRequest.asPutDataRequest();
        //PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
        //        .putDataItem(mGoogleApiClient, request);
        new SendToDataLayerThread(Util.GAC_PATH_SENSOR_DATA, dataMap).start();

        */
    }


    // Placeholders for required connection callbacks
    @Override
    public void onConnectionSuspended(int cause) {
    }

    private static Asset createAssetFromFile(String fileName) {
        Log.d(TAG, "asset Creation from File");
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
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
        } catch (IOException e) {
            Log.d(TAG, "asset Creation from File failed: " + e.getMessage());
        }
        return Asset.createFromBytes(byteStream.toByteArray());
    }


    @Override
    public void onDestroy() {
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
        String path;
        DataMap dataMap;

        // Constructor for sending data objects to the data layer
        SendToDataLayerThread() {
        }

        public void run() {
            Log.d(TAG, "SendToDataLayerThread run");

            String fileName = getFilenameToUpload(false);
            if(fileName == null){
                Log.d(TAG, "there's no file to send");
                return;
            }

            Asset asset = createAssetFromFile(getFilenameToUpload());

            PutDataMapRequest dataMap = PutDataMapRequest.create(Util.GAC_PATH_SENSOR_DATA);
            dataMap.getDataMap().putString(Util.SENSOR_FILE_NAME, fileName);
            dataMap.getDataMap().putAsset(Util.SENSOR_FILE, asset);
            //dataMap.getDataMap().putInt(COUNT_KEY, count++);
            PutDataRequest request = dataMap.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                    .putDataItem(mGoogleApiClient, request);


/*
            //Asset asset = createAssetFromFile(fileName);

            //PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Util.GAC_PATH_SENSOR_DATA);
            //DataMap dataMap = putDataMapRequest.getDataMap();

            PutDataMapRequest putDMR = PutDataMapRequest.create(Util.GAC_PATH_SENSOR_DATA);
            DataMap dataMap = putDMR.getDataMap();

            //dataMap.putAsset(Util.SENSOR_FILE, asset);
            dataMap.putString(Util.SENSOR_FILE_NAME, fileName);
            PutDataRequest request = putDMR.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                    .putDataItem(mGoogleApiClient, request);

            /*
            DataApi.DataItemResult result = Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();

            Log.d(TAG, "DataApi put completed");

            if (result.getStatus().isSuccess()) {
                Log.v(TAG, "DataMap: " + dataMap + " sent");
            } else {
                // Log an error
                Log.v(TAG, "ERROR: failed to send DataMap");
            }
            */
        }


        private String getFilenameToUpload() {
            return getFilenameToUpload(true);
        }

        private String getFilenameToUpload(Boolean full) {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + File.separator + Util.fileDir);
            String[] listOfFiles = dir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".zip") ? true : false;
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

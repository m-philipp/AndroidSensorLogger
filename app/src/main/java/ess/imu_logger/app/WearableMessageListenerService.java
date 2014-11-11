package ess.imu_logger.app;

/**
 * Created by martin on 09.09.2014.
 */

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import ess.imu_logger.libs.Util;
import ess.imu_logger.libs.WearableMessageSenderService;
import ess.imu_logger.libs.data_save.SensorDataSavingService;

/**
 * Listens for a message telling it to start the Wearable MainActivity.
 */
public class WearableMessageListenerService extends WearableListenerService implements
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "ess.imu_logger.app.WearableMessageListenerService";

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate(){

        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();

    }


    @Override
    public void onMessageReceived(MessageEvent event) {
        Log.d(TAG, "onMessageReceived");
        if (event.getPath().equals(Util.GAC_PATH_TEST_ACTIVITY)) {
            Toast.makeText(this, "Hello from Wearable!", Toast.LENGTH_LONG).show();
        } else if (event.getPath().equals(Util.GAC_PATH_ANNOTATED_SMOKING)) {
            Log.d(TAG, "annotating Smoking");
            Intent sendIntent = new Intent(SensorDataSavingService.BROADCAST_ANNOTATION);
            sendBroadcast(sendIntent);
        }
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        Log.d(TAG, "onDataChanged");

        // final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);

        /*
        long token = Binder.clearCallingIdentity();
        try {
            performOperationRequiringPermissions();
        } finally {
            Binder.restoreCallingIdentity(token);
        }
        */
/*
        if(!mGoogleApiClient.isConnected()){
            ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "Failed to connect to GoogleApiClient.");
                return;
            }
        }
*/

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {

                Log.d(TAG, "DataItem changed: " + event.getDataItem().getUri());

                if (event.getDataItem().getUri().getPath().equals(Util.GAC_PATH_SENSOR_DATA)) {

                    Log.d(TAG, "trying to extract asset");

                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());

                    String fileName = dataMapItem.getDataMap().getString(Util.SENSOR_FILE_NAME);
                    Asset profileAsset = dataMapItem.getDataMap().getAsset(Util.SENSOR_FILE);

                    saveFileFromAsset(fileName, profileAsset);
                    // Do something with the bitmap


                    ConnectionResult result =
                            mGoogleApiClient.blockingConnect(1000, TimeUnit.MILLISECONDS);
                    if (!result.isSuccess()) {
                        return;
                    }
                    Uri.Builder uri = new Uri.Builder().scheme(PutDataRequest.WEAR_URI_SCHEME).path(Util.GAC_PATH_SENSOR_DATA);
                    Wearable.DataApi.deleteDataItems(mGoogleApiClient, uri.build()).await();
                    mGoogleApiClient.disconnect();


                    sendMessage(Util.GAC_PATH_CONFIRM_FILE_RECEIVED, fileName);

                    Log.d(TAG, "SensorDataFileName: " + fileName);
                }
            }
        }

        // mGoogleApiClient.disconnect();
    }


    private void sendMessage(String path, String content) {

        Log.d(TAG, "starting Message Sender ...");

        Intent messageSenderIntent = new Intent(this, WearableMessageSenderService.class);
        messageSenderIntent.setAction(WearableMessageSenderService.ACTION_SEND_MESSAGE);
        messageSenderIntent.putExtra(WearableMessageSenderService.EXTRA_PATH, path);
        messageSenderIntent.putExtra(WearableMessageSenderService.EXTRA_MESSAGE_CONTENT_STRING, content);


        this.startService(messageSenderIntent);

    }


    private void saveFileFromAsset(String fileName, Asset asset) {

        Log.d(TAG, "trying to Save File form Asset");

        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result =
                mGoogleApiClient.blockingConnect(1000, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();
        mGoogleApiClient.disconnect();

        if (assetInputStream == null) {
            Log.w(TAG, "Requested an unknown Asset.");
            return;
        }

        if (!Util.isExternalStorageWritable())
            return; // TODO cry for some help...

        Util.checkDirs();
        try {

            // TODO CHECK IF FILE ALREADY EXISTED!! IF THEN DON'T APPEND!
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + Util.fileDir, "wear_" + fileName);
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
        } catch (IOException e) {
            e.printStackTrace();
        }

        // decode the stream into a bitmap
        // return BitmapFactory.decodeStream(assetInputStream);
    }



    @Override
    public void onDestroy(){
        if(mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Failed to connect to Google Api Client");
    }

}

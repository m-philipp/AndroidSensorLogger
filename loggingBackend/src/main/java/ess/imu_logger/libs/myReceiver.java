package ess.imu_logger.libs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ess.imu_logger.libs.data_zip_upload.ZipUploadService;

public class myReceiver extends BroadcastReceiver {

    private static final String TAG = "ess.imu_logger.libs.myReceiver";

    public myReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.



        if (intent != null) {
            String action = intent.getAction();

            if (action.equals(intent.ACTION_BOOT_COMPLETED)) {
                // TODO magic boot starting
                /*
			      Intent serviceIntent = new Intent(context, MySystemService.class);
                  context.startService(serviceIntent);
			     */
            } else if (action.equals(ZipUploadService.ACTION_START_SERVICE)) {

                Intent mServiceIntent = new Intent(context, ZipUploadService.class);
                mServiceIntent.setAction(ZipUploadService.ACTION_START_SERVICE);
                context.startService(mServiceIntent);

            } else if (action.equals(ZipUploadService.ACTION_START_ZIPPER_ONLY)) {

                Intent mServiceIntent = new Intent(context, ZipUploadService.class);
                mServiceIntent.setAction(ZipUploadService.ACTION_START_ZIPPER_ONLY);
                context.startService(mServiceIntent);

            } else if (action.equals(TransferDataAsAssets.ACTION_TRANSFER)) {

                Intent mServiceIntent = new Intent(context, TransferDataAsAssets.class);
                mServiceIntent.setAction(TransferDataAsAssets.ACTION_TRANSFER);
                context.startService(mServiceIntent);

            } else {
                Log.d(TAG, action);
            }
        }
    }
}

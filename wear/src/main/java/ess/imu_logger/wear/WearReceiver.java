package ess.imu_logger.wear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import ess.imu_logger.libs.TransferDataAsAssets;
import ess.imu_logger.libs.Util;
import ess.imu_logger.libs.WearableMessageSenderService;
import ess.imu_logger.libs.data_zip_upload.ZipUploadService;

public class WearReceiver extends BroadcastReceiver {

    private static final String TAG = "ess.imu_logger.wear.wearReceiver";
    SharedPreferences sharedPrefs;

    public WearReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.


        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (intent != null) {
            String action = intent.getAction();

            if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {

                Log.d(TAG, "received boot complete");

                if(sharedPrefs.getBoolean(Util.PREFERENCES_START_ON_BOOT, false)) {

                    Intent startActivityIntent = new Intent(context, WearStartActivity.class);
                    startActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(startActivityIntent);

                }

            } else if (action.equals(ZipUploadService.ACTION_START_ZIPPER_ONLY)) { // WEAR

                Log.d(TAG, "received ACTION_START_ZIPPER_ONLY");

                Intent mServiceIntent = new Intent(context, ZipUploadService.class);
                mServiceIntent.setAction(ZipUploadService.ACTION_START_ZIPPER_ONLY);
                context.startService(mServiceIntent);

            } else if (action.equals(TransferDataAsAssets.ACTION_TRANSFER)) { // WEAR

                Log.d(TAG, "received ACTION_TRANSFER");


                Intent mServiceIntent = new Intent(context, TransferDataAsAssets.class);
                mServiceIntent.setAction(TransferDataAsAssets.ACTION_TRANSFER);
                context.startService(mServiceIntent);

            } else if (action.equals(Util.ACTION_PERIODIC_ALARM)) { // WEAR

                Log.d(TAG, "received ACTION_PERIODIC_ALARM");

                WearUtil.updateLoggingState(context, sharedPrefs);

            } else {

                Log.d(TAG, "received something ELSE");

                Log.d(TAG, action);
            }
        }
    }
}

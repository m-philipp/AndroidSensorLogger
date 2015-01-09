package ess.imu_logger.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import ess.imu_logger.libs.Util;
import ess.imu_logger.libs.WearableMessageSenderService;
import ess.imu_logger.libs.data_zip_upload.ZipUploadService;

public class PhoneReceiver extends BroadcastReceiver {

    private static final String TAG = "ess.imu_logger.app.myReceiver";
    SharedPreferences sharedPrefs;

    public PhoneReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.


        if (intent != null) {
            String action = intent.getAction();

            if (action.equals(intent.ACTION_BOOT_COMPLETED)) {

                Log.d(TAG, "received boot complete");

                if (sharedPrefs.getBoolean(Util.PREFERENCES_START_ON_BOOT, false)) {

                    Intent startActivityIntent = new Intent(context, StartScreen.class);
                    startActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(startActivityIntent);

                }

            } else if (action.equals(ZipUploadService.ACTION_START_SERVICE)) { // PHONE

                Log.d(TAG, "received ACTION_START_SERVICE");

                Intent mServiceIntent = new Intent(context, ZipUploadService.class);
                mServiceIntent.setAction(ZipUploadService.ACTION_START_SERVICE);
                context.startService(mServiceIntent);

            } else if (action.equals(Util.ACTION_PERIODIC_ALARM)) { // PHONE

                Log.d(TAG, "received ACTION_PERIODIC_ALARM");

                sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
                PhoneUtil.updateLoggingState(context, sharedPrefs);


                // TODO start smartphone services
                // TODO maybe first just start the Service, wait an then just start the Action through the Service Handler

                Intent sendPrefsIntent = new Intent(context, WearableMessageSenderService.class);
                sendPrefsIntent.setAction(WearableMessageSenderService.ACTION_SEND_PREFERENCES);
                context.startService(sendPrefsIntent);

                Intent stopIntent = new Intent(context, WearableMessageSenderService.class);
                stopIntent.setAction(WearableMessageSenderService.ACTION_STOP_SERVICE);
                context.startService(stopIntent);

            } else {

                Log.d(TAG, "received something ELSE");

                Log.d(TAG, action);
            }
        }
    }
}

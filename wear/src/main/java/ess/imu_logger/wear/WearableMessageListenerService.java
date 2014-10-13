package ess.imu_logger.wear;

/**
 * Created by martin on 09.09.2014.
 */

import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import ess.imu_logger.libs.Util;


/**
 * Listens for a message telling it to start the Wearable MainActivity.
 */
public class WearableMessageListenerService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent event) {
        if (event.getPath().equals(Util.GAC_PATH_TEST_ACTIVITY)) {
            Toast.makeText(this, "Hello from Phone!", Toast.LENGTH_LONG).show();
        } else if (event.getPath().equals(Util.GAC_PATH_ANNOTATE_SMOKING_ACTIVITY)) {
            Intent startIntent = new Intent(this, AnnotateSmoking.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        }
    }
}

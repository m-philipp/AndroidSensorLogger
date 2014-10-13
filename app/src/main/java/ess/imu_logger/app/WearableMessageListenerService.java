package ess.imu_logger.app;

/**
 * Created by martin on 09.09.2014.
 */

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import ess.imu_logger.libs.Util;

/**
 * Listens for a message telling it to start the Wearable MainActivity.
 */
public class WearableMessageListenerService extends WearableListenerService {

    private static final String TAG = "ess.imu_logger.app.WearableMessageListenerService";

    @Override
    public void onMessageReceived(MessageEvent event) {
        if (event.getPath().equals(Util.GAC_PATH_TEST_ACTIVITY)) {
            Toast.makeText(this, "Hello from Wearable!", Toast.LENGTH_LONG).show();
        }
    }


}

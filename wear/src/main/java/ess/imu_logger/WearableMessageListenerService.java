package ess.imu_logger;

/**
 * Created by martin on 09.09.2014.
 */
import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Listens for a message telling it to start the Wearable MainActivity.
 */
public class WearableMessageListenerService extends WearableListenerService {
	private static final String ANNOTATE_SMOKING_ACTIVITY_PATH = "/annotate-smoking";

	@Override
	public void onMessageReceived(MessageEvent event) {
		if (event.getPath().equals(ANNOTATE_SMOKING_ACTIVITY_PATH)) {
			Intent startIntent = new Intent(this, AnnotateSmoking.class);
			startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(startIntent);
		}
	}
}

package ess.imu_logger.wear;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.os.Bundle;
import android.support.wearable.view.DelayedConfirmationView;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class AnnotateSmoking extends Activity implements
		DelayedConfirmationView.DelayedConfirmationListener,
		GoogleApiClient.OnConnectionFailedListener {


	private static final String TAG = "ess.imu_Logger.wear.AnnotateSmoking";
	private static final int NUM_SECONDS = 5;

	private static final String TIMER_SELECTED_PATH = "/timer_selected";
	private static final String TIMER_FINISHED_PATH = "/timer_finished";

	private DelayedConfirmationView delayedConfirmationView;
	private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annotate_smoking);

	    delayedConfirmationView = (DelayedConfirmationView) findViewById(R.id.delayed_confirmation);
	    delayedConfirmationView.setTotalTimeMs(NUM_SECONDS * 1000);
	    mGoogleApiClient = new GoogleApiClient.Builder(this)
			    .addApi(Wearable.API)
			    .addOnConnectionFailedListener(this)
			    .build();
    }


	@Override
	protected void onResume() {
		super.onResume();
		if (!mGoogleApiClient.isConnected()) {
			mGoogleApiClient.connect();
		}
	}

	@Override
	protected void onDestroy() {
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
		super.onDestroy();
	}

	/**
	 * Starts the DelayedConfirmationView when user presses "Start Timer" button.
	 */
	public void onStartTimer(View view) {
		Log.d(TAG, "timer started");

		delayedConfirmationView.start();
		delayedConfirmationView.setListener(this);
	}

	@Override
	public void onTimerSelected(View v) {
		v.setPressed(true);

		Notification notification = new Notification.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(getString(R.string.notification_title))
				.setContentText(getString(R.string.notification_timer_selected))
				.build();
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(0, notification);
		sendMessageToCompanion(TIMER_SELECTED_PATH);



		// Prevent onTimerFinished from being heard.
		((DelayedConfirmationView) v).setListener(null);

		finish();
	}

	@Override
	public void onTimerFinished(View v) {
		Notification notification = new Notification.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(getString(R.string.notification_title))
				.setContentText(getString(R.string.notification_timer_finished))
				.build();
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(0, notification);
		sendMessageToCompanion(TIMER_FINISHED_PATH);

		finish();
	}

	private void sendMessageToCompanion(final String path) {
		Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
				new ResultCallback<NodeApi.GetConnectedNodesResult>() {
					@Override
					public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
						for (final Node node : getConnectedNodesResult.getNodes()) {
							Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path,
									new byte[0]).setResultCallback(getSendMessageResultCallback());
						}
					}
				}
		);

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.e(TAG, "Failed to connect to Google Api Client");
	}



	private ResultCallback<MessageApi.SendMessageResult> getSendMessageResultCallback() {
		return new ResultCallback<MessageApi.SendMessageResult>() {
			@Override
			public void onResult(MessageApi.SendMessageResult sendMessageResult) {
				if (!sendMessageResult.getStatus().isSuccess()) {
					Log.e(TAG, "Failed to connect to Google Api Client with status "
							+ sendMessageResult.getStatus());
				}
			}
		};
	}
}

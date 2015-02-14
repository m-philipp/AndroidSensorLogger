package de.smart_sense.tracker.wear;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DelayedConfirmationView;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import de.smart_sense.tracker.libs.Util;
import de.smart_sense.tracker.libs.data_save.SensorDataSavingService;

import static de.smart_sense.tracker.wear.WearUtil.updateLoggingState;

public class Annotate extends Activity implements
		DelayedConfirmationView.DelayedConfirmationListener,
		GoogleApiClient.OnConnectionFailedListener {


	private static final String TAG = "de.smart_sense.tracker.wear.Annotate";
	private static final int NUM_SECONDS = 3;


	private DelayedConfirmationView delayedConfirmationView;
	private GoogleApiClient mGoogleApiClient;


    protected SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_annotate_smoking);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        final Annotate me = this;

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub watchViewStub) {
                delayedConfirmationView = (DelayedConfirmationView) findViewById(R.id.delayed_confirmation);
                delayedConfirmationView.setListener(me);

                delayedConfirmationView.setTotalTimeMs(NUM_SECONDS * 1000);
                delayedConfirmationView.start();
            }
        });



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



    Boolean timerCanceled = false;

	@Override
	public void onTimerSelected(View v) {
		v.setPressed(true);

        /*
		Notification notification = new Notification.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(getString(R.string.annotateSmoking_finished_notification_title))
				.setContentText(getString(R.string.annotateSmoking_finished_notification_timer_selected))
				.build();
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(0, notification);

		// Prevent onTimerFinished from being heard.
		((DelayedConfirmationView) v).setListener(null);
        */

        Log.d(TAG, "time selected");

        timerCanceled = true;

        finish();


	}

	@Override
	public void onTimerFinished(View v) {

        if(timerCanceled) {
            finish();
            return;
        }

        v.setPressed(false);

        Log.d(TAG, "time finished");

        sendMessageToCompanion(Util.GAC_PATH_ANNOTATED);



        if(sharedPrefs.getBoolean(Util.PREFERENCES_WEAR_TEMP_LOGGING, false)) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(Util.PREFERENCES_LAST_ANNOTATION, String.valueOf(System.currentTimeMillis()));
            editor.commit();

            updateLoggingState(this, sharedPrefs, true);
        }
        // TODO make this (evtl.) an else part
        Intent sendIntent = new Intent(SensorDataSavingService.BROADCAST_ANNOTATION);
        sendIntent.putExtra(SensorDataSavingService.EXTRA_ANNOTATION_NAME, sharedPrefs.getString(Util.PREFERENCES_ANNOTATION_NAME, "smoking"));
        sendBroadcast(sendIntent);


        // show confirmation screen
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE,
                //getString(R.string.msg_sent));
                "gespeichert");
        startActivity(intent);

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
				} else {
                    Log.d(TAG, "Successfully connected to Google Api Client.");
                }
			}
		};
	}
}

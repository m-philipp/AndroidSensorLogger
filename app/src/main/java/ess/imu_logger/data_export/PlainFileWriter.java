package ess.imu_logger.data_export;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Created by martin on 28.08.2014.
 */
public class PlainFileWriter extends Thread {

	private static final String TAG = PlainFileWriter.class.getSimpleName();

	public static final int MESSAGE_WRITE = 0;

	public static final String MESSAGE_TYPE_ACTION = "ess.imu_logger.data_export.MESSAGE_TYPE_ACTION";
	public static final int MESSAGE_ACTION_SAVE = 0;
	public static final int MESSAGE_ACTION_UPLOAD = 1;

	public static final String MESSAGE_DATA = "ess.imu_logger.data_export.MESSAGE_DATA";

	private Handler outHandler;
	private Handler inHandler;

	public PlainFileWriter(Handler handler) {

		this.outHandler = handler;

	}


	public void run() {
		try {
			// preparing a looper on current thread
			// the current thread is being detected implicitly
			Looper.prepare();

			// now, the handler will automatically bind to the
			// Looper that is attached to the current thread
			// You don't need to specify the Looper explicitly
			inHandler = new Handler() {
				public void handleMessage(Message msg) {
					// Act on the message received from my UI thread doing my stuff

					if(msg.getData().getInt(MESSAGE_TYPE_ACTION) == MESSAGE_ACTION_SAVE){

						Log.i("SensorDataSavingService", "saving some Data");

					} else if(msg.getData().getInt(MESSAGE_TYPE_ACTION) == MESSAGE_ACTION_UPLOAD){

						Log.i("SensorDataSavingService", "uploading some Data");

					}
				}
			};

			// After the following line the thread will start
			// running the message loop and will not normally
			// exit the loop unless a problem happens or you
			// quit() the looper (see below)
			Looper.loop();

			Log.i(TAG, "DownloadThread exiting gracefully");
		} catch (Throwable t) {
			Log.e(TAG, "DownloadThread halted due to an error", t);
		}
	}

	// This method is allowed to be called from any thread
	public synchronized void requestStop() {
		// using the handler, post a Runnable that will quit()
		// the Looper attached to our DownloadThread
		// obviously, all previously queued tasks will be executed
		// before the loop gets the quit Runnable
		inHandler.post(new Runnable() {
			@Override
			public void run() {
				// This is guaranteed to run on the DownloadThread
				// so we can use myLooper() to get its looper
				Log.i(TAG, "DownloadThread loop quitting by request");

				Looper.myLooper().quit();
			}
		});
	}


	public synchronized void saveString(final String data) {
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putInt(MESSAGE_TYPE_ACTION, MESSAGE_ACTION_SAVE);
		b.putString(MESSAGE_DATA, data);
		msg.setData(b);

		// could be a runnable when calling post instead of sendMessage
		inHandler.sendMessage(msg);
	}

	public synchronized void test2(final String test) {
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putString("key", test);
		msg.setData(b);

		// could be a runnable when calling post instead of sendMessage
		inHandler.sendMessage(msg);
	}


}
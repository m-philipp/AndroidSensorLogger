package ess.imu_logger.app.data_zip_upload;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import ess.imu_logger.app.Util;
import ess.imu_logger.app.data_save.PlainFileWriter;

/**
 * Created by martin on 15.09.2014.
 */
public class Uploader extends Thread {


	private static final String TAG = "ess.imu_logger.app.data_zip_upload.Uploader";

	public static final String MESSAGE_TYPE_ACTION = "ess.imu_logger.app.data_zip_upload.MESSAGE_TYPE_ACTION";

	public static final int MESSAGE_ACTION_UPLOAD = 0;
	public static final int MESSAGE_ACTION_RETURNING_UPLOAD = 1;

	private boolean messageInQueue = false;

	private Handler inHandler;

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


					if(msg.getData().getInt(MESSAGE_TYPE_ACTION) == MESSAGE_ACTION_UPLOAD ||
							msg.getData().getInt(MESSAGE_TYPE_ACTION) == MESSAGE_ACTION_RETURNING_UPLOAD){

						// TODO Magic
						getFilenameToUpload();
					}

					if(msg.getData().getInt(MESSAGE_TYPE_ACTION) == MESSAGE_ACTION_RETURNING_UPLOAD ||
							(msg.getData().getInt(MESSAGE_TYPE_ACTION) == MESSAGE_ACTION_UPLOAD && !messageInQueue)) {

						Message m = new Message();
						Bundle b = new Bundle();
						b.putInt(MESSAGE_TYPE_ACTION, MESSAGE_ACTION_RETURNING_UPLOAD);
						m.setData(b);

						// could be a runnable when calling post instead of sendMessage
						inHandler.sendMessageDelayed(m, 2000);
						messageInQueue = true;
					}
				}
			};

			// After the following line the thread will start
			// running the message loop and will not normally
			// exit the loop unless a problem happens or you
			// quit() the looper (see below)
			Looper.loop();

			Log.i(TAG, "Upload Thread exiting gracefully");
		} catch (Throwable t) {
			Log.e(TAG, "Upload Thread halted due to an error", t);
		}
	}


	private String getFilenameToUpload(){
		File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + File.separator + Util.fileDir);
		String[] listOfFiles = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".zip") ? true : false;
			}
		});

		Arrays.sort(listOfFiles);

		if(listOfFiles.length > 1){
			return dir.getAbsolutePath() + File.separator + listOfFiles[0]; // TODO check if we didn't take the newest file.
		}

		return null;

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
				Log.i(TAG, "Upload Thread loop quitting by request");

				Looper.myLooper().quit();
			}
		});
	}

	public synchronized void uo() {
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putInt(MESSAGE_TYPE_ACTION, MESSAGE_ACTION_UPLOAD);
		msg.setData(b);

		// could be a runnable when calling post instead of sendMessage
		inHandler.sendMessage(msg);
	}

}

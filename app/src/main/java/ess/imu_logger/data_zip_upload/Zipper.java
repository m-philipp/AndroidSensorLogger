package ess.imu_logger.data_zip_upload;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import ess.imu_logger.Util;
import ess.imu_logger.data_save.PlainFileWriter;

/**
 * Created by martin on 15.09.2014.
 */
public class Zipper extends Thread {

	private static final String TAG = "ess.imu_logger.data_zip_upload.Zipper";

	public static final String MESSAGE_TYPE_ACTION = "ess.imu_logger.data_zip_upload.MESSAGE_TYPE_ACTION";

	public static final int MESSAGE_ACTION_ZIP = 0;

	private Handler inHandler;

	private ZipUploadService zus;

	public Zipper(Context context){
		zus = (ZipUploadService) context;
	}


	public void run() {
		try {
			// preparing a looper on current thread
			// the current thread is being detected implicitly
			Looper.prepare();

			synchronized (this) {
				// now, the handler will automatically bind to the
				// Looper that is attached to the current thread
				// You don't need to specify the Looper explicitly
				inHandler = new Handler() {
					public void handleMessage(Message msg) {
						// Act on the message received from my UI thread doing my stuff


						if (msg.getData().getInt(MESSAGE_TYPE_ACTION) == MESSAGE_ACTION_ZIP) {


							// TODO remove
							//SystemClock.sleep(2000);

							if (!Util.isExternalStorageWritable())
								return; // TODO cry for some help...
							Util.checkDirs();

							Log.i(TAG, "processing ZIPPPPING Message.");

							// zip DB
							String sourceFile = getFilenameToZip();

							while (sourceFile != null) {

								String exportFilePath = sourceFile.substring(0, sourceFile.length() - PlainFileWriter.fileExtension.length()) + ".zip";

								Log.d(TAG, "exportFilePath: " + exportFilePath);
								Log.d(TAG, "sourceFile: " + sourceFile);

								Compress zipper = new Compress(sourceFile, exportFilePath);
								if (zipper.zip()) {
									Log.d(TAG, "Zip file size: " + (new File(exportFilePath)).length() + " Bytes");

									File file = new File(sourceFile);
									boolean deleted = file.delete();

								}


								sourceFile = getFilenameToZip();
								//break;
							}


						}


						zus.zipperStopped();
						Looper.myLooper().quit();

					}
				};
				notifyAll();}


			// After the following line the thread will start
			// running the message loop and will not normally
			// exit the loop unless a problem happens or you
			// quit() the looper (see below)
			Looper.loop();

			Log.i(TAG, "Zipper Thread exiting gracefully");
		} catch (Throwable t) {
			Log.e(TAG, "Zipper Thread halted due to an error", t);
		}
	}


	private String getFilenameToZip(){
		File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + File.separator + Util.fileDir);
		String[] listOfFiles = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return filename.endsWith(PlainFileWriter.fileExtension) ? true : false;
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

        getHandler().post(new Runnable() {
			@Override
			public void run() {
				// This is guaranteed to run on the DownloadThread
				// so we can use myLooper() to get its looper
				Log.i(TAG, "Zipper Thread loop quitting by request");

				Looper.myLooper().quit();
			}
		});
	}

	public synchronized void zip() {
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putInt(MESSAGE_TYPE_ACTION, MESSAGE_ACTION_ZIP);
		msg.setData(b);

		// could be a runnable when calling post instead of sendMessage
		getHandler().sendMessage(msg);
	}


	private Handler getHandler() {
		while (inHandler == null) {
			try {
				wait();
			} catch (InterruptedException e) {
				//Ignore and try again.
			}
		}
		return inHandler;
	}

}

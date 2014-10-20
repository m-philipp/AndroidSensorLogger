package ess.imu_logger.libs;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import ess.imu_logger.libs.BasicLogger;

public class DataCollectorService extends Service {
	
	public static final String TAG = "DataCollectorService";
	
	public static final int SENSOR_FREQUENCY_DELAY = 20000; 
	
	private boolean collectionStarted = false;
	
	private final IBinder mBinder = new MyBinder();

    private BasicLogger serviceHandler;
	private HandlerThread thread;
	private Looper serviceLooper;
	SharedPreferences sharedPrefs;
	



	@Override
	public void onCreate() {
		sharedPrefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

		// start worked thread
		thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_FOREGROUND);
		thread.start();
		Log.v(TAG, "HandlerThread started.");
	    
		// Get the HandlerThread's Looper and use it for our Handler 
		serviceLooper = thread.getLooper();
        int logging_frequency = Integer.parseInt(sharedPrefs.getString("sampling_rate", "0"));

        serviceHandler = new BasicLogger(serviceLooper, this, logging_frequency, 10000, 1080);
		
		Log.d(TAG, TAG + " started");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {	

        startRecording(intent);

		
		return START_STICKY;
	}

	@Override
	public void onDestroy() {

		stopRecording();

		thread.quit();

	}
	
	public synchronized void startAnnotation(String activity, long timestamp, int inputType) {
		Log.d(TAG, "Annotation Start: " + activity);

	}
	
	public synchronized void endAnnotation(String activity, long timestamp, int inputType) {
		Log.d(TAG, "Annotation End: " + activity);
		

	}
	
	private synchronized void startRecording(Intent intent){
		if (intent == null) {
			intent = new Intent();
		}
		
		if(!collectionStarted){	
			// add sensor preferences
	        intent.putExtra("gyroscope", sharedPrefs.getBoolean("gyroscope", false));
	        intent.putExtra("accelerometer", sharedPrefs.getBoolean("accelerometer", true));
	        intent.putExtra("magneticField", sharedPrefs.getBoolean("magneticField", false));
	        intent.putExtra("rotation", sharedPrefs.getBoolean("rotation", false));
	        intent.putExtra("linearAccelerometer", sharedPrefs.getBoolean("linearAccelerometer", false));
	        intent.putExtra("gravity", sharedPrefs.getBoolean("gravity", false));
	        intent.putExtra("ambientLight", sharedPrefs.getBoolean("ambientLight", false));
	        intent.putExtra("proximity", sharedPrefs.getBoolean("proximity", false));
	        intent.putExtra("temperature", sharedPrefs.getBoolean("temperature", false));
	        intent.putExtra("humidity", sharedPrefs.getBoolean("humidity", false));
	        intent.putExtra("pressure", sharedPrefs.getBoolean("pressure", false));
	        intent.putExtra("time", SystemClock.elapsedRealtime() + 1000 * 86400 * 14);
		
			
			startSession();


			// send message to BasicLogger
			Message msg = serviceHandler.obtainMessage();
			msg.what = BasicLogger.MESSAGE_INIT;
			msg.obj = intent;
			serviceHandler.sendMessage(msg);
		}
		collectionStarted = true;
	}
	
	private synchronized void stopRecording() {
		endSession();
		
		serviceHandler.sendEmptyMessage(BasicLogger.MESSAGE_FINISH);
	}
	
	private void startSession() {
	}
	
	private void endSession() {
	}
	
	
	private void exportDb(boolean pauseThread) {
		long unixTime = System.currentTimeMillis();
		
		// pause logger
		if (pauseThread) {
			serviceHandler.sendEmptyMessage(BasicLogger.MESSAGE_PAUSE);
		}
		
		Log.d(TAG+" export", "Start zipping");
		// zip DB

		// resume logger
		if (pauseThread) {
			serviceHandler.sendEmptyMessage(BasicLogger.MESSAGE_RESUME);
		} 
		


	}


	private void logEvent(int action) {

        // TODO log event
	}
    
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder; 
	}

	public class MyBinder extends Binder {
		public DataCollectorService getService() {
			return DataCollectorService.this;
		}

	}
	

	
}

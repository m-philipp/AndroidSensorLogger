package ess.imu_logger.libs;

import java.util.concurrent.ConcurrentLinkedQueue;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import ess.imu_logger.libs.data_save.SensorDataSavingService;
import ess.imu_logger.libs.logging.Logger;

//Handler that receives messages from the thread
final class BasicLogger extends Handler implements SensorEventListener{
    
	private static final String TAG = "BasicLogger";
	public static final int MESSAGE_INIT = 1;
	public static final int MESSAGE_COMMIT = 0;
	public static final int MESSAGE_FINISH = -1;
	public static final int MESSAGE_PAUSE = 2;
	public static final int MESSAGE_RESUME = 3;
	
	private Service c;
	private ConcurrentLinkedQueue<ContentValues> queue;
	private Sensor gyroscopeSensor, accelerometerSensor, magneticFieldSensor, rotationSensor, linearAccelerometerSensor, gravitySensor, ambientLightSensor, proximitySensor, temperatureSensor, humiditySensor, pressureSensor;
	private SensorManager mSensorManager;
	
	private IntentFilter ifilter;

	//private WakeLock wl;
	
	int transaction_delay, logging_frequency, export_interval;
	//FIXME - this is incredibly stupid!
	long sessionId, time, counter;
	
	boolean paused = false;
	
	public BasicLogger(Looper looper, Service c, int logging_frequency, int transaction_delay, int export_interval) {
        super(looper);
        this.c = c;

        this.logging_frequency = logging_frequency; //SensorManager.SENSOR_DELAY_FASTEST;
        this.transaction_delay = transaction_delay; 
        this.export_interval = export_interval;
        
        //this.counter = DatabaseUtils.queryNumEntries(db, DatabaseConfiguration.TransactionLog.TABLE_NAME);
        
        Log.v(TAG, "Logging frequency: " + 1e6/this.logging_frequency + " Hz. Transaction delay: " + transaction_delay/1000 + " Sec.");
        
        queue = new ConcurrentLinkedQueue<ContentValues>();
        Log.v(TAG,"SensorEvent queue created.");
        
        mSensorManager = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
        gyroscopeSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticFieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        rotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        linearAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        gravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        ambientLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        proximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) temperatureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        else temperatureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
        humiditySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        pressureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        
        ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Log.v(TAG, "Battery listener registered.");
        

        Log.v(TAG, "Instance created.");
        
        //PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
    	//wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "local");
    	
    }
	
    @Override
    public void handleMessage(Message msg) { 
    	//wl.acquire();
    	if(msg.what == MESSAGE_INIT){
    		Log.v(TAG, "Entered handleMessage.");
    	       
    		Intent i = (Intent) msg.obj;
    		Log.v(TAG, "Recovered intent " + i + ".");
            
    		if(gyroscopeSensor != null && i.getBooleanExtra("gyroscope", false)) mSensorManager.registerListener(this, gyroscopeSensor, logging_frequency);
    		if(accelerometerSensor != null && i.getBooleanExtra("accelerometer", false)) mSensorManager.registerListener(this, accelerometerSensor, logging_frequency);
    		if(magneticFieldSensor != null && i.getBooleanExtra("magneticField", false)) mSensorManager.registerListener(this, magneticFieldSensor, logging_frequency);
    		if(rotationSensor != null && i.getBooleanExtra("rotation", false)) mSensorManager.registerListener(this, rotationSensor, logging_frequency);
    		if(linearAccelerometerSensor != null && i.getBooleanExtra("linearAccelerometer", false)) mSensorManager.registerListener(this, linearAccelerometerSensor, logging_frequency);
    		if(gravitySensor != null && i.getBooleanExtra("gravity", false)) mSensorManager.registerListener(this, gravitySensor, logging_frequency);
    		if(ambientLightSensor != null && i.getBooleanExtra("ambientLight", false)) mSensorManager.registerListener(this, ambientLightSensor, logging_frequency);
    		if(proximitySensor != null && i.getBooleanExtra("proximity", false)) mSensorManager.registerListener(this, proximitySensor, logging_frequency);
    		if(temperatureSensor != null && i.getBooleanExtra("temperature", false)) mSensorManager.registerListener(this, temperatureSensor, logging_frequency);
    		if(humiditySensor != null && i.getBooleanExtra("humidity", false)) mSensorManager.registerListener(this, humiditySensor, logging_frequency);
    		if(pressureSensor != null && i.getBooleanExtra("pressure", false)) mSensorManager.registerListener(this, pressureSensor, logging_frequency);    
    		Log.v(TAG, "Sensors registered.");
    		
    		sessionId = i.getLongExtra("sessionId", 0);
    		time = i.getLongExtra("time", 0);
    		Log.v(TAG, "Service scheduled to run until " + time + ".");
    		
    		this.sendEmptyMessageDelayed(MESSAGE_COMMIT, transaction_delay);
    	} else if(msg.what == MESSAGE_COMMIT){
    		if (this.paused) {
    			// try again in a while
    			this.sendEmptyMessageDelayed(MESSAGE_COMMIT, 500);
    		} else {
	    		purgeQueueToDB();
	    		this.sendEmptyMessageDelayed(MESSAGE_COMMIT, transaction_delay);
    		}
    	} else if(msg.what == MESSAGE_PAUSE){
    		this.paused = true;
    		Log.v(TAG, "Logger paused.");
    	} else if(msg.what == MESSAGE_RESUME){
    		this.paused = false;
    		Log.v(TAG, "Logger resumed.");
    	} else if(msg.what == MESSAGE_FINISH){
    		if (this.paused) {
    			// try again in a while
    			this.sendEmptyMessageDelayed(MESSAGE_FINISH, 500);
    		} else {
    			this.removeMessages(0);
	    		mSensorManager.unregisterListener(this);
	    		Log.v(TAG, "Sensors unregistered.");
	            purgeQueueToDB();
	           Log.v(TAG, "SensorEvent queue empty.");
	                		}
    	} 
    	//wl.release();
    }
    
	@Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
    	// Many sensors return 3 values, one for each axis.


        //Log.d(TAG, "sensor event...");


        Intent intent = new Intent(SensorDataSavingService.BROADCAST_SENSOR_DATA);
        intent.putExtra(SensorDataSavingService.EXTRA_SENSOR_DATA, Logger.getString(event));
        LocalBroadcastManager.getInstance(c).sendBroadcast(intent);
    	
        /*
        ContentValues values = new ContentValues();

        values.put(DatabaseConfiguration.EventLog.COLUMN_NAME_1, event.sensor.getType());
        values.put(DatabaseConfiguration.EventLog.COLUMN_NAME_2, event.values[0]);
        if(event.values.length > 1) values.put(DatabaseConfiguration.EventLog.COLUMN_NAME_3, event.values[1]);
        if(event.values.length > 2) values.put(DatabaseConfiguration.EventLog.COLUMN_NAME_4, event.values[2]);
        if(event.values.length > 3) values.put(DatabaseConfiguration.EventLog.COLUMN_NAME_5, event.values[3]);
        values.put(DatabaseConfiguration.EventLog.COLUMN_NAME_6, event.accuracy);
        values.put(DatabaseConfiguration.EventLog.COLUMN_NAME_7, event.timestamp/1000);

        queue.add(values);
        */

    }
    
    private void purgeQueueToDB(){
        /*
    	long t_start = SystemClock.elapsedRealtime();
   		long t_synch = SystemClock.uptimeMillis();
   		
   		if(Utils.LOG) Log.v(TAG, "Writing " + queue.size() + " items");   
   			
   		Intent batteryStatus = c.registerReceiver(null, ifilter);
   		int chargeStatus = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
   		int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
   		int batteryPct = (batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100) / batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
   			
   		db.beginTransaction();
   		stmt_transaction.bindLong(1, sessionId);
   		stmt_transaction.bindLong(2, t_start);
   		stmt_transaction.bindLong(3, t_synch);
   		stmt_transaction.bindLong(4, chargeStatus);
   		stmt_transaction.bindLong(5, chargePlug);
   		stmt_transaction.bindLong(6, batteryPct);
   		long transactionLog = stmt_transaction.executeInsert();
   		stmt_transaction.clearBindings();
   	    while(true){
   	        ContentValues values = queue.poll();
   	        if(values == null) break;
   	        stmt_event.bindLong(1, transactionLog);
   	        if(values.containsKey(DatabaseConfiguration.EventLog.COLUMN_NAME_1))
   	        stmt_event.bindString(2, values.getAsString(DatabaseConfiguration.EventLog.COLUMN_NAME_1));
   	        if(values.containsKey(DatabaseConfiguration.EventLog.COLUMN_NAME_2))
   	        stmt_event.bindString(3, values.getAsString(DatabaseConfiguration.EventLog.COLUMN_NAME_2));
   	        if(values.containsKey(DatabaseConfiguration.EventLog.COLUMN_NAME_3))
   	        stmt_event.bindString(4, values.getAsString(DatabaseConfiguration.EventLog.COLUMN_NAME_3));
   	        if(values.containsKey(DatabaseConfiguration.EventLog.COLUMN_NAME_4))
   	        stmt_event.bindString(5, values.getAsString(DatabaseConfiguration.EventLog.COLUMN_NAME_4));
   	        if(values.containsKey(DatabaseConfiguration.EventLog.COLUMN_NAME_5))
   	        stmt_event.bindString(6, values.getAsString(DatabaseConfiguration.EventLog.COLUMN_NAME_5));
   	        if(values.containsKey(DatabaseConfiguration.EventLog.COLUMN_NAME_6))
   	        stmt_event.bindString(7, values.getAsString(DatabaseConfiguration.EventLog.COLUMN_NAME_6));
   	        if(values.containsKey(DatabaseConfiguration.EventLog.COLUMN_NAME_7))
   	        stmt_event.bindString(8, values.getAsString(DatabaseConfiguration.EventLog.COLUMN_NAME_7));
   	        stmt_event.execute();
   	        stmt_event.clearBindings();       
   	    }
   	    db.setTransactionSuccessful();
   	    db.endTransaction();
   	 if(Utils.LOG) Log.v(TAG, "Transaction successful.");
        */
    }
}

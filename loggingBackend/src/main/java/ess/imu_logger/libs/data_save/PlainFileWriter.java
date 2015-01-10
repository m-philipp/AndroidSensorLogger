package ess.imu_logger.libs.data_save;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import ess.imu_logger.libs.Util;

/**
 * Created by martin on 28.08.2014.
 */
public class PlainFileWriter extends Thread {

    // private static final String TAG = PlainFileWriter.class.getSimpleName();
    private static final String TAG = "ess.imu_logger.libs.data_save.PlainFileWriter";


    public static final String MESSAGE_TYPE_ACTION = "ess.imu_logger.libs.data_save.MESSAGE_TYPE_ACTION";
    public static final int MESSAGE_ACTION_SAVE = 0;
    public static final int MESSAGE_ACTION_POLL = 1;

    public static final String MESSAGE_DATA = "ess.imu_logger.libs.data_save.MESSAGE_DATA";

    private Handler inHandler;
    private Boolean polling = false;

    public static final String fileExtension = ".log";

    private static final Integer MAX_FILE_SIZE = 1024*1024*1; // 1 MB



    public PlainFileWriter() {
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



                        if (msg.getData().getInt(MESSAGE_TYPE_ACTION) == MESSAGE_ACTION_POLL) {

                            Log.d(TAG, "polling");
                            writeSensorDataToFile();
                            startPolling(false);

                        } else if (msg.getData().getInt(MESSAGE_TYPE_ACTION) == MESSAGE_ACTION_SAVE) {

                            // TODO This is only for additional Data Generators like the Gas-Sensor
                            SensorDataSavingService.sensorEvents.add(msg.getData().getString(MESSAGE_DATA));

                        }
                    }
                };
                notifyAll();
            }

            // After the following line the thread will start
            // running the message loop and will not normally
            // exit the loop unless a problem happens or you
            // quit() the looper (see below)
            Looper.loop();

            Log.i(TAG, "Thread exiting gracefully");
        } catch (Throwable t) {
            Log.e(TAG, "Thread halted due to an error", t);
        }
    }

    private boolean writeSensorDataToFile() {
        if (!Util.isExternalStorageWritable())
            return false;

        Util.checkDirs();
        File file;
        FileOutputStream out = null;

        try {
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + Util.fileDir, getFilename());
            out = new FileOutputStream(file, true);
            while(!SensorDataSavingService.sensorEvents.isEmpty()){
                String sensorEvent = SensorDataSavingService.sensorEvents.poll();
                out.write(sensorEvent.getBytes(), 0, sensorEvent.length());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
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
                Log.i(TAG, "Thread loop quitting by request");

                Looper.myLooper().quit();
            }
        });
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

    public synchronized void saveString(final String data) {
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putInt(MESSAGE_TYPE_ACTION, MESSAGE_ACTION_SAVE);
        b.putString(MESSAGE_DATA, data);
        msg.setData(b);

        // could be a runnable when calling post instead of sendMessage
        getHandler().sendMessage(msg);
    }

    public synchronized void startPolling(Boolean initCall) {
        if(initCall && polling)
            return;
        polling = true;

        Message msg = new Message();
        Bundle b = new Bundle();
        b.putInt(MESSAGE_TYPE_ACTION, MESSAGE_ACTION_POLL);
        msg.setData(b);

        // TODO make this a constant
        getHandler().sendMessageDelayed(msg, 500);
    }

    public synchronized void startPolling() {
        startPolling(true);
    }


    private String getFilename() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + File.separator + Util.fileDir);
        String[] listOfFiles = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(fileExtension) ? true : false;
            }
        });

        if (listOfFiles.length < 1) {
            return String.valueOf(System.currentTimeMillis()) + fileExtension;
        }

        Arrays.sort(listOfFiles);


        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + Util.fileDir, listOfFiles[listOfFiles.length - 1]);
        if (file.length() > MAX_FILE_SIZE) {
            return String.valueOf(System.currentTimeMillis()) + fileExtension;
        }

        return listOfFiles[listOfFiles.length - 1];

    }


}
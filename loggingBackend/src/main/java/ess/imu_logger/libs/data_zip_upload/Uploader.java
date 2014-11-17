package ess.imu_logger.libs.data_zip_upload;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;

import ess.imu_logger.libs.URIEncoder;
import ess.imu_logger.libs.Util;

/**
 * Created by martin on 15.09.2014.
 */
public class Uploader extends Thread {


    private static final String TAG = "ess.imu_logger.libs.data_zip_upload.Uploader";

    public static final String MESSAGE_TYPE_ACTION = "ess.imu_logger.libs.data_zip_upload.MESSAGE_TYPE_ACTION";

    public static final int MESSAGE_ACTION_UPLOAD = 0;

    private SharedPreferences sharedPrefs;
    private ConnectivityManager connManager;
    private static String sID = null;

    private Handler inHandler;

    private ZipUploadService zus;

    public Uploader(Context context) {
        zus = (ZipUploadService) context;

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        sID = Util.id(context);
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

                        Log.d(TAG, "handle Message");


                        if (msg.getData().getInt(MESSAGE_TYPE_ACTION) == MESSAGE_ACTION_UPLOAD) {

                            Log.d(TAG, "received message to upload data");

                            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);


                            if ((!(mWifi == null) &&mWifi.isConnected()) ||
                                    !sharedPrefs.getBoolean("wifi_only", true)) {

                                Log.d(TAG, "now trying to upload date while concerning settings and WIFI State");

                                // Doing Magic
                                while (doingMagic()) ;

                            }
                        }

                        zus.uploaderStopped();
                        Looper.myLooper().quit();
                    }
                };
                notifyAll();
            }


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

    private boolean doingMagic() {
        try {
            String sourceFileName = getFilenameToUpload();
            if (sourceFileName != null) {

                File sourceFile = new File(sourceFileName);

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                DataOutputStream dos = null;
                //URL url = new URL("http://192.168.2.50:8080/upload");
                String name = "default";
                if (!sharedPrefs.getBoolean("anonymize", true))
                    name = sharedPrefs.getString("name", "default");


                name = URIEncoder.encodeURI(name);


                String u = sharedPrefs.getString("server_url", "http://192.168.2.50") + ":" +
                        sharedPrefs.getString("server_port", "8080") +
                        "/upload/" + sID + "/" + name;
                if (!u.startsWith("http://"))
                    u = "http://" + u;

                Log.i(TAG, "URL: " + u);
                URL url = new URL(u);


                String fileName = getFilenameToUpload(false);
                HttpURLConnection conn = null;
                String twoHyphens = "--";
                String boundary = "*****";
                String lineEnd = "\r\n";
                int maxBufferSize = 1 * 1024 * 1024;
                int bytesRead, bytesAvailable, bufferSize;
                byte[] buffer;
                int serverResponseCode = 0;

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("source", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"source\";filename=\""
                        + fileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode(); // TODO check that
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);


                //close the streams //

                fileInputStream.close();
                fileInputStream = null;

                dos.flush();
                dos.close();

                //System.gc();

                if (serverResponseCode == 200) {
                    Log.d(TAG, "UPLOAD OK");


                    // TODO check remove
                    File f = new File(sourceFileName);
                    f.setWritable(true);
                    if (f.delete()) {
                        Log.d(TAG, f.getName() + " is deleted!");
                    } else {
                        Log.d(TAG, f.getName() + " Delete operation is failed.");
                    }


                    if (!(getFilenameToUpload() == null)) {
                        Log.d(TAG, "Upload was OK an there is at least ONE MORE THING to upload.");

                        return true;
                    } else {
                        return false;
                    }

                }

                // Upload failed therefore we're trying to upload next Time.
                return false;

            } else {
                Log.d(TAG, "There's no File to Upload.");
                return false;
            }

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            Log.e(TAG, "error: " + ex.getMessage(), ex);
        } catch (ConnectException ex) {
            Log.d(TAG, "Host was not reachable."); // TODO reduce timeout ?
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception : " + e.getMessage(), e);
        }

        // on exceptions we're nor trying again.
        return false;
    }



    private String getFilenameToUpload() {
        return getFilenameToUpload(true);
    }

    private String getFilenameToUpload(Boolean full) {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + File.separator + Util.fileDir);
        String[] listOfFiles = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".zip") ? true : false;
            }
        });

        Arrays.sort(listOfFiles);

        if (listOfFiles.length > 1) {
            if (full)
                return dir.getAbsolutePath() + File.separator + listOfFiles[0]; // TODO check if we didn't take the newest file.
            return listOfFiles[0];
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
                Log.i(TAG, "Upload Thread loop quitting by request");

                Looper.myLooper().quit();
            }
        });
    }

    public synchronized void up() {
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putInt(MESSAGE_TYPE_ACTION, MESSAGE_ACTION_UPLOAD);
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

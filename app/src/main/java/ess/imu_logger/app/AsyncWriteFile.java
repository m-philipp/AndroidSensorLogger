package ess.imu_logger.app;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by martin on 19.08.2014.
 */
public class AsyncWriteFile extends AsyncTask<ArrayList<String>, Integer, Boolean> {

    private String fileExtension = ".log";
    private String dirname = "smokingStudy";
    private static final Integer MAX_FILE_SIZE = 655360;


    FileOutputStream outputStream;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();


    }

    protected Boolean doInBackground(ArrayList<String>... sensorValueQueue) {

        System.out.println("file writer task started...");
        checkDirs();

        if (isCancelled()) return false;

        if (!isExternalStorageWritable()) return false;


        for (int i = 0; i < sensorValueQueue.length; i++) {

            File file;
            FileOutputStream out = null;
            try {
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + dirname, getFilename());
                out = new FileOutputStream(file, true);

                for (int o = 0; o < sensorValueQueue[i].size(); o++) {
                    out.write((sensorValueQueue[i].get(o) + '\n').getBytes(), 0, sensorValueQueue[i].get(o).length());
                }


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {

                    if (out != null) {
                        out.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }


    protected void onProgressUpdate(String... progress) {
        // Set progress percentage

    }


    protected void onPostExecute(String file_url) {

    }

    private void checkDirs() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!dir.isDirectory()) {
            // mkdir Environment.DIRECTORY_DOCUMENTS
            dir.mkdir();
        }

        dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + File.separator + dirname);
        if (!dir.isDirectory()) {
            // mkdir Environment.DIRECTORY_DOCUMENTS + File.separator + dirname
            dir.mkdir();
        }
    }

    private String getFilename() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + File.separator + dirname);
        String[] listOfFiles = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".log") ? true : false;
            }
        });

        if (listOfFiles.length < 1) {
            return String.valueOf(System.currentTimeMillis()) + fileExtension;
        }

        Arrays.sort(listOfFiles);


        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + dirname, listOfFiles[listOfFiles.length - 1]);
        if (file.length() > MAX_FILE_SIZE) {
            return String.valueOf(System.currentTimeMillis()) + fileExtension;
        }

        return listOfFiles[listOfFiles.length - 1];

    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}

// new DownloadMusicfromInternet().execute(file_url);
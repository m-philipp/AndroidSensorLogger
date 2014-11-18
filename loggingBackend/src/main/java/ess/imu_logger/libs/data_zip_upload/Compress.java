package ess.imu_logger.libs.data_zip_upload;

/**
 * Created by martin on 15.09.2014.
 */

// http://www.jondev.net/articles/Zipping_Files_with_Android_%28Programmatically%29

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class Compress {

    private static final String TAG = "ess.imu_logger.libs.data_zip_upload.Compress";


    private static final int BUFFER = 2048;

    private String[] _files;
    private String _zipFile;

    public Compress(List<String> files, String zipFile) {
        _files = files.toArray(_files);
        _zipFile = zipFile;
    }

    public Compress(String file, String zipFile) {
        String[] t = {file};
        _files = t;
        _zipFile = zipFile;
    }

    public boolean zip() {
        try {

            Log.d(TAG, "trying to zip");

            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(_zipFile);

            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte data[] = new byte[BUFFER];

            for (int i = 0; i < _files.length; i++) {
                Log.v("Compress", "Adding: " + _files[i]);
                FileInputStream fi = new FileInputStream(_files[i]); // TODO catch NoSuchFile Exception
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

}
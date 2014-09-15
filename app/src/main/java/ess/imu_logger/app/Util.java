package ess.imu_logger.app;

import android.os.Environment;

import java.io.File;

/**
 * Created by martin on 15.09.2014.
 */
public class Util {

	public static final String fileDir = "smokingStudy";

	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	public static boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) ||
				Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}


	public static void checkDirs() {
		File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
		if (!dir.isDirectory()) {
			// mkdir Environment.DIRECTORY_DOCUMENTS
			dir.mkdir();
		}

		dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + File.separator + fileDir);
		if (!dir.isDirectory()) {
			// mkdir Environment.DIRECTORY_DOCUMENTS + File.separator + dirname
			dir.mkdir();
		}
	}

}

package ess.imu_logger;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

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

	public static float round(float valueToRound, int numberOfDecimalPlaces) {
		return (float) round((double) valueToRound, numberOfDecimalPlaces);
	}

	public static double round(double valueToRound, int numberOfDecimalPlaces)
	{
		double multiplicationFactor = Math.pow(10, numberOfDecimalPlaces);
		double interestedInZeroDPs = valueToRound * multiplicationFactor;
		return Math.round(interestedInZeroDPs) / multiplicationFactor;
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


	private static String sID = null;
	private static final String INSTALLATION = "INSTALLATION";

	public synchronized static String id(Context context) {
		if (sID == null) {
			File installation = new File(context.getFilesDir(), INSTALLATION);
			try {
				if (!installation.exists())
					writeInstallationFile(installation);
				sID = readInstallationFile(installation);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return sID;
	}

	private static String readInstallationFile(File installation) throws IOException {
		RandomAccessFile f = new RandomAccessFile(installation, "r");
		byte[] bytes = new byte[(int) f.length()];
		f.readFully(bytes);
		f.close();
		return new String(bytes);
	}

	private static void writeInstallationFile(File installation) throws IOException {
		FileOutputStream out = new FileOutputStream(installation);
		String id = UUID.randomUUID().toString();
		out.write(id.getBytes());
		out.close();
	}


    public static Long getFolderSize() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + File.separator + fileDir);
        return getFolderSize(dir);
    }

    public static Long getFolderSize(File dir) {


        if(dir == null || !dir.isDirectory())
            return 0L;

        if (isExternalStorageReadable()) {
            long size = 0;
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    // System.out.println(file.getName() + " " + file.length());
                    size += file.length();
                } else
                    size += getFolderSize(file);
            }
            System.out.println("getfoldersize --------------------"+size+"--------------------");

            return size;
        } else {
            return 0L;
        }
    }
}

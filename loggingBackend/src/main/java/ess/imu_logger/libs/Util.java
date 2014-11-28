package ess.imu_logger.libs;

import android.content.Context;
import android.os.Environment;
import android.os.SystemClock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by martin on 15.09.2014.
 */
public class Util {


    public static final String ILITIT_ANNOTATE = "de.unifreiburg.es.iLitIt.LIGHTER_ADD_CIG";
    public static final String ILITIT_ANNOTATE_REMOVE = "de.unifreiburg.es.iLitIt.REM_CIG";
    public static final String ILITIT_EXTRA_VIA = "via";
    public static final String ILITIT_EXTRA_TIMESTAMP = "timestamp";
    public static final String ILITIT_EXTRA_LAT = "latitude";
    public static final String ILITIT_EXTRA_LON = "longitude";


    //public static final String GAC_PATH_ANNOTATE_SMOKING_ACTIVITY = "/ess/imu_logger/annotate-smoking";
    public static final String GAC_PATH_ANNOTATED_SMOKING = "/ess/imu_logger/annotated-smoking";

    public static final String GAC_PATH_START_LOGGING = "/ess/imu_logger/startLogging";
    public static final String GAC_PATH_STOP_LOGGING = "/ess/imu_logger/stopLogging";


    public static final String GAC_PATH_PREFERENCES = "/ess/imu_logger/preferences";
    public static final String GAC_PATH_UPLOAD_DATA = "/ess/imu_logger/uploadData";

    public static final String GAC_PATH_SENSOR_DATA = "/ess/imu_logger/sensorData";
    public static final String GAC_PATH_CONFIRM_FILE_RECEIVED = "/ess/imu_logger/sensorDataReceived";

    public static final String GAC_PATH_TEST_ACTIVITY = "/test";


    public static final String PREFERENCES_NAME = "name";
    public static final String PREFERENCES_ANONYMIZE = "anonymize";
    public static final String PREFERENCES_AUTO_START = "auto_start";
    public static final String PREFERENCES_START_ON_BOOT = "start_on_boot";

    public static final String PREFERENCES_SENSOR_ACTIVATE = "sensor_activate";
    public static final String PREFERENCES_SAMPLING_RATE = "sampling_rate";

    public static final String PREFERENCES_ACCELEROMETER = "accelerometer";
    public static final String PREFERENCES_GYROSCOPE = "gyroscope";
    public static final String PREFERENCES_MAGNETIC_FIELD = "magneticField";
    public static final String PREFERENCES_AMBIENT_LIGHT = "ambientLight";
    public static final String PREFERENCES_PROXIMITY = "proximity";
    public static final String PREFERENCES_TEMPERATURE = "temperature";
    public static final String PREFERENCES_HUMIDITY = "humidity";
    public static final String PREFERENCES_PRESSURE = "pressure";

    public static final String PREFERENCES_ROTATION = "rotation";
    public static final String PREFERENCES_GRAVITY = "gravity";
    public static final String PREFERENCES_LINEAR_ACCELEROMETER = "linearAccelerometer";
    public static final String PREFERENCES_STEPS = "steps";

    public static final String PREFERENCES_SERVER_URL = "server_url";
    public static final String PREFERENCES_SERVER_PORT = "server_port";
    public static final String PREFERENCES_UPLOAD_FREQUENCY = "upload_frequency";
    public static final String PREFERENCES_LAST_UPLOAD = "last_upload";
    public static final String PREFERENCES_WIFI_ONLY = "wifi_only";

    public static final String SENSOR_FILE_NAME = "fileName";
    public static final String SENSOR_FILE = "file";
    public static final String SENSOR_SENT_TIMESTAMP = "timestamp";

    public static final int ZIP_UPLOAD_SERVICE_FREQUENCY = 10000;

    public static final String fileDir = "smokingStudy";


    public static final String ACTION_ANNOTATE_SMOKING = "ess.imu_logger.libs.Util.ACTION_ANNOTATE_SMOKING";
    public static final String ACTION_OPEN_START_ACTIVITY = "ess.imu_logger.libs.Util.ACTION_OPEN_START_ACTIVITY";



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

    public static double round(double valueToRound, int numberOfDecimalPlaces) {
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
        //return getFolderSize(dir);
        return getFileSize(dir);
    }

    public static Long getFolderSize(File dir) {


        if (dir == null || !dir.isDirectory())
            return 0L;

        if (isExternalStorageReadable()) {
            long size = 0;
            // TODO Multiply dir.listFiles() with Zipped File Size.
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    // System.out.println(file.getName() + " " + file.length());
                    size += file.length();
                } else
                    size += getFolderSize(file);
            }

            return size;
        } else {
            return 0L;
        }
    }

    public static long getFileSize(final File file) {
        if (file == null || !file.exists())
            return 0;
        if (!file.isDirectory())
            return file.length();
        final List<File> dirs = new LinkedList<File>();
        dirs.add(file);
        long result = 0;
        while (!dirs.isEmpty()) {
            final File dir = dirs.remove(0);
            if (!dir.exists())
                continue;
            final File[] listFiles = dir.listFiles();
            if (listFiles == null || listFiles.length == 0)
                continue;
            for (final File child : listFiles) {
                result += child.length();
                if (child.isDirectory())
                    dirs.add(child);
            }
        }
        return result;
    }


    public static String getFriendlyTime(long firstMillies, long secondMillies) {
        StringBuffer sb = new StringBuffer();
        long diffInSeconds;
        if (firstMillies > secondMillies)
            diffInSeconds = (firstMillies - secondMillies) / 1000;
        else
            diffInSeconds = (secondMillies - firstMillies) / 1000;

        long sec = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
        long min = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
        long hrs = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
        long days = (diffInSeconds = (diffInSeconds / 24)) >= 30 ? diffInSeconds % 30 : diffInSeconds;
        long months = (diffInSeconds = (diffInSeconds / 30)) >= 12 ? diffInSeconds % 12 : diffInSeconds;
        long years = (diffInSeconds = (diffInSeconds / 12));

        if (years > 0) {
            if (years == 1) {
                sb.append("ein Jahr");
            } else {
                sb.append(years + " Jahre");
            }
            if (years <= 6 && months > 0) {
                if (months == 1) {
                    sb.append(" und ein Monat");
                } else {
                    sb.append(" und " + months + " Monate");
                }
            }
        } else if (months > 0) {
            if (months == 1) {
                sb.append("ein Monat");
            } else {
                sb.append(months + " Monate");
            }
            if (months <= 6 && days > 0) {
                if (days == 1) {
                    sb.append(" und ein Tag");
                } else {
                    sb.append(" und " + days + " Tage");
                }
            }
        } else if (days > 0) {
            if (days == 1) {
                sb.append("ein Tag");
            } else {
                sb.append(days + " Tage");
            }
            if (days <= 3 && hrs > 0) {
                if (hrs == 1) {
                    sb.append(" und eine Stunde");
                } else {
                    sb.append(" und " + hrs + " Stunden");
                }
            }
        } else if (hrs > 0) {
            if (hrs == 1) {
                sb.append("eine Stunde");
            } else {
                sb.append(hrs + " Stunden");
            }
            if (min > 1) {
                sb.append(" und " + min + " Minuten");
            }
        } else if (min > 0) {
            if (min == 1) {
                sb.append("eine Minute");
            } else {
                sb.append(min + " Minuten");
            }
            if (sec > 1) {
                sb.append(" und " + sec + " Sekunden");
            }
        } else {
            if (sec <= 1) {
                sb.append("ungefähr eine Sekunde");
            } else {
                sb.append("ungefähr " + sec + " Sekunden");
            }
        }

        //sb.append(" ago");

        return sb.toString();
    }




    public static String formatLogString(Long unixTimestampInMillis, Long nanosSinceBoot, Long eventNanos, String type, String value0, String value1, String value2) {
        StringBuilder dataString = new StringBuilder();

        if (unixTimestampInMillis == null){
            dataString.append(System.currentTimeMillis());
            dataString.append(" ");
        } else {
            dataString.append(unixTimestampInMillis);
            dataString.append(" ");
        }

        if (nanosSinceBoot == null) {
            dataString.append(SystemClock.elapsedRealtime());
            dataString.append(" ");
        } else {
            dataString.append(nanosSinceBoot);
            dataString.append(" ");
        }

        if (eventNanos == null) {
            dataString.append("0 ");
        } else {
            dataString.append(eventNanos);
            dataString.append(" ");
        }

        if (type == null || type.equals("")) {
            dataString.append("UNKNOWN_TYPE ");
        } else {
            dataString.append(type.replaceAll("\\s+",""));
            dataString.append(" ");
        }

        if (!(value0 == null || value0.equals(""))) {
            dataString.append(value0.replaceAll("\\s+",""));
            dataString.append(" ");
        }

        if (!(value1 == null || value1.equals(""))) {
            dataString.append(value1.replaceAll("\\s+",""));
            dataString.append(" ");
        }

        if (!(value2 == null || value2.equals(""))) {
            dataString.append(value2.replaceAll("\\s+",""));
            dataString.append(" ");
        }

        dataString.append("\n");
        return dataString.toString();
    }
    public static String formatLogString(Long unixTimestampInMillis, Long nanosSinceBoot, String type, String value0, String value1, String value2) {
        return formatLogString(unixTimestampInMillis, nanosSinceBoot, (Long) null, type, value0, value1, value2);
    }
    public static String formatLogString(Long unixTimestampInMillis, String type, String value0, String value1, String value2) {
        return formatLogString(unixTimestampInMillis, (Long) null, type, value0, value1, value2);
    }
    public static String formatLogString(String type, String value0, String value1, String value2) {
        return formatLogString((Long) null, type, value0, value1, value2);
    }


    public static String formatLogString(Long unixTimestampInMillis, Long nanosSinceBoot, Long eventNanos, String type, String value0, String value1) {
        return formatLogString(unixTimestampInMillis, nanosSinceBoot, eventNanos, type, value0, value1, "");
    }
    public static String formatLogString(Long unixTimestampInMillis, Long eventNanos, String type, String value0, String value1) {
        return formatLogString(unixTimestampInMillis, eventNanos, type, value0, value1, "");
    }
    public static String formatLogString(Long unixTimestampInMillis, String type, String value0, String value1) {
        return formatLogString(unixTimestampInMillis, type, value0, value1, "");
    }
    public static String formatLogString(String type, String value0, String value1) {
        return formatLogString((Long) null, type, value0, value1);
    }


    public static String formatLogString(Long unixTimestampInMillis, Long nanosSinceBoot, Long eventNanos, String type, String value0) {
        return formatLogString(unixTimestampInMillis, nanosSinceBoot, eventNanos, type, value0, "");
    }
    public static String formatLogString(Long unixTimestampInMillis, Long eventNanos, String type, String value0) {
        return formatLogString(unixTimestampInMillis, eventNanos, type, value0, "");
    }
    public static String formatLogString(Long unixTimestampInMillis, String type, String value0) {
        return formatLogString(unixTimestampInMillis, type, value0, "");
    }
    public static String formatLogString(String type, String value0) {
        return formatLogString((Long) null, type, value0, "");
    }


    public static String formatLogString(Long unixTimestampInMillis, Long nanosSinceBoot, Long eventNanos, String type) {
        return formatLogString(unixTimestampInMillis, nanosSinceBoot, eventNanos, type, "");
    }
    public static String formatLogString(Long unixTimestampInMillis, Long eventNanos, String type) {
        return formatLogString(unixTimestampInMillis, eventNanos, type, "");
    }
    public static String formatLogString(Long unixTimestampInMillis, String type) {
        return formatLogString(unixTimestampInMillis, type, "");
    }
    public static String formatLogString(String type) {
        return formatLogString((Long) null, type, "");
    }




}

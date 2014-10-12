package ess.imu_logger.libs;

import android.content.Context;
import android.os.Environment;

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


    public static final String GAC_PATH_ANNOTATE_SMOKING_ACTIVITY = "/annotate-smoking";
    public static final String GAC_PATH_TEST_ACTIVITY = "/test";

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
}

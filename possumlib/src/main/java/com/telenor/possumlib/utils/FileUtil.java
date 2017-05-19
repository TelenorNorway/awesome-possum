package com.telenor.possumlib.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Handles storing of data to file
 */
public class FileUtil {
    private static final String tag = FileUtil.class.getName();
    private static DecimalFormat decimalFormat = new DecimalFormat("#,##0.#");
    private final static String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
    private static final int BUFFER = 2048;

    private static String dataDirectory(Context context) {
        return ensureDirExists(context.getFilesDir().getAbsolutePath() + "/data").getAbsolutePath();
    }

    private static File getUploadDirectory(Context context) {
        return ensureDirExists(dataDirectory(context) + "/Upload");
    }

    /**
     * Retrieves all relevant detector files from the upload directory
     * @param context an android context
     * @param detectorType the detectortype you want files from
     * @return a list of all files of the specific type present in the upload directory
     */
    public static List<File> getAllDetectorFiles(final Context context, final int detectorType) {
        File uploadDir = getUploadDirectory(context);
        File[] files = uploadDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                String[] nameSplit = toBucketKey(file).split("/");
                if (nameSplit.length < 3) return false;
                String detector = nameSplit[2];
                return detector.equals(SensorUtil.detectorTypeString(detectorType));
                }
        });
        return Arrays.asList(files);
    }
    /**
     * Returns the list of files ready to be uploaded to S3 ordered by modification date.
     *
     * @return files to be uploaded
     */
    public static List<File> getFilesReadyForUpload(Context context) {
        return Ordering
                .natural()
                .onResultOf(
                        new Function<File, Long>() {
                            public Long apply(File file) {
                                return file.lastModified();
                            }
                        })
                .sortedCopy(Arrays.asList(FileUtil
                        .getUploadDirectory(context)
                        .listFiles()));
    }

    public static File toUploadFile(Context context, String bucketKey) {
        return new File(getUploadDirectory(context), bucketKey.replace('/', '#'));
    }

    public static String toBucketKey(File uploadFile) {
        return uploadFile.getName().replace('#', '/');
    }

    private static File ensureDirExists(String pathname) {
        File dir = new File(pathname);
        if (!dir.exists()) {
            // Is it better to use dir.mkdirs() here?
            if (!dir.mkdir()) throw new RuntimeException("Unable to create data directory");
        }
        return dir;
    }

    /**
     * Gets you a file in the local file system with the given name, much like the files for the detectors
     *
     * @param context a context
     * @param name    name of the file
     * @param clear   whether the file should be emptied or not
     * @return a newly created or already existing file or null if unable to handle it
     */
    public static File getFile(@NonNull Context context, @NonNull String name, boolean clear) {
        File dataFile = createDataFile(new File(dataDirectory(context) + "/" + name));
        if (clear) {
            if (dataFile.exists() && !dataFile.delete())
                throw new RuntimeException("Failed to delete file:" + name);
            try {
                if (!dataFile.createNewFile())
                    throw new RuntimeException("Failed to create file - not allowed:" + name);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create file:" + name + "-" + e.getMessage());
            }
        }
        return dataFile;
    }

    /**
     * Returns the file it should store data to based on detectorType
     *
     * @param context      a context
     * @param detectorType the type of detector
     * @return the file used to save data
     */
    public static File getFile(Context context, int detectorType) {
        return createDataFile(new File(dataDirectory(context) + "/" + SensorUtil.detectorTypeString(detectorType)));
    }

    /**
     * Creates an empty file to be used for detectors
     *
     * @param dataFile files location. Existing files are not deleted
     * @return the file
     */
    public static synchronized File createDataFile(File dataFile) {
        if (!dataFile.exists()) {
            try {
                if (!dataFile.createNewFile()) {
                    if (dataFile.exists()) return dataFile;
                    else throw new RuntimeException("Unable to create file:" + dataFile.getName());
                }
            } catch (IOException | SecurityException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return dataFile;
    }

    /**
     * Returns a zipped version of the file (but does not delete the original)
     *
     * @param file  the file to be zipped
     * @param toZip the file to be zipped to
     * @return boolean true for success, false for failure
     */
    public static File zipFile(File file, File toZip) {
        if (file == null || file.length() == 0) {
            return null;
        }
        try {
            BufferedInputStream origin = new BufferedInputStream(new FileInputStream(file), BUFFER);

            // Create output file and corresponding OutputStream
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(toZip));

            // Write content of input file to a single entry in zip archive
            ZipEntry entry = new ZipEntry(file.getName());
            out.putNextEntry(entry);
            int count;
            byte data[] = new byte[BUFFER];
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }

            // Clean up
            origin.close();
            out.close();

            return toZip;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Stores list of lines to file by appending
     *
     * @param file  detectors stored file
     * @param lines lines to store in the file
     */
    public static void storeLines(@NonNull File file, final Queue<String> lines) {
        try {
            FileWriter writer = new FileWriter(file, true);
            while (!lines.isEmpty()) {
                String line = lines.remove();
                writer.append(line);
                writer.append("\r\n");
            }
            writer.close();
        } catch (IOException e) {
            Log.i(tag, "Write failed:", e);
        }
    }

    /**
     * Checks all detectors and finds if their length is greater than 0
     *
     * @return true if any detector has data stored, false if not
     */
    public static boolean hasData() {
//        for (AbstractDetector detector : AwesomePossum.detectors()) {
//            if (detector.storedData().length() > 0) return true;
//        }
        return false;
    }

    /**
     * Clear the main data directory (and all subdirectories)
     *
     * @param context a viable context
     */
    public static void clearDirectory(Context context) {
        clearDirectory(context, null);
    }

    /**
     * Clear a specific directory (and all subdirectories)
     *
     * @param context a viable context
     * @param dir     file representing the directory to delete
     */
    public static void clearDirectory(Context context, File dir) {
        if (dir == null) {
            dir = new File(dataDirectory(context));
        }
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    clearDirectory(context, file);
                } else {
                    if (!file.delete()) {
                        Log.i(tag, "Unable to delete file:" + file.getAbsolutePath());
                    }
                }
            }
        }
    }

    /**
     * Shows file size as number of the closest unit types (kB, MB, GB etc)
     *
     * @param sensorFile The file containing the sensorData
     * @return string with corresponding information about size
     */
    @SuppressWarnings("unused")
    public static String getSizeString(File sensorFile) {
        if (sensorFile == null || !sensorFile.exists()) return null;
        return getSizeFromLong(sensorFile.length());
    }

    /**
     * Shows the long as a number of the closest unit types (kB, MB, GB etc)
     *
     * @param size the size you want to show as bytes/kB etc
     * @return string with corresponding information about size
     */
    public static String getSizeFromLong(long size) {
        if (size <= 0)
            return "0kB";
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return decimalFormat.format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];

    }

    /**
     * Shorthand method for deleting file without having to handle boolean result
     *
     * @param file file to be deleted
     */
    public static void deleteFile(@NonNull File file) {
        if (file.exists()) {
            if (!file.delete()) {
                Log.i(tag, "Failed to delete file:" + file.getName());
            }
        }
    }

    /**
     * Deletes and creates an empty file of the given file
     *
     * @param file the file you want to delete and replace
     */
    public static void clearFile(@NonNull File file) {
        if (file.exists()) {
            if (!file.delete()) throw new RuntimeException("Failed to delete file:" + file);
        }
        try {
            if (!file.createNewFile())
                throw new RuntimeException("Failed to create new file:" + file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create new file:" + file + ":", e);
        }
    }
}
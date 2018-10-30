package com.adnd.capstonebackupfinal.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.adnd.capstonebackupfinal.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by haymon on 2018-09-28.
 */

/* -Used mainly for external storage, i.e. SD card. */
public final class StorageUtils {
    private static final String LOG_TAG = StorageUtils.class.getName();
    public final static int BACKUP_TYPE_OVERWRITE = 1;
    public final static int BACKUP_TYPE_RENAME = 2;
    /** 100 MB */
    private static final long MAX_FILE_SIZE = 104857600l;
    private static final double MIN_FREE_SPACE_PERCENT = 10d;

    private StorageUtils() {
    }

    /**
     *  Checks if external storage is available for read and write
     *  This may be unavailable if the user has mounted the storage to
     *  a PC or has removed the SD card.  Check if the volume is
     *  available first.
     *  */
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /** Checks if external storage is available to at least read
     *  This may be unavailable if the user has mounted the storage to
     *  a PC or has removed the SD card.  Check if the volume is
     *  available first.
    * */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Get all the files (and folders) in a folder
     * @return
     */
    public static List<File> getAllFiles(final File folder) {
        Log.d(LOG_TAG, "Listing files for folder...(" + folder.getAbsolutePath() + ")...:");
        return Arrays.asList(folder.listFiles());
    }

    /**
     *
     * @return
     */
    public static String getDefaultExternalStoragePath() {
        return Environment.getExternalStorageDirectory().toString();
    }

    /**
     * Copy file.
     * Note: the destination should not be set to the root directory of the SD card, instead,
     * it should be at least within a sub-folder as a minimum
     *
     * @param src The source file/folder.
     * @param dest The destination file/folder.
     * @param type The Backup type (overwrite vs replace).
     * @return true or false, depending on the status.
     * @throws UserException Contains the error message and error code.
     */
    public static boolean copyFile(File src, File dest, int type, Context context) throws UserException {
        //-Error-handling (and limitations):
        //-Make sure that the external storage volume is at least
        //readable and writable (before copying/'backup' file(s)).
        if (!(isExternalStorageReadable() && isExternalStorageWritable())) {
            Log.d(LOG_TAG, "Copy file failed. External storage not readable/writable.");
            throw new UserException(context.getResources().getString(R.string.toast_userexception_storage_not_readable_writable),
                    UserException.TYPE_STORAGE_NOT_READABLE_WRITABLE);
        }

        //-Make sure that the source file(s) or destination directory (folder)
        //has not changed.
        if (!(src.exists() && dest.exists())) {
            Log.d(LOG_TAG, "Copy file failed. Source/Dest file(s)/folder missing.");
            throw new UserException(context.getResources().getString(R.string.toast_userexception_source_dest_not_found),
                    UserException.TYPE_SOURCE_DEST_NOT_FOUND);
        }

        //-Make sure there is free space on the drive.
        boolean freeSpace = false;
        File dir = new File(StorageUtils.getDefaultExternalStoragePath());
        //-must cast division operand to float.
        long percentage = (long)(((float)dir.getFreeSpace()/dir.getTotalSpace()) * 100);
        double percentageDbl = Long.valueOf(percentage).doubleValue();

        if (percentageDbl >= MIN_FREE_SPACE_PERCENT) {
            freeSpace = true;
        }

        if (!freeSpace) {
            Log.d(LOG_TAG, "Copy file failed. Insufficient disk space.");
            throw new UserException(context.getResources().getString(R.string.toast_userexception_no_free_space),
                    UserException.TYPE_NO_FREE_SPACE);
        }

        //-Impose a 'max file size' rule (100 MB).
        if (src.length() > MAX_FILE_SIZE) {
            Log.d(LOG_TAG, "Copy file failed. File > 100 MB.");
            throw new UserException(context.getResources().getString(R.string.toast_userexception_max_filesize_exceed),
                    UserException.TYPE_MAX_FILE_SIZE_EXCEEDED);
        }

        //-Make sure the BackupConfig setting is correct.
        if (type != BACKUP_TYPE_RENAME && type != BACKUP_TYPE_OVERWRITE) {
            Log.d(LOG_TAG, "Copy file failed. Backup type setting invalid.");
            throw new UserException(context.getResources().getString(R.string.toast_userexception_backuptype_setting_invalid),
                    UserException.TYPE_BACKUPTYPE_SETTING_INVALID);
        }

        //-Based on the SharedPrefs setting (overwrite vs rename/append Date),
        //tweak the destination accordingly.
        File finalDest = null;
        String destPath = dest.getPath();
        String fileNameWithExtension = src.getName();

        if (type == BACKUP_TYPE_OVERWRITE) {
            String finalPath = destPath + File.separator + fileNameWithExtension;
            finalDest = new File(finalPath);
        }
        else if (type == BACKUP_TYPE_RENAME) {
            //-Also, handle the scenario if the filename has no file
            //extension.
            String fileNameWithoutExtension = "";
            int pos = fileNameWithExtension.lastIndexOf(".");
            if (pos > 0) {
                fileNameWithoutExtension = fileNameWithExtension.substring(0, pos);
            }
            else {
                fileNameWithoutExtension = fileNameWithExtension;
            }

            String extension = "";
            int i = fileNameWithExtension.lastIndexOf('.');
            if (i > 0) {
                extension = fileNameWithExtension.substring(i+1);
            }

            String dateSuffix = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

            String finalPath = "";
            if (extension.isEmpty()) {
                finalPath = destPath + File.separator + fileNameWithoutExtension + "_" + dateSuffix;
            }
            else {
                finalPath = destPath + File.separator + fileNameWithoutExtension + "_" + dateSuffix + "." + extension;
            }
            finalDest = new File(finalPath);
        }
        dest = finalDest;

        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            in.close();
            out.close();

            Log.d(LOG_TAG, "Copy file successful.");
        }
        catch (Exception e) {
            Log.d(LOG_TAG, "error: " + e.getMessage());
            return false;
        }
        return true;
    }
}

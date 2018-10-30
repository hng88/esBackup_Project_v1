package com.adnd.capstonebackupfinal.util;

/**
 * Created by haymon on 2018-10-17.
 */

//-Used to house possible UserExceptions.
public class UserException extends Exception {
    private int errorCode;
    public static final int TYPE_STORAGE_NOT_READABLE_WRITABLE = 1;
    public static final int TYPE_SOURCE_DEST_NOT_FOUND = 2;
    public static final int TYPE_NO_FREE_SPACE = 3;
    public static final int TYPE_MAX_FILE_SIZE_EXCEEDED = 4;
    public static final int TYPE_BACKUPTYPE_SETTING_INVALID = 5;

    public UserException() {
        super();
    }

    public UserException(String message) {
        super(message);
    }

    public UserException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}

package com.adnd.capstonebackupfinal.util;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Stack;

/**
 * Created by haymon on 2018-09-29.
 */

//-Used mainly to store the storage volume (i.e. SD card)
// navigation history
public class FileNavigationHistory implements Parcelable {
    private static final String LOG_TAG = "FileHistory";
    private String currDirPath;
    private Stack<String> currHistory;

    public FileNavigationHistory(String currDirPath) {
        this.currDirPath = currDirPath;
        this.currHistory = new Stack<String>();
    }

    protected FileNavigationHistory(Parcel in) {
        currDirPath = in.readString();
        if (in.readByte() == 0x01) {
            currHistory = new Stack<String>();
            in.readList(currHistory, String.class.getClassLoader());
        } else {
            currHistory = null;
        }
    }

    public static final Creator<FileNavigationHistory> CREATOR = new Creator<FileNavigationHistory>() {
        @Override
        public FileNavigationHistory createFromParcel(Parcel in) {
            return new FileNavigationHistory(in);
        }

        @Override
        public FileNavigationHistory[] newArray(int size) {
            return new FileNavigationHistory[size];
        }
    };

    public String getCurrDirPath() {
        return currDirPath;
    }

    public void setCurrDirPath(String currDirPath) {
        this.currDirPath = currDirPath;
    }

    public String getPrevDir() {
        return currHistory.pop();
    }

    public void setPrevDir(String prevDir) {
        currHistory.add(prevDir);
    }

    /**
     * Whether or not there is a navigation history
     *
     * @return
     */
    public boolean isCurrHistory() {
        return !currHistory.isEmpty();
    }

    @Override
    public String toString() {
        return "FileNavigationHistory{" +
                "currDirPath=" + currDirPath +
                ", currHistory=" + currHistory +
                '}';
    }

    @Override
    public int describeContents () {
        return 0;
    }

    @Override
    public void writeToParcel (Parcel dest,int flags){
        dest.writeString(currDirPath);
        if (currHistory == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(currHistory);
        }
    }
}

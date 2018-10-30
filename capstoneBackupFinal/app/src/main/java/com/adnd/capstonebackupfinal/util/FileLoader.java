package com.adnd.capstonebackupfinal.util;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.io.File;
import java.util.List;

/**
 * Created by haymon on 2018-09-29.
 */

public class FileLoader extends AsyncTaskLoader<List<File>> {
    private String path;

    public FileLoader(Context context, String path) {
        super(context);
        this.path = path;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<File> loadInBackground() {
        if (path == null) {
            return null;
        }

        File folder = new File(path);

        List<File> files = StorageUtils.getAllFiles(folder);
        return files;
    }
}

package com.adnd.capstonebackupfinal.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.adnd.capstonebackupfinal.db.AppDatabase;
import com.adnd.capstonebackupfinal.db.entity.BackupConfigEntity;

import java.util.List;

/**
 * Created by haymon on 2018-09-30.
 */

public class MainViewModel extends AndroidViewModel {
    private static final String TAG = MainViewModel.class.getSimpleName();
    private LiveData<List<BackupConfigEntity>> backupConfigs;

    public MainViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the backupConfigs from the DataBase");
        backupConfigs = database.backupConfigDao().loadAllBackupConfigs();
    }

    public LiveData<List<BackupConfigEntity>> getBackupConfigs() {
        return backupConfigs;
    }
}

package com.adnd.capstonebackupfinal.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.adnd.capstonebackupfinal.db.AppDatabase;
import com.adnd.capstonebackupfinal.db.entity.BackupConfigEntity;

/**
 * Created by haymon on 2018-09-30.
 */

public class AddBackupConfigViewModel extends ViewModel {
    private LiveData<BackupConfigEntity> backupConfig;

    public AddBackupConfigViewModel(AppDatabase database, int backupConfigId) {
        backupConfig = database.backupConfigDao().loadBackupConfigById(backupConfigId);
    }

    public LiveData<BackupConfigEntity> getBackupConfig() {
        return backupConfig;
    }
}

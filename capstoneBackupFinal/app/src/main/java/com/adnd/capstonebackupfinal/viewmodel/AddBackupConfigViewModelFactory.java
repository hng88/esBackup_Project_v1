package com.adnd.capstonebackupfinal.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.adnd.capstonebackupfinal.db.AppDatabase;

/**
 * Created by haymon on 2018-09-30.
 */

public class AddBackupConfigViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final AppDatabase db;
    private final int backupConfigId;

    public AddBackupConfigViewModelFactory(AppDatabase database, int backupConfigId) {
        db = database;
        this.backupConfigId = backupConfigId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new AddBackupConfigViewModel(db, backupConfigId);
    }
}

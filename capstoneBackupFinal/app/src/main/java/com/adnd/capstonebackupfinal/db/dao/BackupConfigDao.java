package com.adnd.capstonebackupfinal.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.adnd.capstonebackupfinal.db.entity.BackupConfigEntity;

import java.util.List;

/**
 * Created by haymon on 2018-09-30.
 */

@Dao
public interface BackupConfigDao {
    @Query("SELECT * FROM backup_config ORDER BY name")
    LiveData<List<BackupConfigEntity>> loadAllBackupConfigs();

    @Insert
    long insertBackupConfig(BackupConfigEntity backupConfigEntity);

    @Update
    void updateBackupConfig(BackupConfigEntity backupConfigEntity);

    @Delete
    void deleteBackupConfig(BackupConfigEntity backupConfigEntity);

    @Query("SELECT * FROM backup_config WHERE id = :id")
    LiveData<BackupConfigEntity> loadBackupConfigById(int id);
}

package com.adnd.capstonebackupfinal.model;

import java.util.Date;

/**
 * Created by haymon on 2018-09-30.
 */

public interface BackupConfig {
    int getId();
    String getName();
    String getDescription();
    String getSrcPath();
    String getDestPath();
    Date getUpdatedAt();
}

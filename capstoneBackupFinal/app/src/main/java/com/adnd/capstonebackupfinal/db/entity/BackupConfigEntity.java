package com.adnd.capstonebackupfinal.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import com.adnd.capstonebackupfinal.model.BackupConfig;

import java.util.Date;

/**
 * Created by haymon on 2018-09-30.
 */

@Entity(tableName = "backup_config",
        indices = {@Index(value = {"name"}, unique = true)})
public class BackupConfigEntity implements BackupConfig, Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String description;
    @ColumnInfo(name = "src_path")
    private String srcPath;
    @ColumnInfo(name = "dest_path")
    private String destPath;
    @ColumnInfo(name = "updated_at")
    private Date updatedAt;

    @Ignore
    public BackupConfigEntity(String name, String description, String srcPath, String destPath, Date updatedAt) {
        this.name = name;
        this.description = description;
        this.srcPath = srcPath;
        this.destPath = destPath;
        this.updatedAt = updatedAt;
    }

    public BackupConfigEntity(int id, String name, String description, String srcPath, String destPath, Date updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.srcPath = srcPath;
        this.destPath = destPath;
        this.updatedAt = updatedAt;
    }

    protected BackupConfigEntity(Parcel in) {
        id = in.readInt();
        name = in.readString();
        description = in.readString();
        srcPath = in.readString();
        destPath = in.readString();
    }

    public static final Creator<BackupConfigEntity> CREATOR = new Creator<BackupConfigEntity>() {
        @Override
        public BackupConfigEntity createFromParcel(Parcel in) {
            return new BackupConfigEntity(in);
        }

        @Override
        public BackupConfigEntity[] newArray(int size) {
            return new BackupConfigEntity[size];
        }
    };

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getSrcPath() {
        return srcPath;
    }

    public void setSrcPath(String srcPath) {
        this.srcPath = srcPath;
    }

    @Override
    public String getDestPath() {
        return destPath;
    }

    public void setDestPath(String destPath) {
        this.destPath = destPath;
    }

    @Override
    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(srcPath);
        dest.writeString(destPath);
    }
}

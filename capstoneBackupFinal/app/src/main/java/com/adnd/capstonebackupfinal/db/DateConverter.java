package com.adnd.capstonebackupfinal.db;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

/**
 * Created by haymon on 2018-09-30.
 */

public class DateConverter {
    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}

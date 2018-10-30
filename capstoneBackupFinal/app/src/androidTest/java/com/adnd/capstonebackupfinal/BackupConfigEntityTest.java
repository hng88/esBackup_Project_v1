package com.adnd.capstonebackupfinal;

import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.adnd.capstonebackupfinal.db.AppDatabase;
import com.adnd.capstonebackupfinal.db.entity.BackupConfigEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by haymon on 2018-08-27.
 */

/**
 * Instrumented test, which will execute on an Android device.
 */
@RunWith(AndroidJUnit4.class)
public class BackupConfigEntityTest {
    private AppDatabase db;

    @Before
    public void createDb() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getTargetContext(), AppDatabase.class).build();
    }

    @Test
    public void testBackupConfigDb() throws Exception {
        final BackupConfigEntity backupConfig = new BackupConfigEntity("name",
                "desc", "src", "dest", null);

        long insertedBackupConfigId = db.backupConfigDao().insertBackupConfig(backupConfig);
        int id = (int)insertedBackupConfigId;

        assertThat(id, equalTo(1));
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }
}

package com.adnd.capstonebackupfinal.ui;

import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.adnd.capstonebackupfinal.AppExecutors;
import com.adnd.capstonebackupfinal.MyApplication;
import com.adnd.capstonebackupfinal.R;
import com.adnd.capstonebackupfinal.db.AppDatabase;
import com.adnd.capstonebackupfinal.db.entity.BackupConfigEntity;
import com.adnd.capstonebackupfinal.util.PermissionUtils;
import com.adnd.capstonebackupfinal.util.QueryUtils;
import com.adnd.capstonebackupfinal.util.StorageUtils;
import com.adnd.capstonebackupfinal.util.TaskFragment;
import com.adnd.capstonebackupfinal.viewmodel.MainViewModel;
import com.adnd.capstonebackupfinal.widget.UpdateWidgetService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// This activity is responsible for displaying the user-defined
//'Backup Configurations' (if they exist in the DB)
public class MainActivity extends AppCompatActivity implements BackupConfigAdapter.ListItemClickListener,
        TaskFragment.TaskCallbacks {
    private static final String TAG = "MainActivity";
    private AppDatabase db;
    private FloatingActionButton fabBtn;
    private BackupConfigAdapter backupConfigAdapter;
    private BackupConfigRecyclerView listviewBackupConfig;
    private View loadingIndicator;
    public static final String PARAM_BACKUPCONFIGID = "paramBackupConfigId";
    private TaskFragment taskFragment;
    public static final String INTENT_ACTION_UPDATE_WIDGET = "intentActionUpdateWidget";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    public static final String SHAREDPREF_KEY = "sharedPrefIntKey";
    private static final String DATE_FORMAT = "dd/MM/yyy";
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
    private ProgressDialog dialog;
    private static final String PARAM_BACKUP_PROGRESSDIALOG = "paramBackupProgressDialog";
    private static final String SHAREDPREF_RUNTIMEPERMISSION_RUNONCE_KEY = "sharedPrefPermOnceKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //-for 6.0+/API23+
        checkRuntimePermissions();

        db = AppDatabase.getInstance(getApplicationContext());

        listviewBackupConfig = findViewById(R.id.backupconfigs_listview);
        LinearLayoutManager trailerLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        listviewBackupConfig.setHasFixedSize(true);
        backupConfigAdapter = new BackupConfigAdapter(this, new ArrayList<BackupConfigEntity>());
        listviewBackupConfig.setEmptyView(findViewById(android.R.id.empty));
        listviewBackupConfig.setLayoutManager(trailerLayoutManager);
        listviewBackupConfig.setAdapter(backupConfigAdapter);

        loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        setupViewModel();

        fabBtn = findViewById(R.id.fab_btn);
        fabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabBtnClicked();
            }
        });

        //-Init the TaskFragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        taskFragment = (TaskFragment)fragmentManager.findFragmentByTag(TaskFragment.TAG_HEADLESS_FRAGMENT);
        if (taskFragment == null) {
            taskFragment = new TaskFragment();
            fragmentManager.beginTransaction().add(taskFragment, TaskFragment.TAG_HEADLESS_FRAGMENT).commit();
        }

        if (QueryUtils.isNetworkConnection(this)) {
            //-Google Admob.
            AdView mAdView = findViewById(R.id.adView);
            // Create an ad request. Check logcat output for the hashed device ID to
            // get test ads on a physical device. e.g.
            // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
            // Or .addTestDevice(AdRequest.DEVICE_ID_EMULATOR) for emulator.
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("ABCDEF012345")
                    .build();
            mAdView.loadAd(adRequest);

            //-GA Analytics.
            MyApplication application = (MyApplication) getApplication();
            application.trackScreenView("MainActivity");
        }
    }

    private void setupViewModel() {
        //-only for 'BackupConfig'
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getBackupConfigs().observe(this, new Observer<List<BackupConfigEntity>>() {
            @Override
            public void onChanged(@Nullable List<BackupConfigEntity> backupConfigs) {
                Log.d(TAG, "onChanged(): update BackupConfigs list data...");
                backupConfigAdapter.updateItems(backupConfigs);
                updateWidget(backupConfigs);
            }
        });
    }

    /**
     * This is to update the 'updated_at' Date field for the respective
     * BackupConfig to reflect the last backed up date (post-'Backup' btn).
     * @param backupConfig
     */
    private void updateLastBackedUpDate(BackupConfigEntity backupConfig) {
        Log.d(TAG, "Backup done. Updating the last backed up Date.  BackupConfig: " + backupConfig.getName());
        //-make sure post-backup, that the respective BackupConfig
        //is also updated in the DB, i.e. reflect the 'updated_at' field
        //properly, i.e. current Date (last backed up)
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                //-update existing BackupConfig
                Log.d(TAG, "Backup Config updated: " + backupConfig.getName());
                backupConfig.setId(backupConfig.getId());
                Date date = new Date();
                backupConfig.setUpdatedAt(date);
                db.backupConfigDao().updateBackupConfig(backupConfig);
            }
        });
    }

    @Override
    public void onListItemClick(int backupConfigId) {
        Log.d(TAG, "Backup Config clicked from master list...");
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(PARAM_BACKUPCONFIGID, backupConfigId);
        startActivity(intent);
    }

    @Override
    public void onBackupBtnClick(BackupConfigEntity backupConfig) {
        //-Runtime permissions check.
        if (PermissionUtils.isRuntimePermissionsSupported()) {
            if (PermissionUtils.getOutstandingPermissions(this, PermissionUtils.permissionsNeeded).size() > 0) {
                Log.d(TAG, "Read/Write External Storage Permission Denied. Must enable in Settings.");
                Toast.makeText(this, R.string.toast_permissions_denied_need_settings_enable, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Log.d(TAG, "Backing up...:" + backupConfig.getName());

        //-Lookup the BackupConfig Settings (overwrite vs rename)
        //from SharedPrefs
        int backupType = -1;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String backupBy = sharedPrefs.getString(getString(R.string.settings_backup_by_key),
                getString(R.string.settings_backup_by_default));

        if (backupBy.equalsIgnoreCase(getString(R.string.settings_backup_by_overwrite_value))) {
            backupType = StorageUtils.BACKUP_TYPE_OVERWRITE;
        }
        else if (backupBy.equalsIgnoreCase(getString(R.string.settings_backup_by_rename_value))) {
            backupType = StorageUtils.BACKUP_TYPE_RENAME;
        }

        //-Backup the SD card file.
        //-Do this via AsyncTask so it won't block the main UI thread
        taskFragment.startBackupFileTask(backupConfig, backupType);
    }

    private void fabBtnClicked() {
        Log.d(TAG, "FAB btn clicked. Intent to DetailActivity.");
        Intent intent = new Intent(this, DetailActivity.class);
        startActivity(intent);
    }

    private void initProgressDialog() {
        //-Init the ProgressDialog
        dialog = new ProgressDialog(this, android.R.style.Theme_Material_Dialog);
        dialog.setMessage(getString(R.string.toast_progressdialog_backingup));
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.show();
    }

    //Background task callback
    @Override
    public void onPreExecute() {
        initProgressDialog();
    }

    //Background task callback
    @Override
    public void onProgressUpdate(int progress) {
        //do nothing for now
    }

    //Background task callback
    @Override
    public void onPostExecute(BackupConfigEntity backupConfig) {
        if (taskFragment != null) {
            taskFragment.updateExecutingStatus(false);
        }

        //-remove the ProgressDialog.
        dialog.dismiss();

        //-if for whatever reason, the Backup fails.
        if (backupConfig == null) {
            Log.d(TAG, "Backup failed. Ignoring update for last backed up Date.");
            Toast.makeText(this, R.string.toast_backupbtn_backup_fail, Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Backup complete. Updating last backed up Date.");
        String successToastMsg = getString(R.string.toast_backupbtn_backup_complete) + " " + backupConfig.getName();
        Toast.makeText(this, successToastMsg, Toast.LENGTH_LONG).show();
        //-make sure post-backup, that the respective BackupConfig
        //is also updated in the DB, i.e. reflect the 'updated_at' field
        //properly, i.e. current Date (last backed up)
        updateLastBackedUpDate(backupConfig);
    }

    //Background task callback
    @Override
    public void onCancelled() {
        //do nothing for now
    }

    private void updateWidget(List<BackupConfigEntity> backupConfigs) {
        Log.d(TAG, "BackupConfigs have changed. Update the Widget to reflect the change.");
        prepareWidgetData(backupConfigs);

        //-Notify the Widget broadcaster receiver to update the Widget
        //with the BackupConfigs data via onUpdate() via the Widget Service
        //(all done asynchronously on a separate background thread)
        Intent intent = new Intent(this, UpdateWidgetService.class);
        intent.setAction(INTENT_ACTION_UPDATE_WIDGET);
        startService(intent);
    }

    private void prepareWidgetData(List<BackupConfigEntity> backupConfigs) {
        //-build the BackupConfigs String
        StringBuilder sb = new StringBuilder();
        String updatedAt = "";
        for (BackupConfigEntity bc : backupConfigs) {
            //-get the 'updated_at' field from the row.
            Date date = bc.getUpdatedAt();
            if (date == null) {
                updatedAt = getString(R.string.widget_list_lastbackedup_date_null);
            }
            else {
                updatedAt = dateFormat.format(date);
            }
            sb.append(bc.getName()).append(", ")
            .append(getString(R.string.widget_list_lastbackedup_date_header))
            .append(" ").append(updatedAt)
            .append("\n");
        }
        String backupConfigurations = sb.toString();

        //-Store the BackupConfigs data in SharedPrefs
        //-Using SharedPrefs for storing the current BackupConfigs
        //to be used for the Widget (the Widget will display the BackupConfig
        //name and last backed up date)
        // Obtain a reference to the SharedPreferences file for this app
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        editor.putString(SHAREDPREF_KEY, backupConfigurations);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();

        //-Note: this is needed to handle specific use-cases
        //i.e. if the User is playing around with the lifecycle while this
        //Delete confirmation dialog is still present, and comes back to
        //the screen, then this can cause a 'Window Leaked' error if the
        //Dialog is not 'cleaned up' properly
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    /**
     * Save the current state of this activity
     * This is to support screen orientation rotation
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d(TAG, "onSave()");
        //-Save the ProgressDialog state.
        boolean isDisplayed = false;
        if (dialog != null) {
            if (dialog.isShowing()) {
                isDisplayed = true;
            }
        }
        outState.putBoolean(PARAM_BACKUP_PROGRESSDIALOG, isDisplayed);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.i(TAG, "onRestore()");
        //-Restore the ProgressDialog.
        boolean isDisplayed = savedInstanceState.getBoolean(PARAM_BACKUP_PROGRESSDIALOG);
        if (isDisplayed) {
            initProgressDialog();
        }
    }

    private void checkRuntimePermissions() {
        //-Permissions (for 6.0+/API23+)
        Log.d(TAG, "checkRuntimePermissions()");
        if (!PermissionUtils.isRuntimePermissionsSupported()) {
            return;
        }

        // Obtain a reference to the SharedPreferences file for this app
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //-Only run this permissions check once (check this via SharedPrefs).
        if (!prefs.getBoolean(SHAREDPREF_RUNTIMEPERMISSION_RUNONCE_KEY, false)) {
            PermissionUtils.checkAndPromptPermissions(this, this);
        }
        editor = prefs.edit();
        editor.putBoolean(SHAREDPREF_RUNTIMEPERMISSION_RUNONCE_KEY, true);
        editor.commit();
    }

    /**
     * Permissions callback.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d(TAG, "onRequestPermissionResult()");
        if (requestCode == PermissionUtils.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {
            if (!(grantResults.length > 0)) {
                return;
            }

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "permission was granted, yay!");
                //-Good, mandatory permissions are granted.
                Toast.makeText(this, R.string.toast_permission_readwritestorage_granted, Toast.LENGTH_SHORT).show();
            }
            else if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                Log.d(TAG, "permission denied, boo!");
                //-Bad, mandatory permissions denied.
                Toast.makeText(this, R.string.toast_permission_readwritestorage_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }
}

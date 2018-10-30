package com.adnd.capstonebackupfinal.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.adnd.capstonebackupfinal.R;
import com.adnd.capstonebackupfinal.util.PermissionUtils;
import com.adnd.capstonebackupfinal.util.StorageUtils;

// This activity is responsible for displaying the Details of the
//'Backup Configuration'
//-It may display a two-pane or single-pane UI, depending on phone screen/tablet screen
//-For a single-pane scenario, it will only display the 'New'/'Edit' screen
//(the User will have to navigate again for additional details [i.e. file explorer])
//-For a two-pane scenario, it will display both the 'New'/'Edit' screen AND
//the file explorer screen on the second pane to the right (if applicable)
public class DetailActivity extends AppCompatActivity implements AddBackupConfigFragment.OnMyClickListener,
        FileNavigationFragment.OnMyClickListener, EditBackupConfigFragment.OnMyClickListener {
    private static final String TAG = "DetailActivity";
    private int backupConfigId;
    private boolean isTwoPane;
    private static final String FILENAV_FRAGMENT_TAG = "fileNavFragmentTag";
    public static final int STARTACTIVITYFORRESULT_REQUEST_CODE = 1;
    private static final String ADDBACKUPCONFIG_FRAGMENT_TAG = "addBackupConfigFragmentTag";
    private static final String EDITBACKUPCONFIG_FRAGMENT_TAG = "editBackupConfigFragmentTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        /** -Note: the data (backupConfigId) can still be null - this can happen in the scenario
         * of the 'up' button (the container Activity is destroyed/re-created), and
         * thus, this Fragment is re-created
         * -as a result, to prevent the container Activity from being destroyed/re-created
         * post-'up' button, the launchmode 'singleTop' is used in the manifest xml
         */
        //-based on the Intent (New/Edit)
        //-on init, get the data from the other Activity
        Intent intent = getIntent();
        backupConfigId = intent.getIntExtra(MainActivity.PARAM_BACKUPCONFIGID, -1);
        //-change the AppBar title accordingly.
        if (backupConfigId == -1) {
            //-'New' scenario
            setTitle(getString(R.string.appbar_title_new_backupconfig));
        }
        else {
            //-'Edit' scenario
            setTitle(getString(R.string.appbar_title_edit_backupconfig));
        }

        //-The mandatory 'New'/'Edit' BackupConfig Fragment must be
        //populated regardless of the device (two-pane or single-pane UI).
        //-Only populate Fragment on init
        if(savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();

            if (backupConfigId == -1) {
                // Creating a new NewBackupConfig fragment
                AddBackupConfigFragment backupConfigFragment = AddBackupConfigFragment.newInstance();
                // Add the fragment to its container using a transaction
                fragmentManager.beginTransaction()
                        .add(R.id.backupconfigdetail_container, backupConfigFragment, ADDBACKUPCONFIG_FRAGMENT_TAG)
                        .commit();
            }
            else {
                // Creating a new EditBackupConfig fragment
                EditBackupConfigFragment backupConfigFragment =
                        EditBackupConfigFragment.newInstance(backupConfigId);
                // Add the fragment to its container using a transaction
                fragmentManager.beginTransaction()
                        .add(R.id.backupconfigdetail_container, backupConfigFragment, EDITBACKUPCONFIG_FRAGMENT_TAG)
                        .commit();
            }
        }

        // Determine two-pane vs single-pane display
        if(findViewById(R.id.backupconfig_linear_layout) != null) {
            // This LinearLayout will only initially exist in the two-pane tablet case
            isTwoPane = true;
        }
        else {
            // single-pane mode - display fragments on a phone in separate activities
            isTwoPane = false;
        }
    }

    private void userInputPathInteraction() {
        //-Note: the User's device may not have any external storage volume.
        //(ex. if the user has mounted the storage to a PC or has
        // removed the SD card.)
        //Thus, make sure they have it, before proceeding any further.
        if (!StorageUtils.isExternalStorageReadable()) {
            Log.d(TAG, "External storage volume not found.");
            Toast.makeText(this, R.string.toast_no_externalstoragevolume_found, Toast.LENGTH_SHORT).show();
            return;
        }

        //-Runtime permissions check.
        if (PermissionUtils.isRuntimePermissionsSupported()) {
            if (PermissionUtils.getOutstandingPermissions(this, PermissionUtils.permissionsNeeded).size() > 0) {
                Log.d(TAG, "Read/Write External Storage Permission Denied. Must enable in Settings.");
                Toast.makeText(this, R.string.toast_permissions_denied_need_settings_enable, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Handle the two-pane case and replace existing fragments right when a
        // BackupConfig path is selected
        if (isTwoPane) {
            Log.d(TAG, "Two-Pane mode: Edit Paths - replace Fragment.");
            //-The file explorer Fragment (only need to populate this Fragment
            //for two-pane scenario).

            //-Make sure to destroy the Loader as well or else
            //the Fragment will not reload the data when it is 'replaced'
            getSupportLoaderManager().destroyLoader(FileNavigationFragment.FILES_LOADER_ID);

            // Create two-pane interaction
            FileNavigationFragment fileNavFragment = FileNavigationFragment.newInstance();

            // Set the currently displayed BackupConfig path for the correct FileNav fragment
            // Replace the old head fragment with a new one
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.filenavigation_container, fileNavFragment, FILENAV_FRAGMENT_TAG)
                    .commit();
        }
        else {
            Log.d(TAG, "Single-pane mode: Edit Paths - Intent to Activity.");
            // Handle the single-pane phone case
            Intent intent = new Intent(this, FileNavContainerActivity.class);
            startActivityForResult(intent, STARTACTIVITYFORRESULT_REQUEST_CODE);
        }
    }

    //-This is the Callback from the 'New' BackupConfig Fragment
    @Override
    public void onEditPathClicked() {
        Log.d(TAG, "User input edit Path.  Launch file explorer navigator via Activity/Fragment.");
        userInputPathInteraction();
    }

    //-This is the Callback from the 'Edit' BackupConfig Fragment
    @Override
    public void onUpdatePathClicked() {
        Log.d(TAG, "User input edit Path.  Launch file explorer navigator via Activity/Fragment.");
        userInputPathInteraction();
    }

    //-Populate the Activity's child Fragment (source/dest path field).
    private void populatePathFieldUI(int type, String path) {
        if ((type != FileNavigationFragment.PATH_TYPE_SOURCE && type != FileNavigationFragment.PATH_TYPE_DESTINATION) ||
                path.isEmpty()) {
            Log.d(TAG, "Failed to retrieve the file explorer navigation source/dest paths.");
            Toast.makeText(this, R.string.toast_failed_retrieve_filepath, Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Populating the 'path' field UI in the (Add/Edit) Fragment.");
        Log.d(TAG, "Source/Dest type: " + type);
        Log.d(TAG, "Path: " + path);

        //-populate the fields (source/dest path, depending on type) in the Fragment.
        FragmentManager fragmentManager = getSupportFragmentManager();

        //-Depending on the scenario.
        if (backupConfigId == -1) {
            //-'New' scenario
            AddBackupConfigFragment addBackupConfigFragment = (AddBackupConfigFragment) fragmentManager.findFragmentByTag(ADDBACKUPCONFIG_FRAGMENT_TAG);

            //-'Source'
            if (type == FileNavigationFragment.PATH_TYPE_SOURCE) {
                addBackupConfigFragment.populateSourcePathField(path);
            }
            //-'Destination'
            else if (type == FileNavigationFragment.PATH_TYPE_DESTINATION) {
                addBackupConfigFragment.populateDestinationPathField(path);
            }
        }
        else {
            //-'Edit' scenario
            EditBackupConfigFragment editBackupConfigFragment = (EditBackupConfigFragment) fragmentManager.findFragmentByTag(EDITBACKUPCONFIG_FRAGMENT_TAG);

            //-'Source'
            if (type == FileNavigationFragment.PATH_TYPE_SOURCE) {
                editBackupConfigFragment.populateSourcePathField(path);
            }
            //-'Destination'
            else if (type == FileNavigationFragment.PATH_TYPE_DESTINATION) {
                editBackupConfigFragment.populateDestinationPathField(path);
            }
        }
    }

    @Override
    public void onCreateBackupConfigClicked() {
        Log.d(TAG, "Created BackupConfig. Close parent Activity.");
        //-Nothing unique to be done here since both the two-pane
        //and single-pane have the same behavior.
        finish();
    }

    @Override
    public void onUpdateBackupConfigClicked() {
        Log.d(TAG, "Updated BackupConfig. Close parent Activity.");
        //-Nothing unique to be done here since both the two-pane
        //and single-pane have the same behavior.
        finish();
    }

    //-This is the Callback from the Tablet Fragment (file explorer).
    @Override
    public void onChoosePathDialogClicked(int pathType, String path) {
        //-this is unique to Tablet devices only.
        if (!isTwoPane) {
            return;
        }

        Log.d(TAG, "Two-Pane mode: Chose Path Dialog - populate field in left Fragment.");

        //-pass back the source/dest path back to the left Fragment.
        populatePathFieldUI(pathType, path);
    }

    //-This is the Callback from the phone Activity (file explorer).
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "Single-Pane mode: Chose Path Dialog - retrieved field in parent Activity.");
        //-this is unique to phone devices only.
        //-retrieve the data (file path, and type) from child Activity.
        if (requestCode != STARTACTIVITYFORRESULT_REQUEST_CODE || resultCode != Activity.RESULT_OK) {
            return;
        }

        String path = data.getStringExtra(FileNavContainerActivity.RESULT_PARAM_FILE_PATH);
        int type = data.getIntExtra(FileNavContainerActivity.RESULT_PARAM_FILE_PATH_TYPE, -1);

        //-pass back the source/dest path back to the Fragment.
        populatePathFieldUI(type, path);
    }

    //-This is the Callback from the Tablet Fragment (file explorer).
    @Override
    public void onCloseClicked() {
        //-File Navigation 'Close/X' btn (only exists for Tablets)
        Log.d(TAG, "Closing Fragment...");
        //-Make sure to destroy the Loader as well or else
        //the Fragment will not reload the data when it is 'replaced'
        getSupportLoaderManager().destroyLoader(FileNavigationFragment.FILES_LOADER_ID);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FileNavigationFragment fileNavFragment = (FileNavigationFragment) fragmentManager.findFragmentByTag(FILENAV_FRAGMENT_TAG);
        // Remove the fragment
        fragmentManager.beginTransaction().remove(fileNavFragment).commit();
    }
}

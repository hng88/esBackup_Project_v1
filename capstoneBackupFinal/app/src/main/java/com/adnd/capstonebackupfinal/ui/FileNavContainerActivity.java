package com.adnd.capstonebackupfinal.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.adnd.capstonebackupfinal.R;

// This activity is responsible for displaying the file explorer
//navigation (in a Fragment)
//-This is only to support single-pane UI (i.e. phone)
//-For tablets, the Fragment will be on the right-side in two-pane mode.
public class FileNavContainerActivity extends AppCompatActivity implements FileNavigationFragment.OnMyClickListener {
    private static final String TAG = "FileNavContainer";
    private FileNavigationFragment fileNavFragment;
    public static final String RESULT_PARAM_FILE_PATH = "resultParamFilePath";
    public static final String RESULT_PARAM_FILE_PATH_TYPE = "resultParamFilePathType";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filenavcontainer);

        //-The mandatory FileExplorer navigation Fragment
        //-Only populate Fragment on init
        if(savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();

            // Creating a new FileExplorer navigation fragment
            fileNavFragment = FileNavigationFragment.newInstance();
            // Add the fragment to its container using a transaction
            fragmentManager.beginTransaction()
                    .add(R.id.filenavigation_container, fileNavFragment)
                    .commit();
        }
    }

    @Override
    public void onChoosePathDialogClicked(int pathType, String path) {
        Log.d(TAG, "Single-Pane mode: Chose Path Dialog - populate field in parent Activity.");
        //-this is unique to phone devices only.
        //i.e. pass back the source/dest path back to the DetailActivity
        Intent intent = new Intent();
        intent.putExtra(RESULT_PARAM_FILE_PATH, path);
        intent.putExtra(RESULT_PARAM_FILE_PATH_TYPE, pathType);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onCloseClicked() {
        //-Do nothing (this is unique to Tablet devices only.)
    }
}

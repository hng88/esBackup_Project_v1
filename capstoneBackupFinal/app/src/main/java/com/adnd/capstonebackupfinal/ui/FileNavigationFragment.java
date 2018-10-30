package com.adnd.capstonebackupfinal.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.adnd.capstonebackupfinal.R;
import com.adnd.capstonebackupfinal.util.FileLoader;
import com.adnd.capstonebackupfinal.util.FileNavigationHistory;
import com.adnd.capstonebackupfinal.util.PermissionUtils;
import com.adnd.capstonebackupfinal.util.StorageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// This fragment displays the external storage volume (i.e. SD card)
//-It will allow navigation of folders/files (file explorer)
public class FileNavigationFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<File>>,
        FileAdapter.ListItemClickListener, FileAdapter.ListItemLongClickListener {
    private static final String TAG = "FileNavigationFragment";
    private FileAdapter fileAdapter;
    private FileRecyclerView listview;
    private GridLayoutManager gridLayoutManager;
    private View loadingIndicator;
    public static final int FILES_LOADER_ID = 1;
    public static final int MIN_NUM_LIST_ITEMS = 1;
    private static final String DEFAULT_FOLDER_PATH = StorageUtils.getDefaultExternalStoragePath();
    private FileNavigationHistory fileNavigationHistory;
    public static final String PARAM_FILENAVIGATIONHISTORY = "fileNavigationHistory";
    private Button fileNavigationHistoryBackBtn;
    private TextView fileNavBreadcrumbBar;
    private Button fileNavCloseBtn;
    public final static int PATH_TYPE_SOURCE = 1;
    public final static int PATH_TYPE_DESTINATION = 2;
    private AlertDialog alertDialog;

    private OnMyClickListener myClickListener;

    public interface OnMyClickListener {
        void onChoosePathDialogClicked(int pathType, String path);
        void onCloseClicked();
    }

    public FileNavigationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FileNavigationFragment.
     */
    public static FileNavigationFragment newInstance() {
        FileNavigationFragment fragment = new FileNavigationFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //-get the correct Folder (state)
        //-support screen orientation rotation change
        if (savedInstanceState != null) {
            fileNavigationHistory = savedInstanceState.getParcelable(PARAM_FILENAVIGATIONHISTORY);
        }
        //-on init
        else {
            fileNavigationHistory = new FileNavigationHistory(DEFAULT_FOLDER_PATH);
        }
    }

    // Inflates the View of the File Explorer navigation
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_file_navigation, container, false);

        //-for 6.0+/API23+
        if (checkRuntimePermissions()) {
            return rootView;
        }

        //-'Back' btn for moving backwards in the folder structure
        fileNavigationHistoryBackBtn = rootView.findViewById(R.id.fileNavigationHistoryBackBtn);
        fileNavigationHistoryBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileNavHistoryBackBtnClicked();
            }
        });

        listview = rootView.findViewById(R.id.files_listview);

        gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        listview.setLayoutManager(gridLayoutManager);
        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        listview.setHasFixedSize(true);

        fileAdapter = new FileAdapter(this, new ArrayList<File>(), this);
        listview.setAdapter(fileAdapter);

        listview.setEmptyView(rootView.findViewById(android.R.id.empty));

        loadingIndicator = rootView.findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        //-File Navigation 'bread crumb' bar
        fileNavBreadcrumbBar = rootView.findViewById(R.id.fileNavigationBreadcrumbBarTv);

        //-File Navigation 'Close/X' btn (only exists for Tablets)
        if(rootView.findViewById(R.id.fileNavigationCloseBtn) != null) {
            fileNavCloseBtn = rootView.findViewById(R.id.fileNavigationCloseBtn);
            fileNavCloseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fileNavCloseBtnClicked();
                }
            });
        }

        //-Init the AsyncTaskLoader
        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        loaderManager.initLoader(FILES_LOADER_ID, null, this);

        return rootView;
    }

    private void fileNavHistoryBackBtnClicked() {
        if (!fileNavigationHistory.isCurrHistory()) {
            return;
        }

        fileNavigationHistory.setCurrDirPath(fileNavigationHistory.getPrevDir());

        //-'refresh' the Loader with the new folder data
        LoaderManager loaderManager = getActivity().getSupportLoaderManager();
        loaderManager.restartLoader(FILES_LOADER_ID, null, this);
    }

    private void choosePathDialogBtnClicked(int pathType, String path) {
        // Trigger the callback method
        if (myClickListener != null) {
            myClickListener.onChoosePathDialogClicked(pathType, path);
        }
    }

    private void fileNavCloseBtnClicked() {
        //-File Navigation 'Close/X' btn (only exists for Tablets)
        //-'close' the FileNav Fragment.
        Log.d(TAG, "File Nav Close btn clicked. Close Fragment.");
        // Trigger the callback method
        if (myClickListener != null) {
            myClickListener.onCloseClicked();
        }
    }

    @Override
    public Loader<List<File>> onCreateLoader(int id, Bundle args) {
        return new FileLoader(getActivity(), fileNavigationHistory.getCurrDirPath());
    }

    @Override
    public void onLoadFinished(Loader<List<File>> loader, List<File> files) {
        // Hide loading indicator because the data has been loaded
        loadingIndicator.setVisibility(View.GONE);
        // Clear the adapter of previous file data
        fileAdapter.updateItems(new ArrayList<File>());

        //-double-check that the cloud has returned some results
        if (files != null && !files.isEmpty() && files.size() >= MIN_NUM_LIST_ITEMS) {
            fileAdapter.updateItems(files);
        }

        //-Always update the 'bread crumb' bar regardless.
        fileNavBreadcrumbBar.setText(fileNavigationHistory.getCurrDirPath());
    }

    @Override
    public void onLoaderReset(Loader<List<File>> loader) {
        // Loader reset, so we can clear out our existing data.
        fileAdapter.updateItems(new ArrayList<File>());
    }

    @Override
    public void onListItemClick(File file) {
        Log.d(TAG, "List item (Folder/File) clicked.");
        //-for 6.0+/API23+
        if (checkRuntimePermissions()) {
            return;
        }

        if (file.isDirectory()) {
            //-Save the state of the file navigation
            fileNavigationHistory.setPrevDir(fileNavigationHistory.getCurrDirPath());
            fileNavigationHistory.setCurrDirPath(file.getPath());

            //-'refresh' the Loader with the new folder data
            LoaderManager loaderManager = getActivity().getSupportLoaderManager();
            loaderManager.restartLoader(FILES_LOADER_ID, null, this);
        }
    }

    @Override
    public void onListItemLongClick(File file) {
        Log.d(TAG, "List item (Folder/File) 'long' clicked.");
        //-Pop up the Dialog for the User to 'Choose Path'
        //-i.e. either 'Source' or 'Destination', depending on the type of File
        //(Source must be a File, and Destination must be a Folder.)
        int type = -1;
        String [] choices = new String[1];
        if (file.isDirectory()) {
            choices[0] = getString(R.string.dialog_choice_dest);
            type = PATH_TYPE_DESTINATION;
        }
        else {
            choices[0] = getString(R.string.dialog_choice_src);
            type = PATH_TYPE_SOURCE;
        }

        showPathConfirmationDialog(type, file.getPath(), choices);
    }

    /**
     * Prompt the user to confirm the type of Path (source or destination).
     */
    private void showPathConfirmationDialog(int type, String path, String [] choices) {
        //-Pop up the Dialog for the User to 'Choose Path'
        //-i.e. either 'Source' or 'Destination', depending on the type of File
        //(Source must be a File, and Destination must be a Folder.)
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.dialog_title_choosepath)
                .setSingleChoiceItems(choices, 0, null)
                .setPositiveButton(R.string.dialog_btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //-User confirmed Dialog.
                        Log.d(TAG, "User confirmed Dialog.");
                        //-Pass the data (chosen Path) back to the parent Activity.
                        choosePathDialogBtnClicked(type, path);
                    }
                })
                .setNegativeButton(R.string.dialog_btn_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //-User cancelled Dialog.
                        Log.d(TAG, "User cancelled Dialog.");
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

        alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Save the current state of this fragment
     * This is to support screen orientation rotation
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d(TAG, "onSave()");
        //-Save the fileNavigationHistory
        outState.putParcelable(PARAM_FILENAVIGATIONHISTORY, fileNavigationHistory);
    }

    // Override onAttach to make sure that the container activity has implemented the callback
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMyClickListener) {
            myClickListener = (OnMyClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMyClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        myClickListener = null;
    }

    @Override
    public void onStop() {
        super.onStop();

        //-Note: this is needed to handle specific use-cases
        //i.e. if the User is playing around with the lifecycle while this
        //confirmation dialog is still present, and comes back to
        //the screen, then this can cause a 'Window Leaked' error if the
        //Dialog is not 'cleaned up' properly
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    private boolean checkRuntimePermissions() {
        //-Runtime permissions check.
        if (PermissionUtils.isRuntimePermissionsSupported()) {
            if (PermissionUtils.getOutstandingPermissions(getActivity(), PermissionUtils.permissionsNeeded).size() > 0) {
                Log.d(TAG, "Read/Write External Storage Permission Denied. Must enable in Settings.");
                Toast.makeText(getActivity(), R.string.toast_permissions_denied_need_settings_enable, Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }
}

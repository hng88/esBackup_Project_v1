package com.adnd.capstonebackupfinal.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.adnd.capstonebackupfinal.AppExecutors;
import com.adnd.capstonebackupfinal.R;
import com.adnd.capstonebackupfinal.db.AppDatabase;
import com.adnd.capstonebackupfinal.db.entity.BackupConfigEntity;
import com.adnd.capstonebackupfinal.viewmodel.AddBackupConfigViewModel;
import com.adnd.capstonebackupfinal.viewmodel.AddBackupConfigViewModelFactory;

import java.util.Date;

// This fragment displays BackupConfig Details (for 'Edit' only)
public class EditBackupConfigFragment extends Fragment {
    private static final String TAG = "EditBackupFragment";
    private static final String BACKUPCONFIGID_PARAM = "backupConfigIdParam";
    private int backupConfigId;
    private EditText nameEt;
    private EditText descEt;
    private TextView srcTv;
    private TextView destTv;
    private Button updateBtn;
    private AppDatabase db;
    private BackupConfigEntity currBackupConfig;
    private AlertDialog alertDialog;
    public final static String PARAM_BACKUPCONFIG_USERINPUT = "paramBackupConfigUserInput";
    private String [] backupConfigUserInputsState;
    public final static String PARAM_CURR_BACKUPCONFIG = "paramCurrBackupConfig";

    private OnMyClickListener myClickListener;

    public interface OnMyClickListener {
        void onUpdatePathClicked();
        void onUpdateBackupConfigClicked();
    }

    public EditBackupConfigFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param backupConfigId Parameter 1.
     * @return A new instance of fragment EditBackupConfigFragment.
     */
    public static EditBackupConfigFragment newInstance(int backupConfigId) {
        EditBackupConfigFragment fragment = new EditBackupConfigFragment();
        Bundle args = new Bundle();
        args.putInt(BACKUPCONFIGID_PARAM, backupConfigId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            backupConfigId = getArguments().getInt(BACKUPCONFIGID_PARAM);
        }

        db = AppDatabase.getInstance(getActivity().getApplicationContext());

        //-support screen orientation rotation change
        if(savedInstanceState != null) {
            backupConfigUserInputsState = savedInstanceState.getStringArray(PARAM_BACKUPCONFIG_USERINPUT);
            currBackupConfig = savedInstanceState.getParcelable(PARAM_CURR_BACKUPCONFIG);
        }

        //-Needed to display the Options Menu (for 'Delete')
        setHasOptionsMenu(true);
    }

    // Inflates the View of 'Edit' BackupConfig
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_edit_backupconfig, container, false);
        nameEt = rootView.findViewById(R.id.name_userinput_et);
        descEt = rootView.findViewById(R.id.desc_userinput_et);
        srcTv = rootView.findViewById(R.id.srcPath_userinput_et);
        destTv = rootView.findViewById(R.id.destPath_userinput_et);
        updateBtn = rootView.findViewById(R.id.update_backupconfig_btn);

        srcTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                srcEtClicked();
            }
        });

        destTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                destEtClicked();
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBtnClicked();
            }
        });

        //-support screen orientation rotation change
        if(savedInstanceState != null) {
            nameEt.setText(backupConfigUserInputsState[0]);
            descEt.setText(backupConfigUserInputsState[1]);
            srcTv.setText(backupConfigUserInputsState[2]);
            destTv.setText(backupConfigUserInputsState[3]);
        }
        else {
            setupViewModel();
        }

        return rootView;
    }

    private void setupViewModel() {
        AddBackupConfigViewModelFactory factory = new AddBackupConfigViewModelFactory(db, backupConfigId);
        final AddBackupConfigViewModel viewModel = ViewModelProviders.of(this, factory).get(AddBackupConfigViewModel.class);

        viewModel.getBackupConfig().observe(this, new Observer<BackupConfigEntity>() {
            @Override
            public void onChanged(@Nullable BackupConfigEntity backupConfig) {
                Log.d(TAG, "onChanged(): Finding BackupConfig by id...");

                if(backupConfig == null) {
                    //-no BackupConfig found
                    Log.d(TAG, "BackupConfig not found, BackupConfig id: " + backupConfigId);
                }
                else {
                    //-BackupConfig found
                    Log.d(TAG, "BackupConfig found, BackupConfig name: " + backupConfig.getName());

                    nameEt.setText(backupConfig.getName());
                    descEt.setText(backupConfig.getDescription());
                    srcTv.setText(backupConfig.getSrcPath());
                    destTv.setText(backupConfig.getDestPath());

                    currBackupConfig = backupConfig;
                }
            }
        });
    }

    private void srcEtClicked() {
        // Trigger the callback method
        if (myClickListener != null) {
            myClickListener.onUpdatePathClicked();
        }
    }

    private void destEtClicked() {
        // Trigger the callback method
        if (myClickListener != null) {
            myClickListener.onUpdatePathClicked();
        }
    }

    private void updateBtnClicked() {
        Log.d(TAG, "Update Backup Config Btn clicked...");

        //-Error-handling:
        String name = nameEt.getText().toString();
        String desc = descEt.getText().toString();
        String src = srcTv.getText().toString();
        String dest = destTv.getText().toString();

        if (name.isEmpty() || desc.isEmpty() || src.isEmpty() || dest.isEmpty()) {
            Log.d(TAG, "Invalid user input! Ignoring...");
            Toast.makeText(getActivity().getApplicationContext(), R.string.toast_updatebackupconfig_userinput_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Updating Backup Config...");

        //-Make sure to retain the 'updated_at' field (if it exists).
        Date date = currBackupConfig.getUpdatedAt();
        final BackupConfigEntity backupConfig = new BackupConfigEntity(name, desc, src, dest, date);

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                //-handle possible Constraint Exception.
                try {
                    //-update existing BackupConfig
                    Log.d(TAG, "Backup Config updated: " + backupConfig.getName());
                    backupConfig.setId(backupConfigId);
                    db.backupConfigDao().updateBackupConfig(backupConfig);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity().getApplicationContext(), R.string.toast_updatebackupconfig_success, Toast.LENGTH_SHORT).show();
                            getActivity().finish();

                            // Trigger the callback method
                            if (myClickListener != null) {
                                myClickListener.onUpdateBackupConfigClicked();
                            }
                        }
                    });
                }
                catch (SQLiteConstraintException e) {
                    //-'duplicate' (i.e. same name for BackupConfig)
                    Log.d(TAG, "Backup Config update Failed (SQLiteConstraintException): " + backupConfig.getName());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity().getApplicationContext(), R.string.toast_updatebackupconfig_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void deleteBackupConfig() {
        Log.d(TAG, "Delete Backup Config Btn clicked...");
        final BackupConfigEntity backupConfig = currBackupConfig;

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                //-delete BackupConfig
                Log.d(TAG, "BackupConfig removed from DB: " + backupConfig.getName());
                backupConfig.setId(backupConfigId);
                db.backupConfigDao().deleteBackupConfig(backupConfig);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity().getApplicationContext(), R.string.toast_remove_backupconfig, Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }
                });
            }
        });
    }

    public void populateSourcePathField(String path) {
        srcTv.setText(path);
    }

    public void populateDestinationPathField(String path) {
        destTv.setText(path);
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

    /**
     * Save the current state of this fragment
     * This is to support screen orientation rotation
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d(TAG, "onSave()");
        //-Save all the BackupConfig userinput fields.
        String name = nameEt.getText().toString();
        String desc = descEt.getText().toString();
        String src = srcTv.getText().toString();
        String dest = destTv.getText().toString();
        String [] array = {name, desc, src, dest};
        outState.putStringArray(PARAM_BACKUPCONFIG_USERINPUT, array);

        //-Save the current 'Edit' BackupConfig.
        outState.putParcelable(PARAM_CURR_BACKUPCONFIG, currBackupConfig);
    }

    /**
     * Prompt the user to confirm that they want to delete this BackupConfig.
     */
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.delete_dialog_msg)
        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteBackupConfig();
            }
        })
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_editor, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                showDeleteConfirmationDialog();
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
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }
}

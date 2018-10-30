package com.adnd.capstonebackupfinal.ui;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
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

// This fragment displays BackupConfig Details (for 'New' only)
public class AddBackupConfigFragment extends Fragment {
    private static final String TAG = "AddBackupConfigFragment";
    private EditText nameEt;
    private EditText descEt;
    private TextView srcTv;
    private TextView destTv;
    private Button createBtn;
    private AppDatabase db;
    private String [] backupConfigUserInputsState;

    private OnMyClickListener myClickListener;

    public interface OnMyClickListener {
        void onEditPathClicked();
        void onCreateBackupConfigClicked();
    }

    public AddBackupConfigFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AddBackupConfigFragment.
     */
    public static AddBackupConfigFragment newInstance() {
        AddBackupConfigFragment fragment = new AddBackupConfigFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = AppDatabase.getInstance(getActivity().getApplicationContext());

        //-support screen orientation rotation change
        if(savedInstanceState != null) {
            backupConfigUserInputsState = savedInstanceState.getStringArray(EditBackupConfigFragment.PARAM_BACKUPCONFIG_USERINPUT);
        }
    }

    // Inflates the View of 'New' BackupConfig
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_add_backupconfig, container, false);
        nameEt = rootView.findViewById(R.id.name_userinput_et);
        descEt = rootView.findViewById(R.id.desc_userinput_et);
        srcTv = rootView.findViewById(R.id.srcPath_userinput_et);
        destTv = rootView.findViewById(R.id.destPath_userinput_et);
        createBtn = rootView.findViewById(R.id.create_backupconfig_btn);

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

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createBtnClicked();
            }
        });

        //-support screen orientation rotation change
        if(savedInstanceState != null) {
            nameEt.setText(backupConfigUserInputsState[0]);
            descEt.setText(backupConfigUserInputsState[1]);
            srcTv.setText(backupConfigUserInputsState[2]);
            destTv.setText(backupConfigUserInputsState[3]);
        }

        return rootView;
    }

    private void srcEtClicked() {
        // Trigger the callback method
        if (myClickListener != null) {
            myClickListener.onEditPathClicked();
        }
    }

    private void destEtClicked() {
        // Trigger the callback method
        if (myClickListener != null) {
            myClickListener.onEditPathClicked();
        }
    }

    private void createBtnClicked() {
        Log.d(TAG, "Create Backup Config Btn clicked...");

        //-Error-handling:
        String name = nameEt.getText().toString();
        String desc = descEt.getText().toString();
        String src = srcTv.getText().toString();
        String dest = destTv.getText().toString();

        if (name.isEmpty() || desc.isEmpty() || src.isEmpty() || dest.isEmpty()) {
            Log.d(TAG, "Invalid user input! Ignoring...");
            Toast.makeText(getActivity().getApplicationContext(), R.string.toast_createbackupconfig_userinput_invalid, Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Creating Backup Config...");

        final BackupConfigEntity backupConfig = new BackupConfigEntity(name, desc, src, dest, null);

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                //-handle possible Constraint Exception.
                try {
                    //-insert new BackupConfig
                    Log.d(TAG, "Backup Config created: " + backupConfig.getName());
                    long insertedBackupConfigId = db.backupConfigDao().insertBackupConfig(backupConfig);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity().getApplicationContext(), R.string.toast_createbackupconfig_success, Toast.LENGTH_SHORT).show();
                            getActivity().finish();

                            // Trigger the callback method
                            if (myClickListener != null) {
                                myClickListener.onCreateBackupConfigClicked();
                            }
                        }
                    });
                }
                catch (SQLiteConstraintException e) {
                    //-'duplicate' (i.e. same name for BackupConfig)
                    Log.d(TAG, "Backup Config create Failed (SQLiteConstraintException): " + backupConfig.getName());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity().getApplicationContext(), R.string.toast_createbackupconfig_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
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
        outState.putStringArray(EditBackupConfigFragment.PARAM_BACKUPCONFIG_USERINPUT, array);
    }
}

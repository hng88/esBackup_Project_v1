package com.adnd.capstonebackupfinal.util;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.adnd.capstonebackupfinal.db.entity.BackupConfigEntity;

import java.io.File;

/**
 * Created by haymon on 2018-09-30.
 */

//-Used to house an AsyncTask (copy SD card file(s))
//-This is to support screen rotation (retain instance true)
//-Example: if the User accidentally rotates the screen during a backup, then
//the Backup will still continue to execute regardless.
public class TaskFragment extends Fragment {
    private static final String TAG = "TaskFragment";
    public static final String TAG_HEADLESS_FRAGMENT = "taskFragment";
    private TaskCallbacks taskCallbacks;
    private BackupFileTask backupFileTask;
    /**
     * -Used to prevent multiple concurrent tasks executing at the same time
     * -for example, the User clicks on 'Backup' btn for all of their
     * BackupConfigs, one after the other
     * -in this case, it will only execute one Backup at a time, thus, the other
     * 'Backup' clicks will not be executed if the current execution is still
     * running.
     **/
    private boolean isTaskExecuting = false;

    /**
     * The interface that receives the callbacks to Parent Activity from
     * this Fragment.
     */
    public interface TaskCallbacks {
        void onPreExecute();
        void onProgressUpdate(int progress);
        void onPostExecute(BackupConfigEntity backupConfig);
        void onCancelled();
    }

    /**
     * Called to do initial creation of a fragment.
     * This is called after onAttach(Activity) and before onCreateView(LayoutInflater, ViewGroup, Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //-Needed so that Fragment will not get destroyed (from parent Activity lifecycle),
        //, retaining the AsyncTask upon screen rotation
        //-Note: This Fragment cannot be used if it's in a back stack (so make sure
        //to keep this Fragment out of a standard UI workflow)
        setRetainInstance(true);
    }

    /**
     * An AsyncTask to execute a Task on a background thread.
     * -Backup the SD card file.
     * -Do this via AsyncTask so it won't block the main UI thread
     */
    private class BackupFileTask extends AsyncTask<Void, Integer, BackupConfigEntity> {
        private BackupConfigEntity backupConfig;
        private int backupType;

        public BackupFileTask(BackupConfigEntity backupConfig, int backupType) {
            this.backupConfig = backupConfig;
            this.backupType = backupType;
        }

        /**
         * Runs on the main UI thread.
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(taskCallbacks != null) {
                taskCallbacks.onPreExecute();
            }
        }

        /**
         * This is on a background thread.
         */
        @Override
        protected BackupConfigEntity doInBackground(Void... params) {
            if (backupConfig == null || backupConfig.getSrcPath().isEmpty() || backupConfig.getDestPath().isEmpty()) {
                return null;
            }

            File src = new File(backupConfig.getSrcPath());
            File dest = new File(backupConfig.getDestPath());

            boolean success = false;
            //-Error-handling.
            try {
                success = StorageUtils.copyFile(src, dest, backupType, getActivity());
            }
            catch (UserException e) {
                Log.d(TAG, e.getMessage());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            if (!success) {
                return null;
            }
            else {
                return backupConfig;
            }
        }

        /**
         * Runs on the main UI thread.
         */
        @Override
        protected void onPostExecute(BackupConfigEntity backupConfig) {
            super.onPostExecute(backupConfig);

            if(taskCallbacks != null) {
                taskCallbacks.onPostExecute(backupConfig);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if(taskCallbacks != null) {
                taskCallbacks.onProgressUpdate(values[0]);
            }
        }

        @Override
        protected void onCancelled(BackupConfigEntity backupConfig) {
            if(taskCallbacks != null) {
                taskCallbacks.onCancelled();
            }
        }
    }

    public void startBackupFileTask(BackupConfigEntity backupConfig, int backupType) {
        if(!isTaskExecuting){
            backupFileTask = new BackupFileTask(backupConfig, backupType);
            backupFileTask.execute();
            isTaskExecuting = true;
        }
    }

    public void cancelBackupFileTask() {
        if(isTaskExecuting){
            backupFileTask.cancel(true);
            isTaskExecuting = false;
        }
    }

    public void updateExecutingStatus(boolean isExecuting){
        this.isTaskExecuting = isExecuting;
    }

    // Override onAttach to make sure that the container activity has implemented the callback
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TaskCallbacks) {
            taskCallbacks = (TaskCallbacks) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement TaskCallbacks");
        }
    }

    /**
     * Called when the fragment is no longer attached to its activity. This is called after onDestroy().
     */
    @Override
    public void onDetach() {
        super.onDetach();
        taskCallbacks = null;
    }
}

package com.adnd.capstonebackupfinal.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.adnd.capstonebackupfinal.R;
import com.adnd.capstonebackupfinal.db.entity.BackupConfigEntity;

import java.util.List;

/**
 * Created by haymon on 2018-10-02.
 */

public class BackupConfigAdapter extends RecyclerView.Adapter<BackupConfigAdapter.BackupConfigViewHolder> {
    private static final String TAG = BackupConfigAdapter.class.getSimpleName();

    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    final private ListItemClickListener onClickListener;
    private List<BackupConfigEntity> data;

    /**
     * The interface that receives onClick messages.
     */
    public interface ListItemClickListener {
        void onListItemClick(int backupConfigId);
        void onBackupBtnClick(BackupConfigEntity backupConfig);
    }

    /**
     * Constructor for BackupConfigAdapter that accepts a number of items to display and the specification
     * for the ListItemClickListener.
     *
     * @param listener Listener for list item clicks
     */
    public BackupConfigAdapter(ListItemClickListener listener, List<BackupConfigEntity> data) {
        onClickListener = listener;
        this.data = data;
    }

    /**
     *
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param parent The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new NumberViewHolder that holds the View for each list item
     */
    @NonNull
    @Override
    public BackupConfigViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.backupconfig_listitem;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        BackupConfigViewHolder viewHolder = new BackupConfigViewHolder(view);

        return viewHolder;
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the correct
     * indices in the list for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull BackupConfigViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * Used when the Loader is finished, update the Listview item data with the response data
     * @param data The network API call response data
     */
    public void updateItems(List<BackupConfigEntity> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    /**
     * Cache of the children views for a list item.
     */
    public class BackupConfigViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView backupConfigNameTv;
        private Button backupBtn;

        /**
         * Constructor for our ViewHolder. Within this constructor, we get a reference to our
         * TextViews and set an onClickListener to listen for clicks. Those will be handled in the
         * onClick method below.
         * @param itemView The View that you inflated in
         *                 {@link BackupConfigViewHolder#onCreateViewHolder(ViewGroup, int)}
         */
        public BackupConfigViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            backupConfigNameTv = itemView.findViewById(R.id.backupConfig_name_tv);
            backupBtn = itemView.findViewById(R.id.backupConfig_backup_btn);

            backupBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    backupBtnClicked();
                }
            });
        }

        /**
         * A method we wrote for convenience. This method will take an integer as input and
         * use that integer to display the backup config within a list item.
         * @param listIndex Position of the item in the list
         */
        private void bind(int listIndex) {
            if (data == null || data.size() == 0) {
                return;
            }

            BackupConfigEntity backupConfig = data.get(listIndex);
            String backupConfigName = backupConfig.getName();
            backupConfigNameTv.setText(backupConfigName);
        }

        private void backupBtnClicked() {
            int clickedPosition = getAdapterPosition();

            if (data == null || data.size() == 0) {
                return;
            }

            BackupConfigEntity backupConfig = data.get(clickedPosition);
            Log.d(TAG, "Backup btn clicked for BackupConfig: " + backupConfig.getName());
            onClickListener.onBackupBtnClick(backupConfig);
        }

        /**
         * Called whenever a user clicks on an item in the list.
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();

            if (data == null || data.size() == 0) {
                return;
            }

            BackupConfigEntity backupConfig = data.get(clickedPosition);
            onClickListener.onListItemClick(backupConfig.getId());
        }
    }
}

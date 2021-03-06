package com.adnd.capstonebackupfinal.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.adnd.capstonebackupfinal.R;

import java.io.File;
import java.util.List;

/**
 * Created by haymon on 2018-09-29.
 */

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.FileViewHolder> {
    private static final String TAG = FileAdapter.class.getSimpleName();

    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    private ListItemClickListener onClickListener;
    private List<File> data;
    private ListItemLongClickListener onLongClickListener;

    /**
     * The interface that receives onClick messages.
     */
    public interface ListItemClickListener {
        void onListItemClick(File file);
    }

    /**
     * The interface that receives onLongClick messages.
     */
    public interface ListItemLongClickListener {
        void onListItemLongClick(File file);
    }

    /**
     * Constructor for FileAdapter that accepts a number of items to display and the specification
     * for the ListItemClickListener.
     *
     * @param listener Listener for list item clicks
     */
    public FileAdapter(ListItemClickListener listener, List<File> data, ListItemLongClickListener longListener) {
        onClickListener = listener;
        this.data = data;
        onLongClickListener = longListener;
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
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.file_listitem;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        FileViewHolder viewHolder = new FileViewHolder(view);

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
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
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
    public void updateItems(List<File> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    /**
     * Cache of the children views for a list item.
     */
    public class FileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {
        private TextView fileName_tv;
        private ImageView file_iv;

        /**
         * Constructor for our ViewHolder. Within this constructor, we get a reference to our
         * TextViews and set an onClickListener to listen for clicks. Those will be handled in the
         * onClick method below.
         * @param itemView The View that you inflated in
         *                 {@link FileViewHolder#onCreateViewHolder(ViewGroup, int)}
         */
        public FileViewHolder(View itemView) {
            super(itemView);

            fileName_tv = itemView.findViewById(R.id.listitem_file_name);
            file_iv = itemView.findViewById(R.id.listitem_file_image);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        /**
         * A method we wrote for convenience. This method will take an integer as input and
         * use that integer to display the baking recipe within a list item.
         * @param listIndex Position of the item in the list
         */
        private void bind(int listIndex) {
            if (data == null || data.size() == 0) {
                return;
            }

            File file = data.get(listIndex);
            String fileName = file.getName();
            fileName_tv.setText(fileName);

            if (file.isDirectory()) {
                file_iv.setImageResource(R.drawable.ic_folder);
            }
            else {
                file_iv.setImageResource(R.drawable.ic_file);
            }
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

            File file = data.get(clickedPosition);
            onClickListener.onListItemClick(file);
        }

        /**
         * Called whenever a user 'long' clicks on an item in the list.
         * @param v The View that was 'long' clicked
         */
        @Override
        public boolean onLongClick(View v) {
            int clickedPosition = getAdapterPosition();

            if (data == null || data.size() == 0) {
                return false;
            }

            File file = data.get(clickedPosition);
            onLongClickListener.onListItemLongClick(file);

            return true;
        }
    }
}

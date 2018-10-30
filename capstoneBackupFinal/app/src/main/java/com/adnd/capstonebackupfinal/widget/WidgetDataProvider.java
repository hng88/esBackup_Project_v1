package com.adnd.capstonebackupfinal.widget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.adnd.capstonebackupfinal.R;
import com.adnd.capstonebackupfinal.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by haymon on 2018-08-24.
 */

/**
 * WidgetDataProvider acts as the adapter for the collection view widget,
 * providing RemoteViews to the widget in the getViewAt method.
 */
public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = "WidgetDataProvider";

    List<String> collectionBackupConfigs = new ArrayList<String>();
    Context context = null;

    public WidgetDataProvider(Context context, Intent intent) {
        this.context = context;
    }

    private void initData() {
        Log.d(TAG, "initData(): UpdateWidgetService");
        collectionBackupConfigs.clear();

        //-get data from SharedPref
        // Obtain a reference to the SharedPreferences file for this app
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String currSharedPrefValue = prefs.getString(MainActivity.SHAREDPREF_KEY, "");

        if (currSharedPrefValue.isEmpty()) {
            return;
        }

        String[] backupConfigsList = currSharedPrefValue.split("\\n");

        if (backupConfigsList == null || !(backupConfigsList.length >= 1)) {
            return;
        }

        //-add the BackupConfigs to the collection Widget
        for (String backupConfig: backupConfigsList) {
            collectionBackupConfigs.add(backupConfig);
        }
    }

    @Override
    public void onCreate() {
        initData();
    }

    /**called on start and when notifyAppWidgetViewDataChanged is called*/
    @Override
    public void onDataSetChanged() {
        initData();
    }

    @Override
    public void onDestroy() {
        collectionBackupConfigs = null;
    }

    @Override
    public int getCount() {
        return collectionBackupConfigs.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //-Setup the Widget View via Widget Adapter
        RemoteViews view = new RemoteViews(context.getPackageName(),
                android.R.layout.simple_list_item_1);
        view.setTextViewText(android.R.id.text1, collectionBackupConfigs.get(position));
        view.setTextColor(android.R.id.text1, context.getResources().getColor(R.color.colorLightBlue));
        return view;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}

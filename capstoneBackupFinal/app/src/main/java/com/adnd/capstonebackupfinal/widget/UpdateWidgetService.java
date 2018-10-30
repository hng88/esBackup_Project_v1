package com.adnd.capstonebackupfinal.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.adnd.capstonebackupfinal.R;
import com.adnd.capstonebackupfinal.ui.MainActivity;

/**
 * Created by haymon on 2018-08-24.
 */

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class UpdateWidgetService extends IntentService {
    public static final String TAG = "UpdateWidgetService";

    public UpdateWidgetService() {
        super("UpdateWidgetService");
    }

    /** Runs on a separate background thread
     * Notify all Widgets that the View data has changed and
     * trigger the onUpdate()
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent()");

        String action = intent.getAction();

        if (!MainActivity.INTENT_ACTION_UPDATE_WIDGET.equals(action)) {
            return;
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, CollectionWidget.class));
        //Trigger data update to handle the ListView widgets and force a data refresh
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_listview);
        //Now update all widgets
        Intent updateIntent = new Intent(getApplicationContext(), CollectionWidget.class);
        //set Intent to trigger onUpdate() for the Widget
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        sendBroadcast(updateIntent);
    }
}

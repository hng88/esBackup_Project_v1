package com.adnd.capstonebackupfinal.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by haymon on 2018-08-24.
 */

/**
 * WidgetViewsService is the {@link RemoteViewsService} that will return our RemoteViewsFactory
 */
public class WidgetViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetDataProvider(this, intent);
    }
}

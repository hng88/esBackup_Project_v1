package com.adnd.capstonebackupfinal;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by haymon on 2018-09-30.
 */

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app, such as
 * the {@link Tracker}.
 */
public class MyApplication extends Application {
    private static final String TAG = MyApplication.class.getSimpleName();
    private static GoogleAnalytics sAnalytics;
    private static Tracker sTracker;

    @Override
    public void onCreate() {
        super.onCreate();

        //-Init Facebook Stetho.
        Stetho.initializeWithDefaults(this);
        //-Init GA.
        sAnalytics = GoogleAnalytics.getInstance(this);
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    public synchronized Tracker getDefaultTracker() {
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        if (sTracker == null) {
            sTracker = sAnalytics.newTracker(R.xml.global_tracker);
        }

        return sTracker;
    }

    /**
     * Tracking screen view
     * @param screenName screen name to be displayed on GA dashboard
     */
    public void trackScreenView(String screenName) {
        Tracker tracker = getDefaultTracker();

        // Set screen name.
        tracker.setScreenName(screenName);
        // Send a screen view.
        tracker.send(new HitBuilders.ScreenViewBuilder().build());

        sAnalytics.dispatchLocalHits();
    }
}

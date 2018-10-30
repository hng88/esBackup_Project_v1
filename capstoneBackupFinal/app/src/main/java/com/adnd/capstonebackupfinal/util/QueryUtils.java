package com.adnd.capstonebackupfinal.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by haymon on 2018-10-08.
 */

/**
 * Helper methods related to network.
 */
public final class QueryUtils {
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    private QueryUtils() {
    }

    /**
     * Check if the device has a network connection.
     */
    public static boolean isNetworkConnection(Context context) {
        boolean networkConnected = false;
        // If there is a network connection, fetch data
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            networkConnected = true;
        }
        return networkConnected;
    }
}


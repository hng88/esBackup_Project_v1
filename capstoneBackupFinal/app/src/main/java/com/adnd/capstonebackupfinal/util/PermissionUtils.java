package com.adnd.capstonebackupfinal.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by haymon on 2018-10-21.
 */

//-Used to support Runtime Permissions on 6.0+/API23+.
public final class PermissionUtils {
    private static final String TAG = "PermissionUtils";
    // REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS is an
    // app-defined int constant. The callback method gets the
    // result of the request.
    public static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 123;
    public static final List<String> permissionsNeeded = Collections.unmodifiableList(
        new ArrayList<String>() {{
            add(Manifest.permission.READ_EXTERNAL_STORAGE);
            add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }});

    private PermissionUtils() {
    }

    /**
     * Checks current status of User's permissions in Settings.
     * Checks for both the READ and WRITE permission for external storage.
     * Prompts for permissions if needed.
     * @param context
     * @param activity
     */
    public static void checkAndPromptPermissions(Context context, Activity activity) {
        Log.d(TAG, "checkAndPromptPermissions()");

        //-Read and Write permissions
        List<String> permissions = getOutstandingPermissions(context, permissionsNeeded);

        if (permissions.size() == 0) {
            //-All required permissions are given.
            Log.d(TAG, "All required permissions are given.");
            return;
        }

        //-Prompt for Permission(s).
        Log.d(TAG, "Prompt for Permission(s).");
        ActivityCompat.requestPermissions(activity,
                permissions.toArray(new String[permissions.size()]),
                REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
    }

    /**
     * Retrieves the current User permissions (in Settings
     * [User may revoke/enable permissions whenever])
     * @param context
     * @param permissions
     * @return
     */
    public static List<String> getOutstandingPermissions(Context context, List<String> permissions) {
        Log.d(TAG, "getOutstandingPermissions()");
        List<String> perms = new ArrayList<String>();
        for (String permission: permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                perms.add(permission);
            }
        }
        return perms;
    }

    /**
     * Only to support 6.0+/API23+
     * @return
     */
    public static boolean isRuntimePermissionsSupported() {
        return(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }
}

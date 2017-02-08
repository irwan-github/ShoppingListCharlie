package com.mirzairwan.shopping;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

/**
 * Created by Mirza Irwan on 13/1/17.
 * Copyright 2017, Mirza Irwan Bin Osman , All rights reserved.
 * Contact owner at mirza.irwan.osman@gmail.com
 */

public class PermissionHelper
{
        private static final int PERMISSION_GIVE_ITEM_PICTURE = 32;

        public static void setupStorageReadPermission(Activity activity)
        {
                ArrayList<String> permissionRequests = new ArrayList<>();

                if (!hasReadStoragePermission(activity))
                {
                        permissionRequests.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                }

                if (permissionRequests.size() > 0)
                {
                        ActivityCompat.requestPermissions(activity, permissionRequests.toArray(new String[permissionRequests.size()]), PERMISSION_GIVE_ITEM_PICTURE);
                }
        }

        protected static boolean hasReadStoragePermission(Activity activity)
        {
                return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }

        public static boolean isInternetUp(Context context)
        {
                ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                return (networkInfo != null && networkInfo.isConnected());
        }


}

package com.telenor.possumlib.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;

public class Has {
    public static boolean network(@NonNull Context context) {
        NetworkInfo networkInfo = ((ConnectivityManager)context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static String networkFailedReason(@NonNull Context context) {
        NetworkInfo networkInfo = ((ConnectivityManager)context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.getReason();
        }
        return null;
    }

    public static boolean wifi(@NonNull Context context) {
        ConnectivityManager mng = (ConnectivityManager)context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return mng.getActiveNetworkInfo() != null && ConnectivityManager.TYPE_WIFI == mng.getActiveNetworkInfo().getType();
    }

    public static boolean wifiConnection(@NonNull Context context) {
        ConnectivityManager mng = (ConnectivityManager)context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        WifiManager wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return mng.getActiveNetworkInfo() != null && mng.getActiveNetworkInfo().isConnected() && wifiManager.isWifiEnabled() && ConnectivityManager.TYPE_WIFI == mng.getActiveNetworkInfo().getType();
    }
}
package com.telenor.possumlib.utils;

import android.app.ActivityManager;
import android.content.Context;

public class ServiceUtil {
    /**
     * Method for checking whether a given service is running or not
     * @param context context
     * @param serviceClass the service class you want to check
     * @return true is service is NOT running
     */
    public static boolean isServiceNotRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return false;
            }
        }
        return true;
    }
}
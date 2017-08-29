package com.telenor.possumlib.abstractservices;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;

import net.danlew.android.joda.JodaTimeAndroid;

import org.opencv.android.OpenCVLoader;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Bottom level service to hide binding and initialize Joda
 */
public abstract class AbstractBasicService extends Service {
    protected static final String tag = AbstractBasicService.class.getName();
    protected AtomicBoolean taskStarted = new AtomicBoolean(false);

    @Override
    public int onStartCommand(Intent intent, int flags, int requestCode) {
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (correctArchitecture()) {
            try {
                if (!OpenCVLoader.initDebug(this)) Log.d(tag, "OpenCV not loaded");
                else Log.d(tag, "OpenCV loaded");
            } catch (final UnsatisfiedLinkError e) {
                Log.e(tag, "Failed to initialize openCV library:", e);
            }
        } else {
            Log.d(tag, "Invalid architecture. OpenCV not loaded");
        }
        JodaTimeAndroid.init(this);
    }

    private boolean correctArchitecture() {
        int OSNumber = Build.VERSION.SDK_INT;
        if (OSNumber < Build.VERSION_CODES.LOLLIPOP) {
            String archType = Build.CPU_ABI2;
            String archType2 = Build.CPU_ABI2;
            return archType.equals("armeabi-v7a") || archType2.equals("armeabi-v7a");
        } else {
            return OSNumber >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && hasArmeabi();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean hasArmeabi() {
        String[] supportedABIS = Build.SUPPORTED_ABIS;
        String[] supportedABIS_32_BIT = Build.SUPPORTED_32_BIT_ABIS;
        String[] supportedABIS_64_BIT = Build.SUPPORTED_64_BIT_ABIS;
        for (String abi : supportedABIS) {
            if ("armeabi-v7a".equals(abi)) return true;
        }
        for (String abi : supportedABIS_32_BIT) {
            if ("armeabi-v7a".equals(abi)) return true;
        }
        for (String abi : supportedABIS_64_BIT) {
            if ("armeabi-v7a".equals(abi)) return true;
        }
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

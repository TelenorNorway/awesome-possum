package com.telenor.possumlib.abstractservices;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
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
        try {
            if (!OpenCVLoader.initDebug(this)) Log.d(tag, "OpenCV not loaded");
            else Log.d(tag, "OpenCV loaded");
        } catch (final UnsatisfiedLinkError e) {
            Log.e(tag, "Failed to initialize openCV library:",e);
        }
        JodaTimeAndroid.init(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

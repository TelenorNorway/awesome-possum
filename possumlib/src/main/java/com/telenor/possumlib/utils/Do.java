package com.telenor.possumlib.utils;


import android.os.Handler;
import android.os.Looper;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Do {
    private static Handler handler = new Handler(Looper.getMainLooper());
    private static ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("BackgroundThread #%d").setPriority(Thread.MIN_PRIORITY).build());
    public static void inBackground(Runnable runnable) {
        executorService.submit(runnable);
    }
    public static void onMain(Runnable runnable) {
        handler.post(runnable);
    }
}

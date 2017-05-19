package com.telenor.possumlib.abstractdetectors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.interfaces.IOnReceive;

import java.util.List;

/***
 * Abstract BroadcastReceiver emulating the AbstractAndroidDetector
 */
public abstract class AbstractDetectorReceiver extends AbstractDetector implements IOnReceive {
    private BroadcastReceiver receiver;
    private boolean registered;
    private final IntentFilter intentFilter = new IntentFilter();

    protected AbstractDetectorReceiver(@NonNull Context context, @NonNull List<? extends String> intentFilterList, String identification, String secretKeyHash, EventBus eventBus) {
        super(context, identification, secretKeyHash, eventBus);
        for (Object filter : intentFilterList) {
            intentFilter.addAction((String)filter);
        }
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AbstractDetectorReceiver.this.onReceive(context, intent);
            }
        };
    }

    @Override
    public boolean startListening() {
        boolean listen = super.startListening();
        if (listen && !registered) {
            context().registerReceiver(receiver, intentFilter);
            registered = true;
        }
        return listen;
    }

    @Override
    public void stopListening() {
        super.stopListening();
        if (registered) {
            context().unregisterReceiver(receiver);
            registered = false;
        }
    }

    @VisibleForTesting
    protected IntentFilter intentFilter() {
        return intentFilter;
    }
}
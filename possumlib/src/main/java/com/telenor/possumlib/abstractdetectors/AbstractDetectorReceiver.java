package com.telenor.possumlib.abstractdetectors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;

import com.telenor.possumlib.interfaces.IOnReceive;
import com.telenor.possumlib.models.PossumBus;

import java.util.List;

/***
 * Abstract BroadcastReceiver emulating the AbstractAndroidDetector
 */
public abstract class AbstractDetectorReceiver extends AbstractDetector implements IOnReceive {
    private BroadcastReceiver receiver;
    private boolean registered;
    private final IntentFilter intentFilter = new IntentFilter();

    /**
     * An abstract detector with a broadcast receiver, able to intercept messages from outside
     * intents, either internal or external
     * @param context a valid android context
     * @param intentFilterList the intentFilter you want to limit the receiver to
     * @param uniqueUserId the unique user id
     * @param eventBus an event bus for internal messages
     * @param authenticating whether the detector is used for authentication or data gathering
     */
    protected AbstractDetectorReceiver(@NonNull Context context, @NonNull List<? extends String> intentFilterList, String uniqueUserId, PossumBus eventBus, boolean authenticating) {
        super(context, uniqueUserId, eventBus, authenticating);
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

    protected IntentFilter intentFilter() {
        return intentFilter;
    }
}
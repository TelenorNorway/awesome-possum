package com.telenor.possumlib.interfaces;

import android.hardware.TriggerEvent;

/**
 * Interface for handling trigger events from sensors
 */
public interface ITrigger {
    void onTrigger(TriggerEvent event);
}

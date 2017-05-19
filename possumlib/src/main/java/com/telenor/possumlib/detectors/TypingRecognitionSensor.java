package com.telenor.possumlib.detectors;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.abstractdetectors.AbstractEventDrivenDetector;
import com.telenor.possumlib.changeevents.BasicChangeEvent;
import com.telenor.possumlib.changeevents.TypingChangeEvent;
import com.telenor.possumlib.constants.DetectorType;

/***
 * Uses timing, accelerometer and gyroscope to detect angle, time between clicks and habitual writing when determining if correct person is writing.
 * Alternatively, using given authentication keyboard (AuthenticationIME service)
 */
public class TypingRecognitionSensor extends AbstractEventDrivenDetector {
    public TypingRecognitionSensor(Context context, String identification, String secretKeyHash, @NonNull EventBus eventBus) throws IllegalArgumentException {
        super(context, identification, secretKeyHash, eventBus);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isValidSet() {
        return true;
    }

    @Override
    public int detectorType() {
        return DetectorType.Keyboard;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    protected boolean storeWithInterval() {
        return false;
    }

    @Override
    public void eventReceived(BasicChangeEvent object) {
        if (object instanceof TypingChangeEvent) {
            sessionValues.add(object.message());
            super.eventReceived(object);
        }
    }
}
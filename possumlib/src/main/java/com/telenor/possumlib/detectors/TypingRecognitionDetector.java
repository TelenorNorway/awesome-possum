package com.telenor.possumlib.detectors;

import android.content.Context;
import android.support.annotation.NonNull;

import com.telenor.possumlib.abstractdetectors.AbstractEventDrivenDetector;
import com.telenor.possumlib.changeevents.PossumEvent;
import com.telenor.possumlib.changeevents.TypingChangeEvent;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.models.PossumBus;

/***
 * Uses timing, accelerometer and gyroscope to detect angle, time between clicks and habitual writing when determining if correct person is writing.
 * Alternatively, using given authentication keyboard (AuthenticationIME service)
 */
public class TypingRecognitionDetector extends AbstractEventDrivenDetector {
    /**
     * Constructor for the TypingRecognitionDetector
     *
     * @param context a valid android context
     * @param uniqueUserId the unique user id
     * @param eventBus an event bus for internal messages
     * @param authenticating whether the detector is used for authentication or data gathering
     */
    public TypingRecognitionDetector(Context context, String uniqueUserId, @NonNull PossumBus eventBus, boolean authenticating) {
        super(context, uniqueUserId, eventBus, authenticating);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public int detectorType() {
        return DetectorType.Keyboard;
    }

    @Override
    public String detectorName() {
        return "Keyboard";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String requiredPermission() {
        return null;
    }

    @Override
    protected boolean storeWithInterval() {
        return false;
    }

    @Override
    public void eventReceived(PossumEvent object) {
        if (object instanceof TypingChangeEvent) {
            super.eventReceived(object);
        }
    }
}
package com.telenor.possumlib.detectors;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.abstractdetectors.AbstractEventDrivenDetector;
import com.telenor.possumlib.constants.DetectorType;

import org.joda.time.DateTime;

/**
 * Used to detect touch movements on a given view
 */
public class GestureDetector extends AbstractEventDrivenDetector implements View.OnTouchListener {
    public GestureDetector(Context context, String identification, String secretKeyHash, @NonNull EventBus eventBus) throws IllegalArgumentException {
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
    public boolean isAvailable() {
        return true;
    }

    @Override
    protected boolean storeWithInterval() {
        return false;
    }

    @Override
    public int detectorType() {
        return DetectorType.Gesture;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        sessionValues.add(DateTime.now().getMillis() + " " + event.getAction() + " " + event.getX() + " " + event.getY() + " " + event.getPressure() + " " + event.getSize());
        return false;
    }
}
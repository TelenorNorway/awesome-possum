package com.telenor.possumlib.detectors;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;

import com.google.gson.JsonArray;
import com.telenor.possumlib.abstractdetectors.AbstractEventDrivenDetector;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.models.PossumBus;

/**
 * Used to detect touch movements on a given view
 */
public class GestureDetector extends AbstractEventDrivenDetector implements View.OnTouchListener {

    /**
     * Constructor for GestureDetector
     *
     * @param context a valid android context
     * @param eventBus an event bus for internal messages
     */
    public GestureDetector(Context context, @NonNull PossumBus eventBus) {
        super(context, eventBus);
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
    public String detectorName() {
        return "Gesture";
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        JsonArray array = new JsonArray();
        array.add(""+now());
        array.add(""+event.getAction());
        array.add(""+event.getX());
        array.add(""+event.getY());
        array.add(""+event.getPressure());
        array.add(""+event.getSize());
        sessionValues.add(array);
        if (!storeWithInterval()) {
            storeData();
        }
        return false;
    }
}
package com.telenor.possumlib.functionality;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.gson.JsonArray;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.interfaces.IPollComplete;
import com.telenor.possumlib.models.PossumBus;
import com.telenor.possumlib.utils.Get;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the start and stop of collecting data from the detectors
 */
public class GatheringFunctionality {
    private PossumBus eventBus;
    private boolean isGathering;
    private List<AbstractDetector> detectors = new ArrayList<>();

    private static final String tag = GatheringFunctionality.class.getName();

    public GatheringFunctionality() {
        eventBus = new PossumBus();
    }

    public void setDetectorsWithId(final @NonNull Context context, final @NonNull String uniqueUserId, final boolean authenticating, final IPollComplete listener) {
        stopGathering();
        detectors.clear();
        eventBus.clearAll();
        addDetectors(context, uniqueUserId, authenticating);
        for (AbstractDetector detector : detectors) {
            detector.setPollListener(listener);
        }
    }

    @VisibleForTesting
    protected void addDetectors(@NonNull Context context, @NonNull String uniqueUserId, boolean authenticating) {
        detectors.addAll(Get.Detectors(context, uniqueUserId, eventBus, authenticating));
    }

    public void startGathering() {
        if (!isGathering) {
            for (AbstractDetector detector : detectors) {
                detector.startListening();
            }
            isGathering = true;
        }
    }

    public void stopGathering() {
        if (isGathering) {
            for (AbstractDetector detector : detectors) {
                detector.terminate();
                detector.prepareUpload();
            }
            isGathering = false;
        }
    }

    public JsonArray detectorsAsJson() {
        JsonArray detectorObjects = new JsonArray();
        for (AbstractDetector detector : detectors) {
            detectorObjects.add(detector.toJson());
        }
        return detectorObjects;
    }
    public AbstractDetector acc() {
        for (AbstractDetector detector : detectors) {
            if (detector.detectorType() == DetectorType.Accelerometer) return detector;
        }
        return null;
    }

    public List<AbstractDetector> detectors() {
        return detectors;
    }

    public boolean isGathering() {
        return isGathering;
    }
}
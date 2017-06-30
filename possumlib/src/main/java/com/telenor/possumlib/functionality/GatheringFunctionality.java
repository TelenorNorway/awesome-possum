package com.telenor.possumlib.functionality;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.gson.JsonArray;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
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

    public GatheringFunctionality() {
        eventBus = new PossumBus();
    }

    public void setDetectorsWithId(@NonNull Context context, @NonNull String uniqueUserId, boolean authenticating, IPollComplete listener) {
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
        for (AbstractDetector detector : detectors) {
            detector.startListening();
        }
        isGathering = true;
    }

    public void stopGathering() {
        for (AbstractDetector detector : detectors) {
            detector.terminate();
            detector.prepareUpload();
        }
        isGathering = false;
    }

    public JsonArray detectorsAsJson() {
        JsonArray detectorObjects = new JsonArray();
        for (AbstractDetector detector : detectors) {
            detectorObjects.add(detector.toJson());
        }
        return detectorObjects;
    }

    public List<AbstractDetector> detectors() {
        return detectors;
    }

    public boolean isGathering() {
        return isGathering;
    }

    public void clearData() {
        for (AbstractDetector detector : detectors) {
            detector.clearData();
        }
    }
}
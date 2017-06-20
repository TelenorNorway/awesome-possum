package com.telenor.possumlib.functionality;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.gson.JsonArray;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.models.PossumBus;
import com.telenor.possumlib.utils.Get;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Handles the start and stop of collecting data from the detectors
 */
public class GatheringFunctionality {
    private PossumBus eventBus;
    private ConcurrentLinkedQueue<AbstractDetector> detectors = new ConcurrentLinkedQueue<>();

    public GatheringFunctionality() {
        eventBus = new PossumBus();
    }
    public void setDetectorsWithKurtId(@NonNull Context context, @NonNull String encryptedKurt, boolean authenticating) {
        stopGathering();
        detectors.clear();
        eventBus.clearAll();
        addDetectors(context, encryptedKurt, authenticating);
    }

    @VisibleForTesting
    protected void addDetectors(@NonNull Context context, @NonNull String encryptedKurt, boolean authenticating) {
        detectors.addAll(Get.Detectors(context, encryptedKurt, eventBus, authenticating));
    }

    public void startGathering() {
        for (AbstractDetector detector : detectors) {
            detector.startListening();
        }
    }

    public void stopGathering() {
        for (AbstractDetector detector : detectors) {
            detector.terminate();
            detector.prepareUpload();
        }
    }

    public JsonArray detectorsAsJson() {
        JsonArray detectorObjects = new JsonArray();
        for (AbstractDetector detector : detectors) {
            detectorObjects.add(detector.toJson());
        }
        return detectorObjects;
    }

    public Queue<AbstractDetector> detectors() {
        return detectors;
    }
}
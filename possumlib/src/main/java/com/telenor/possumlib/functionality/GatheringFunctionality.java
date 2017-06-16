package com.telenor.possumlib.functionality;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.models.PossumBus;
import com.telenor.possumlib.utils.Get;

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

    public JsonObject getCollectedObject() {
        JsonObject collectedObject = new JsonObject();
        JsonArray detectors = new JsonArray();
        // TODO: Fill with the collected data

        collectedObject.add("detectors", detectors);
        return collectedObject;
    }
}
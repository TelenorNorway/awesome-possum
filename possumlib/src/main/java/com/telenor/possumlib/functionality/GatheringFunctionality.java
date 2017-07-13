package com.telenor.possumlib.functionality;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.interfaces.IModelLoaded;
import com.telenor.possumlib.models.PossumBus;
import com.telenor.possumlib.utils.Get;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the start and stop of collecting data from the detectors
 */
public class GatheringFunctionality implements IModelLoaded {
    private boolean isGathering;
    private List<AbstractDetector> detectors = new ArrayList<>();

    private static final String tag = GatheringFunctionality.class.getName();

    public GatheringFunctionality(@NonNull Context context) {
        detectors.addAll(Get.Detectors(context, new PossumBus()));
    }

    public void setAuthenticationState(boolean isAuthenticating) {
        for (AbstractDetector detector : detectors) {
            detector.setAuthenticating(isAuthenticating);
        }
    }

    public void setUniqueUserId(String uniqueUserId) {
        for (AbstractDetector detector : detectors) {
            detector.setUniqueUser(uniqueUserId);
        }
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
                detector.stopListening();
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

    public List<AbstractDetector> detectors() {
        return detectors;
    }

    public boolean isGathering() {
        return isGathering;
    }

    public AbstractDetector detectorWithId(int detectorType) {
        for (AbstractDetector detector : detectors) {
            if (detector.detectorType() == detectorType) return detector;
        }
        return null;
    }

    @Override
    public void modelLoaded(int detectorType, Object model) {
        detectorWithId(detectorType).setModel(model);
    }
}
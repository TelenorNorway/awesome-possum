package com.telenor.possumlib.utils.face;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

/**
 * A custom vision tracker for faces. Note: no actual tracking is done atm.
 */
public class AwesomeFaceTracker extends Tracker<Face> {
    @Override
    public void onNewItem(int var1, Face var2) {
    }

    public void onUpdate(Detector.Detections<Face> var1, Face var2) {
    }

    public void onMissing(Detector.Detections<Face> var1) {
    }

    public void onDone() {
    }
}
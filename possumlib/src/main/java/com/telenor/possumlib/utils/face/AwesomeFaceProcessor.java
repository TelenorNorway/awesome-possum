package com.telenor.possumlib.utils.face;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.FocusingProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

/**
 * A custom processor created for the purpose of finding faces
 */
public class AwesomeFaceProcessor extends FocusingProcessor<Face> {
    public AwesomeFaceProcessor(Detector<Face> detector, Tracker<Face> tracker) {
        super(detector, tracker);
    }

    @Override
    public int selectFocus(Detector.Detections detections) {
        return 0;
    }
}

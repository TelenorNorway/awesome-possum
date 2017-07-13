package com.telenor.possumlib.utils;

import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.telenor.possumlib.interfaces.IFaceFound;

public class AwesomeFaceDetector extends Detector<Face> {
    private Detector<Face> mDelegate;
    private IFaceFound listener;

    public AwesomeFaceDetector(Detector<Face> delegate, IFaceFound listener) {
        mDelegate = delegate;
        this.listener = listener;
    }

    @Override
    public SparseArray<Face> detect(Frame frame) {
        SparseArray<Face> faces = mDelegate.detect(frame);
        if (faces.size() > 0 && listener != null) {
            listener.faceFound(faces.get(0),frame);
        }
        return faces;
    }

    public boolean isOperational() {
        return mDelegate.isOperational();
    }

//    public boolean setFocus(int id) {
//        return mDelegate.setFocus(id);
//    }
}
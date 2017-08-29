package com.telenor.possumlib.utils.face;

import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.telenor.possumlib.interfaces.IFaceFound;
import com.telenor.possumlib.utils.ImageUtils;

/**
 * A custom face detector, reporting any face found to the listener interface
 */
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
        // TODO: This class must be changed in master, it should NOT do the image processing here at all
        // When you need to change it, frame should be sent - not the byte array
        if (listener != null) {
            // TODO: This method should compress the image. Any way of compressing it to a scaled size directly?
            byte[] byteArray = ImageUtils.getBytesFromFrame(frame);
            listener.imageTaken(byteArray);
            if (faces.size() > 0) {
                listener.faceFound(faces.get(0), byteArray);
            }
        }
        return faces;
    }

    /**
     * Terminates the detector, releasing it
     */
    public void destroy() {
        if (mDelegate != null) {
            mDelegate.release();
        }
    }

    public boolean isOperational() {
        return mDelegate.isOperational();
    }
}
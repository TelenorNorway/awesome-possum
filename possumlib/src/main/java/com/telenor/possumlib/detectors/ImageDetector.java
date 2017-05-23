package com.telenor.possumlib.detectors;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.abstractdetectors.AbstractEventDrivenDetector;
import com.telenor.possumlib.asynctasks.AsyncFaceTask;
import com.telenor.possumlib.changeevents.BasicChangeEvent;
import com.telenor.possumlib.changeevents.ImageChangeEvent;
import com.telenor.possumlib.changeevents.MetaDataChangeEvent;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.tensorflow.TensorFlowInferenceInterface;

import org.joda.time.DateTime;

import java.util.Arrays;

/***
 * Uses camera to determine familiar places, face and face identity.
 */
public class ImageDetector extends AbstractEventDrivenDetector {
    private static final String tag = ImageDetector.class.getName();
    private static final String IMAGE_SINGLE = "IMAGE_SINGLE";
    private static final String IMAGE_CONTINUOUS = "IMAGE_CONTINUOUS";
    private static final String STOP_IMAGE_CAPTURE = "STOP_IMAGE_CAPTURE";
    private boolean modelLoaded = false;
    private int totalFaces;
    private AsyncFaceTask asyncFaceTask;
    public TensorFlowInferenceInterface tensorFlowInterface;

    public ImageDetector(Context context, String identification, String secretKeyHash, @NonNull EventBus eventBus) {
        super(context, identification, secretKeyHash, eventBus);
        totalFaces = 0;
        // Load tensorFlow interface
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.tensorFlowInterface = getTensorFlowInterface();
            try {
                if (!modelLoaded) {
                    Log.d(tag, "Starting initialize of TensorFlow");
                    if (tensorFlowInterface.initialize(context)) {
                        final int status = tensorFlowInterface.initializeTensorFlow(context.getAssets(),
                                "file:///android_asset/tensorflow_facerecognition.pb");
                        if (status != 0) {
                            Log.e(tag, "TF init status: " + status);
                            return;
                        }
                        modelLoaded = true;
                        Log.d(tag, "Model loaded");
                    } else {
                        Log.w(tag, "Failed to initialize TensorFlow");
                    }
                }
            } catch (Exception e) {
                Log.e(tag, "Failed to initialize TensorFlow:", e);
            }
        }
    }

    @VisibleForTesting
    protected TensorFlowInferenceInterface getTensorFlowInterface() {
        return new TensorFlowInferenceInterface();
    }

    @Override
    protected boolean storeWithInterval() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return Camera.getNumberOfCameras() > 0;
    }

    @Override
    public boolean isValidSet() {
        return true;
    }

    @Override
    public boolean isAvailable() {
        return isPermitted() && modelLoaded;
    }

    @Override
    public boolean isPermitted() {
        return ContextCompat.checkSelfPermission(context(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public int detectorType() {
        return DetectorType.Image;
    }

    @Override
    public String detectorName() {
        return "Image";
    }

    @Override
    public void eventReceived(BasicChangeEvent object) {
        if (!isAvailable() || !isEnabled()) return;
        if (object instanceof ImageChangeEvent) {
            ImageChangeEvent event = (ImageChangeEvent)object;
            if (asyncFaceTask != null) {
                asyncFaceTask.cancel(true);
                asyncFaceTask = null;
                Log.d(tag, "Stopping eventual image capture running");
            }
            switch (event.eventType()) {
                case IMAGE_SINGLE:
                    Log.d(tag, "Starting execution of single image snapshot");
                    snapImage(getFaceTask(false));
                    break;
                case IMAGE_CONTINUOUS:
                    Log.d(tag, "Starting execution of repeating image snapshots");
                    snapImage(getFaceTask(true));
                    break;
                case STOP_IMAGE_CAPTURE:
                    break;
                default:
                    Log.w(tag, "Unknown event received:" + event.eventType());
            }
        }
    }

    @VisibleForTesting
    protected AsyncFaceTask getFaceTask(boolean continuous) {
        try {
            return new AsyncFaceTask(context(), this,
                    Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT),
                    continuous);
        } catch (Exception e) {
            eventBus().post(new MetaDataChangeEvent(DateTime.now().getMillis()+" Camera was busy when taking picture"));
            Log.e(tag, "Could not open camera, aborting");
        }
        return null;
    }

    protected boolean snapImage(AsyncFaceTask asyncFaceTask) {
        if (asyncFaceTask != null) {
            try {
                this.asyncFaceTask = asyncFaceTask;
                this.asyncFaceTask.execute();
            } catch (Exception ignore) {
                return false;
            }
            return true;
        } else return false;
    }

    public void storeFace(float[] weights) {
        totalFaces += 1;
        String weight_list = Arrays.toString(weights);
        // Remove brackets and commas
        weight_list = weight_list.replace("[", "");
        weight_list = weight_list.replace("]", "");
        weight_list = weight_list.replace(",", "");
        // Write to data file
        sessionValues.add(DateTime.now().getMillis() + " " + weight_list);
        storeData();
    }

    public void resetTotalFaces() {
        totalFaces = 0;
    }

    public int getTotalFaces() {
        return totalFaces;
    }
}
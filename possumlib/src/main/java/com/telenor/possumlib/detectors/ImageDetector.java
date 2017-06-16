package com.telenor.possumlib.detectors;

import android.Manifest;
import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.asynctasks.AsyncFaceTask;
import com.telenor.possumlib.changeevents.MetaDataChangeEvent;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.models.PossumBus;
import com.telenor.possumlib.tensorflow.TensorFlowInferenceInterface;

import org.joda.time.DateTime;

import java.util.Arrays;

/***
 * Uses camera to determine familiar places, face and face identity.
 */
public class ImageDetector extends AbstractDetector {
    private static final String tag = ImageDetector.class.getName();
//    private static final String IMAGE_SINGLE = "IMAGE_SINGLE";
//    private static final String IMAGE_CONTINUOUS = "IMAGE_CONTINUOUS";
//    private static final String STOP_IMAGE_CAPTURE = "STOP_IMAGE_CAPTURE";
    private boolean modelLoaded = false;
    private int totalFaces;
    private AsyncFaceTask asyncFaceTask;
    private static final String fileName = "tensorflow_facerecognition.pb";
    private static final String fullPath = "file:///android_asset/"+fileName;
    public TensorFlowInferenceInterface tensorFlowInterface;

    /**
     * Constructor for Image detector
     * @param context a valid android context
     * @param encryptedKurt the encrypted kurt id
     * @param eventBus an event bus for internal messages
     * @param authenticating whether the detector is used for authentication or data gathering
     */
    public ImageDetector(Context context, String encryptedKurt, @NonNull PossumBus eventBus, boolean authenticating) {
        super(context, encryptedKurt, eventBus, authenticating);
        totalFaces = 0;
        // Load tensorFlow interface
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.tensorFlowInterface = getTensorFlowInterface();
            try {
                if (!modelLoaded) {
                    // Confirm file is found in assets before attempting to use it

                    String[] paths = context.getAssets().list("");
                    if (paths.length == 0) {
                        Log.w(tag, "No tensorFlow file found - no assets, ignoring image detector");
                        return;
                    } else {
                        boolean tensorFlowIsFound = false;
                        for (String file : paths) {
                            if (file.equals(fileName)) tensorFlowIsFound = true;
                        }
                        if (!tensorFlowIsFound) {
                            Log.w(tag, "No tensorFlow file is found in assets, ignoring image detector");
                            return;
                        }
                    }
                    Log.d(tag, "Starting initialize of TensorFlow");
                    if (tensorFlowInterface.initialize(context)) {
                        final int status = tensorFlowInterface.initializeTensorFlow(context.getAssets(),
                                fullPath);
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

    protected TensorFlowInferenceInterface getTensorFlowInterface() {
        return new TensorFlowInferenceInterface();
    }

    @Override
    public void stopListening() {
        if (asyncFaceTask != null) {
            asyncFaceTask.cancel(true);
            asyncFaceTask = null;
        }
        super.stopListening();
    }

    @Override
    public boolean isEnabled() {
        return Camera.getNumberOfCameras() > 0;
    }

    @Override
    public boolean isAvailable() {
        return modelLoaded && super.isAvailable();
    }

    @Override
    public String requiredPermission() {
        return Manifest.permission.CAMERA;
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
    public boolean startListening() {
        boolean listen = super.startListening();
        if (listen) {
            if (asyncFaceTask != null) {
                asyncFaceTask.cancel(true);
                asyncFaceTask = null;
                Log.d(tag, "Stopping eventual image capture running");
            }
            snapImage(getFaceTask());
        }
        return listen;
    }

    private AsyncFaceTask getFaceTask() {
        try {
            return new AsyncFaceTask(context(), this,
                    Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT));
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
    }

    public void resetTotalFaces() {
        totalFaces = 0;
    }

    public int getTotalFaces() {
        return totalFaces;
    }
}
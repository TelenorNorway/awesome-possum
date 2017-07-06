package com.telenor.possumlib.detectors;

import android.Manifest;
import android.content.Context;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonArray;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.asynctasks.AsyncFaceTask;
import com.telenor.possumlib.changeevents.MetaDataChangeEvent;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.interfaces.ITensorLoadComplete;
import com.telenor.possumlib.models.PossumBus;
import com.telenor.possumlib.tensorflow.TensorFlowInferenceInterface;

import java.io.IOException;

/***
 * Uses camera to determine familiar places, face and face identity.
 */
public class ImageDetector extends AbstractDetector implements ITensorLoadComplete {
    private static final String tag = ImageDetector.class.getName();
    private boolean modelLoaded = false;
    private boolean requestedListening = false;
    private int totalFaces;
    private AsyncFaceTask asyncFaceTask;
    private TensorLoad tensorLoad;
    private Handler handler = new Handler(Looper.getMainLooper());
    private static final String fileName = "tensorflow_facerecognition.pb";
    private static final String fullPath = "file:///android_asset/" + fileName;
    public TensorFlowInferenceInterface tensorFlowInterface;

    /**
     * Constructor for Image detector
     *
     * @param context        a valid android context
     * @param uniqueUserId   the unique user id
     * @param eventBus       an event bus for internal messages
     * @param authenticating whether the detector is used for authentication or data gathering
     */
    public ImageDetector(Context context, String uniqueUserId, @NonNull PossumBus eventBus, boolean authenticating) {
        super(context, uniqueUserId, eventBus, authenticating);
        totalFaces = 0;
        // Load tensorFlow interface
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                if (!modelLoaded) {
                    // Confirm file is found in assets before attempting to use it
                    tensorLoad = new TensorLoad(context, this);
                    tensorLoad.execute();
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
        cancelFaceSnap();
        cancelTensorLoad();
        requestedListening = false;
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
        return "image";
    }

    @Override
    public boolean startListening() {
        boolean listen = super.startListening();
        requestedListening = true;
        if (listen && isAvailable()) {
            cancelFaceSnap();
            snapImages(getFaceTask());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopListening();
                }
            }, listenInterval());
        }
        return listen;
    }

    private void cancelFaceSnap() {
        if (asyncFaceTask != null) {
            asyncFaceTask.cancel(true);
            asyncFaceTask = null;
            Log.d(tag, "Stopping eventual image capture running");
        }
    }

    private void cancelTensorLoad() {
        if (tensorLoad != null) {
            tensorLoad.cancel(true);
            tensorLoad = null;
        }
    }

    private long listenInterval() {
        return 5000;
    }

    private AsyncFaceTask getFaceTask() {
        try {
            return new AsyncFaceTask(context(), this,
                    Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT));
        } catch (Exception e) {
            eventBus().post(new MetaDataChangeEvent("Camera was busy when taking picture"));
            Log.e(tag, "Could not open camera, aborting");
        }
        return null;
    }

    protected boolean snapImages(AsyncFaceTask asyncFaceTask) {
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
        JsonArray array = new JsonArray();
        array.add("" + now());
        for (float weight : weights) {
            array.add("" + weight);
        }
        sessionValues.add(array);
    }

    @Override
    public void tensorFlowLoaded() {
        modelLoaded = true;
        if (requestedListening) {
            startListening();
        }
    }

    @Override
    public void tensorFlowFailedLoad() {
        modelLoaded = false;
    }

    private class TensorLoad extends AsyncTask<Void, Void, Boolean> {
        private ITensorLoadComplete listener;
        private Context context;

        TensorLoad(Context context, ITensorLoadComplete listener) {
            this.context = context;
            this.listener = listener;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String[] paths;
            try {
                paths = context.getAssets().list("");
            } catch (IOException e) {
                return false;
            }
            if (paths.length == 0) {
                Log.w(tag, "No tensorFlow file found - no assets, ignoring image detector");
                return false;
            } else {
                boolean tensorFlowIsFound = false;
                for (String file : paths) {
                    if (file.equals(fileName)) tensorFlowIsFound = true;
                }
                if (!tensorFlowIsFound) {
                    Log.w(tag, "No tensorFlow file is found in assets, ignoring image detector");
                    return false;
                }
            }
            Log.d(tag, "Starting initialize of TensorFlow");
            try {
                tensorFlowInterface = getTensorFlowInterface();
                if (tensorFlowInterface.initialize(context)) {
                    final int status = tensorFlowInterface.initializeTensorFlow(context.getAssets(),
                            fullPath);
                    if (status != 0) {
                        Log.e(tag, "TF init status: " + status);
                        return false;
                    }
                    Log.d(tag, "Model loaded");
                    return true;
                } else {
                    Log.w(tag, "Failed to initialize TensorFlow");
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) listener.tensorFlowLoaded();
            else listener.tensorFlowFailedLoad();
        }
    }

    public void resetTotalFaces() {
        totalFaces = 0;
    }

    public int getTotalFaces() {
        return totalFaces;
    }
}
package com.telenor.possumlib.detectors;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;
import com.google.gson.JsonArray;
import com.telenor.possumlib.AwesomePossum;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.constants.Messaging;
import com.telenor.possumlib.interfaces.IFaceFound;
import com.telenor.possumlib.models.PossumBus;
import com.telenor.possumlib.tensorflow.TensorFlowInferenceInterface;
import com.telenor.possumlib.utils.ImageUtils;
import com.telenor.possumlib.utils.Send;
import com.telenor.possumlib.utils.face.AwesomeFaceDetector;
import com.telenor.possumlib.utils.face.AwesomeFaceProcessor;
import com.telenor.possumlib.utils.face.AwesomeFaceTracker;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static com.telenor.possumlib.utils.ImageUtils.alignFace;
import static com.telenor.possumlib.utils.ImageUtils.bitmapToIntArray;

/***
 * Uses camera to determine face identity.
 */
public class ImageDetector extends AbstractDetector implements IFaceFound {
    private boolean requestedListening;
    private TensorFlowInferenceInterface tensorFlowInterface;
    private CameraSource cameraSource;
    private static final int PREVIEW_WIDTH = 640;
    private static final int PREVIEW_HEIGHT = 480;
    private static long lastFaceFound;
    private AwesomeFaceDetector faceDetector;
    private boolean supportedArchitecture = true;
    private static final long minTimeBetweenFaces = 2000; // Defines the time between faces in milliseconds

    private static final String tag = ImageDetector.class.getName();

    /**
     * Constructor for Image detector
     *
     * @param context  a valid android context
     * @param eventBus an event bus for internal messages
     */
    public ImageDetector(Context context, @NonNull PossumBus eventBus) {
        super(context, eventBus);
//        findOutIfArchitectureIsInvalid();
//        if (!supportedArchitecture) return;
        setupCameraSource();
    }

    private void setupCameraSource() {
        if (faceDetector != null && faceDetector.isReleased()) {
            faceDetector = getGoogleFaceDetector(context());
            if (cameraSource != null) {
                cameraSource.release();
            }
            cameraSource = null;
            cameraSource = new CameraSource.Builder(context(), faceDetector)
                    .setFacing(CameraSource.CAMERA_FACING_FRONT)
                    .setRequestedPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT)
                    .setRequestedFps(30)
                    .build();
        } else if (cameraSource == null) {
            faceDetector = getGoogleFaceDetector(context());
            cameraSource = new CameraSource.Builder(context(), faceDetector)
                    .setFacing(CameraSource.CAMERA_FACING_FRONT)
                    .setRequestedPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT)
                    .setRequestedFps(30)
                    .build();
        }
    }

    /**
     * Method for blocking out all architectures not of type armeabi-v7a
     */
    @SuppressWarnings("unused")
    private void findOutIfArchitectureIsInvalid() {
        int OSNumber = Build.VERSION.SDK_INT;
        if (OSNumber < Build.VERSION_CODES.LOLLIPOP) {
            String archType = Build.CPU_ABI2;
            String archType2 = Build.CPU_ABI2;
            supportedArchitecture = archType.equals("armeabi-v7a") || archType2.equals("armeabi-v7a");
        } else {
            supportedArchitecture = OSNumber >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && hasArmeabi();
        }
        Log.d(tag, "Architecture: Supports armeabi-v7a:"+supportedArchitecture);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean hasArmeabi() {
        String[] supportedABIS = Build.SUPPORTED_ABIS;
        String[] supportedABIS_32_BIT = Build.SUPPORTED_32_BIT_ABIS;
        String[] supportedABIS_64_BIT = Build.SUPPORTED_64_BIT_ABIS;
        for (String abi : supportedABIS) {
            if ("armeabi-v7a".equals(abi)) return true;
        }
        for (String abi : supportedABIS_32_BIT) {
            if ("armeabi-v7a".equals(abi)) return true;
        }
        for (String abi : supportedABIS_64_BIT) {
            if ("armeabi-v7a".equals(abi)) return true;
        }
        return false;
    }

    private AwesomeFaceDetector getGoogleFaceDetector(Context context) {
        FaceDetector.Builder builder = new FaceDetector.Builder(context);
        builder.setLandmarkType(FaceDetector.ALL_LANDMARKS);
        builder.setTrackingEnabled(false);
        builder.setMode(FaceDetector.FAST_MODE);
        AwesomeFaceDetector detector = new AwesomeFaceDetector(builder.build(), this);
        AwesomeFaceTracker tracker = new AwesomeFaceTracker();
        AwesomeFaceProcessor processor = new AwesomeFaceProcessor(detector, tracker);
        detector.setProcessor(processor);
        return detector;
    }

    @Override
    public boolean isEnabled() {
        return Camera.getNumberOfCameras() > 0;
    }

    @Override
    public void setModel(Object model) {
        this.tensorFlowInterface = (TensorFlowInferenceInterface) model;
        AwesomePossum.sendDetectorStatus(context());
        if (requestedListening) {
            startListening();
        }
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
    @SuppressWarnings("MissingPermission")
    public boolean startListening() {
        requestedListening = true;
        setupCameraSource();
        boolean listen = isAvailable() && super.startListening();
        if (listen) {
            try {
                cameraSource.start();
            } catch (Exception e) {
                Log.i(tag, "Failed:", e);
                stopListening();
                return false;
            }
        }
        return listen;
    }

    @Override
    public void stopListening() {
        if (isListening() && cameraSource != null) {
            cameraSource.stop();
        }
        super.stopListening();
    }

    @Override
    public void terminate() {
        super.terminate();
        if (cameraSource != null) {
            try {
                cameraSource.release();
            } catch (Exception e) {
                Log.e(tag, "Possum failed to release camera:",e);
            }
        }
        if (faceDetector != null) {
            faceDetector.destroy();
        }
    }

    /**
     * Quick method for finding whether the vision api is operational or not
     *
     * @return true if it is operational, false if not
     */
    private boolean isVisionOperational() {
        if (faceDetector == null) return false;
        boolean visionOperational = faceDetector.isOperational();
        if (!visionOperational) {
            Log.d(tag, "Google Vision is not available");
        }
        return visionOperational;
    }

    @Override
    public boolean isAvailable() {
        return supportedArchitecture && tensorFlowInterface != null && super.isAvailable() && isVisionOperational();
    }

    @Override
    public String detectorName() {
        return "image";
    }

    @Override
    public void faceFound(Face face, byte[] byteArray) {
        /*if ((now() - lastFaceFound) < minTimeBetweenFaces) {
            Log.i(tag, "Too short time between faces");
            return;
        }*/
        Bitmap image = ImageUtils.rotateBitmap(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length), -90);
        PointF leftEye = null;
        PointF rightEye = null;
        PointF mouth = null;
        if (face == null) return;
        List<Landmark> landmarks = face.getLandmarks();

        for (Landmark landmark : landmarks) {
            if (landmark.getType() == Landmark.LEFT_EYE) {
                leftEye = landmark.getPosition();
            } else if (landmark.getType() == Landmark.RIGHT_EYE) {
                rightEye = landmark.getPosition();
            } else if (landmark.getType() == Landmark.BOTTOM_MOUTH) {
                mouth = landmark.getPosition();
            }
        }
        if (leftEye == null || rightEye == null || mouth == null) {
            Log.d(tag, "Could not find enough landmarks, skipping face");
            return;
        }
        // Additional check for landmarks with invalid values
        else if (leftEye.x == 0.0 || rightEye.x == 0.0 || mouth.x == 0.0) {
            Log.d(tag, "Some landmarks found to be invalid, skipping face");
            return;
        }
        lastFaceFound = now();
        JsonArray array = new JsonArray();
        array.add("" + now());
        Bitmap scaledOutput = Bitmap.createScaledBitmap(alignFace(image, leftEye, rightEye, mouth), ImageUtils.BMP_WIDTH, ImageUtils.BMP_HEIGHT, false);

        // This part should not be done in pure library, only in POC
        Send.imageByteArrayIntent(context(), true, ImageUtils.getByteArrayFromImage(scaledOutput));

        float[] weights = tensorFlowInterface.getWeights(bitmapToIntArray(scaledOutput));
        for (float weight : weights) {
            array.add("" + weight);
        }
        sessionValues.add(array);
        Send.messageIntent(context(), Messaging.FACE_FOUND, ""+System.currentTimeMillis());
    }

    @Override
    public void imageTaken(byte[] byteArray) {
        Bitmap image = ImageUtils.getRotatedScaledBitmapFromByteArray(byteArray);
//        Bitmap image = ImageUtils.rotateBitmap(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length), -90);
//
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        Send.imageByteArrayIntent(context(), false, stream.toByteArray());
    }
}
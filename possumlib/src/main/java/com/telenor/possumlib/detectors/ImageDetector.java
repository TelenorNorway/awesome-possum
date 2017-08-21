package com.telenor.possumlib.detectors;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;
import com.google.gson.JsonArray;
import com.telenor.possumlib.AwesomePossum;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.interfaces.IFaceFound;
import com.telenor.possumlib.models.PossumBus;
import com.telenor.possumlib.tensorflow.TensorFlowInferenceInterface;
import com.telenor.possumlib.utils.AwesomeFaceDetector;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static com.telenor.possumlib.utils.ImageUtils.alignFace;
import static com.telenor.possumlib.utils.ImageUtils.bitmapToIntArray;
import static com.telenor.possumlib.utils.ImageUtils.rotateBitmap;

/***
 * Uses camera to determine familiar places, face and face identity.
 */
public class ImageDetector extends AbstractDetector implements IFaceFound {
    private boolean requestedListening;
    private TensorFlowInferenceInterface tensorFlowInterface;
    private CameraSource cameraSource;
    private static final int BMP_WIDTH = 96;
    private static final int BMP_HEIGHT = 96;
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
        faceDetector = getGoogleFaceDetector(context);
        faceDetector.setProcessor(
                new LargestFaceFocusingProcessor.Builder(faceDetector, null)
                        .build());
        cameraSource = new CameraSource.Builder(context, faceDetector)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT)
                .setRequestedFps(30)
                .build();
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
        builder.setMode(FaceDetector.ACCURATE_MODE);
        return new AwesomeFaceDetector(builder.build(), this);
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
        if (isListening()) {
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
    public void faceFound(Face face, Frame frame) {
        if ((now() - lastFaceFound) < minTimeBetweenFaces) {
            return;
        }
        int height = frame.getMetadata().getHeight();
        int width = frame.getMetadata().getWidth();
        YuvImage yuvimage = new YuvImage(frame.getGrayscaleImageData().array(), ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, width, height), 100, byteArrayOutputStream); // Where 100 is the quality of the generated jpeg
        byte[] jpegArray = byteArrayOutputStream.toByteArray();
        Bitmap image = rotateBitmap(BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length), -90);
        PointF leftEye = null;
        PointF rightEye = null;
        PointF mouth = null;
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
            Log.i(tag, "Could not find enough landmarks, skipping face");
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
        float[] weights = tensorFlowInterface.getWeights(bitmapToIntArray(Bitmap.createScaledBitmap(alignFace(image, leftEye, rightEye, mouth), BMP_WIDTH, BMP_HEIGHT, false)));
        for (float weight : weights) {
            array.add("" + weight);
        }
        sessionValues.add(array);
    }
}
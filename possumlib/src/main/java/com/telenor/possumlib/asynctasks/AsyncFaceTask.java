package com.telenor.possumlib.asynctasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;
import com.telenor.possumlib.changeevents.MetaDataChangeEvent;
import com.telenor.possumlib.detectors.ImageDetector;
import com.telenor.possumlib.exceptions.NotSupportedException;
import com.telenor.possumlib.models.PossumBus;
import com.telenor.possumlib.utils.Do;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import static com.telenor.possumlib.utils.ImageUtils.RGBArrayToIntArray;
import static com.telenor.possumlib.utils.ImageUtils.alignFace;
import static com.telenor.possumlib.utils.ImageUtils.bitmapToRGBArray;
import static com.telenor.possumlib.utils.ImageUtils.rotateBitmap;

public class AsyncFaceTask extends AsyncTask<Void, Void, Void> implements Camera.PictureCallback {
    private Camera camera;
    private static Semaphore semaphore;
    private static Semaphore semaphore_inner;

    private PossumBus eventBus = new PossumBus();
    private static final int maxRepeat = 20;
    private static final String tag = AsyncFaceTask.class.getName();
    private static final int BMP_WIDTH = 96;
    private static final int BMP_HEIGHT = 96;
    private byte[] data;
    private int totalNumberOfPictures;
    private int pictureNumber;
    private SurfaceTexture fakeSurface;
    private Handler timeoutHandler = new Handler();
    private FaceDetector faceDetector;
    private ImageDetector imageDetector;
    private Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isCancelled() && (getStatus() == AsyncTask.Status.RUNNING || getStatus() == AsyncTask.Status.PENDING)) {
                eventBus.post(new MetaDataChangeEvent(DateTime.now().getMillis()+" Cancelled AsyncFaceTask"));
                cancel(true);
            }
        }
    };

    public AsyncFaceTask(Context context, ImageDetector imageDetector, Camera camera) throws NotSupportedException {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        this.imageDetector = imageDetector;
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, cameraInfo);
        if (!cameraInfo.canDisableShutterSound) throw new NotSupportedException("Unable to disable shutter sound");
        this.camera = camera;
        this.totalNumberOfPictures = maxRepeat;
        semaphore = new Semaphore(0);
        semaphore_inner = new Semaphore(0);
        Log.i(tag, "AsyncFaceTask: Initializing");
        faceDetector = getGoogleFaceDetector(context);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        fakeSurface = new SurfaceTexture(0);
    }

    private FaceDetector getGoogleFaceDetector(Context context) {
        FaceDetector.Builder builder = new FaceDetector.Builder(context);
        builder.setLandmarkType(FaceDetector.ALL_LANDMARKS);
        builder.setTrackingEnabled(false);
        builder.setMode(FaceDetector.ACCURATE_MODE);
        return builder.build();
    }

    @Override
    protected void onCancelled() {
        Log.d(tag, "AsyncTask cancelled, releasing camera");
        this.camera.release();
        super.onCancelled();
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                this.camera.enableShutterSound(false);
            }
            Log.i(tag, "AsyncFaceTask: Background running:"+totalNumberOfPictures);
            for (pictureNumber = 0; pictureNumber < totalNumberOfPictures; pictureNumber++) {
                if (isCancelled()) {
                    Log.d(tag, "stopped taking pictures");
                    eventBus.post(new MetaDataChangeEvent(DateTime.now().getMillis()+" Picture series of " + totalNumberOfPictures + " interrupted at " + pictureNumber));
                    return null;
                }
                Log.d(tag, "Taking picture " + (pictureNumber + 1) + " of " + totalNumberOfPictures + " " + Thread.currentThread());
                camera.setPreviewTexture(fakeSurface);
                camera.startPreview();
                camera.takePicture(null, null, this);
                timeoutHandler.postDelayed(timeoutRunnable, 10000);
                semaphore.acquire();
                Do.inBackground(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap source = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Bitmap image = rotateBitmap(source, -90);
                        source.recycle();
                        // Get ImageDetector instance from AwesomePossum
                        // Scan image using dlib and return list of detected faces
                        List<Bitmap> faces = detectFaces(image);
                        Log.i(tag, faces.size() + " valid faces returned");
                        image.recycle();
                        // Convert faces to int arrays
                        int rgbArray[][][];
                        // For every detected face, use tensorflow to get weights and write to file
                        for (int i = 0; i < faces.size(); i++) {
                            Log.i(tag, "Processing face number " + (i + 1) + " using TensorFlow");
                            rgbArray = bitmapToRGBArray(faces.get(i));
                            //savePicture(faces.get(i), time + "_" + i + ".png");
                            faces.get(i).recycle();
                            imageDetector.storeFace(imageDetector.tensorFlowInterface.getWeights(
                                    RGBArrayToIntArray(rgbArray, BMP_WIDTH)));
                            Log.i(tag, "Face weights written to file");
                        }
                        imageDetector.storeData();
                        semaphore_inner.release();
                    }
                });
                semaphore_inner.acquire();
                timeoutHandler.removeCallbacks(timeoutRunnable);
            }

        } catch (InterruptedException e) {
            Log.w(tag, "Semaphore interrupted - cancelled?");
        } catch (IOException e) {
            Log.e(tag, "IOException:", e);
        } catch (Exception e) {
            Log.e(tag, "Failed something:", e);
        }
        return null;
    }

    private List<Bitmap> detectFaces(Bitmap image) {
        // Detect faces
        Frame frame = new Frame.Builder().setBitmap(image).build();
        SparseArray<Face> faceList = faceDetector.detect(frame);
        Log.i(tag, faceList.size() + " faces found");
        // For every face, crop and scale
        Bitmap croppedFace;
        List<Bitmap> faces = new ArrayList<>();
        int count = 0;
        for (int i = 0; i < faceList.size(); i++) {
            Log.d(tag, "Validating face number " + (i + 1));
            Face face = faceList.get(faceList.keyAt(i));
            count = count + 1;
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
                break;
            }
            // Additional check for landmarks with invalid values
            else if (leftEye.x == 0.0 || rightEye.x == 0.0 || mouth.x == 0.0) {
                Log.d(tag, "Some landmarks found to be invalid, skipping face");
                break;
            }
            Log.d(tag, "Landmarks found");

            Bitmap alignedFace = alignFace(image, leftEye, rightEye, mouth);
            faces.add(Bitmap.createScaledBitmap(alignedFace, BMP_WIDTH, BMP_HEIGHT, false));
            alignedFace.recycle();
        }
        return faces;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        if (pictureNumber >= totalNumberOfPictures - 1) {
            camera.release();
        }
        this.data = data;
        semaphore.release();
    }
}
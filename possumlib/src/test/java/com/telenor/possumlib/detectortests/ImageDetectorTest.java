package com.telenor.possumlib.detectortests;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.hardware.Camera;
import android.os.Build;

import com.telenor.possumlib.JodaInit;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.asynctasks.AsyncFaceTask;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.detectors.ImageDetector;
import com.telenor.possumlib.models.PossumBus;
import com.telenor.possumlib.tensorflow.TensorFlowInferenceInterface;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowCamera;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PossumTestRunner.class)
public class ImageDetectorTest {
    private ImageDetector imageDetector;
    //    private int permission;
    @Mock
    private AssetManager mockedAssets;
    private PossumBus eventBus;
    @Mock
    private TensorFlowInferenceInterface mockedTensorFlow;
    @Mock
    private Context mockedContext;
    @Mock
    private AssetManager mockedAssetManager;
    @Mock
    private AsyncFaceTask mockedAsyncFaceTask;
    private Camera.CameraInfo cameraInfo;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        JodaInit.initializeJodaTime();
        eventBus = new PossumBus();
        when(mockedTensorFlow.initialize(any(Context.class))).thenReturn(true);
        when(mockedTensorFlow.initializeTensorFlow(any(AssetManager.class), anyString())).thenReturn(0);
        when(mockedContext.getAssets()).thenReturn(mockedAssetManager);
        String[] paths = new String[]{"tensorflow_facerecognition.pb"};
        when(mockedAssetManager.list(anyString())).thenReturn(paths);
        when(mockedContext.getFilesDir()).thenReturn(RuntimeEnvironment.application.getFilesDir());
        when(mockedContext.checkPermission(eq(Manifest.permission.CAMERA), anyInt(), anyInt())).thenReturn(PackageManager.PERMISSION_GRANTED);
        cameraInfo = new Camera.CameraInfo();
        cameraInfo.canDisableShutterSound = true;
//        ShadowApplication.getInstance().grantPermissions(Manifest.permission.CAMERA);
        ShadowCamera.addCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, cameraInfo);
        imageDetector = new ImageDetector(mockedContext, "fakeUnique", eventBus, false) {
            @Override
            protected TensorFlowInferenceInterface getTensorFlowInterface() {
                return mockedTensorFlow;
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        imageDetector.terminate();
        ShadowCamera.clearCameraInfo();
    }

    @Test
    public void testInitialization() throws Exception {
        Assert.assertNotNull(imageDetector);
    }

    @Test
    public void testDefaultValues() throws Exception {
        int numberOfCameras = Camera.getNumberOfCameras();
        Assert.assertEquals(1, numberOfCameras);
        Assert.assertTrue(imageDetector.isEnabled());
        Assert.assertEquals(DetectorType.Image, imageDetector.detectorType());
        Assert.assertTrue(imageDetector.isValidSet());
    }

    @Test
    public void testTensorFlowSuccededToLoadModel() throws Exception {
//        Assert.assertTrue(imageDetector.modelDownloaded());
    }

    @Test
    public void testTensorFlowFailedToLoadModel() throws Exception {
        when(mockedTensorFlow.initializeTensorFlow(any(AssetManager.class), anyString())).thenReturn(1);
        imageDetector = new ImageDetector(RuntimeEnvironment.application, "fakeUnique", eventBus, false) {
            @Override
            protected TensorFlowInferenceInterface getTensorFlowInterface() {
                return mockedTensorFlow;
            }
        };
//        Assert.assertFalse(imageDetector.modelDownloaded());

    }

    @Test
    public void testTensorFlowFailsToInitialize() throws Exception {
        when(mockedTensorFlow.initialize(any(Context.class))).thenReturn(false);
        imageDetector = new ImageDetector(RuntimeEnvironment.application, "fakeUnique", eventBus, false) {
            @Override
            protected TensorFlowInferenceInterface getTensorFlowInterface() {
                return mockedTensorFlow;
            }
        };
//        Assert.assertFalse(imageDetector.modelDownloaded());
    }

    @Test
    public void testTensorFlowFailsToFindModel() throws Exception {
        when(mockedTensorFlow.initializeTensorFlow(any(AssetManager.class), anyString())).thenThrow(new RuntimeException("Failed to find file"));
        imageDetector = new ImageDetector(RuntimeEnvironment.application, "fakeUnique", eventBus, false) {
            @Override
            protected TensorFlowInferenceInterface getTensorFlowInterface() {
                return mockedTensorFlow;
            }
        };
//        Assert.assertFalse(imageDetector.modelDownloaded());
    }

    @Test
    public void testSnapImageMethod() throws Exception {
        imageDetector = new ImageDetector(RuntimeEnvironment.application, "fakeUnique", eventBus, false) {
            @Override
            protected TensorFlowInferenceInterface getTensorFlowInterface() {
                return mockedTensorFlow;
            }
        };
        Method snapMethod = ImageDetector.class.getDeclaredMethod("snapImage", AsyncFaceTask.class);
        snapMethod.setAccessible(true);
        Assert.assertTrue((boolean) snapMethod.invoke(imageDetector, mockedAsyncFaceTask));
        verify(mockedAsyncFaceTask).execute();
    }

    @Test
    public void testSnapImageFails() throws Exception {
        imageDetector = new ImageDetector(RuntimeEnvironment.application, "fakeUnique", eventBus, false) {
            @Override
            protected TensorFlowInferenceInterface getTensorFlowInterface() {
                return mockedTensorFlow;
            }
        };
        Method snapMethod = ImageDetector.class.getDeclaredMethod("snapImage", AsyncFaceTask.class);
        snapMethod.setAccessible(true);
        when(mockedAsyncFaceTask.execute()).thenThrow(new RuntimeException("test"));
        Assert.assertFalse((boolean) snapMethod.invoke(imageDetector, mockedAsyncFaceTask));
        verify(mockedAsyncFaceTask).execute();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testStartListenRegistersForEvent() throws Exception {
//        Field eventField = EventBus.class.getDeclaredField("subscribersPerTopic");
//        eventField.setAccessible(true);
//        HashMap<String, Set<EventSubscriber>> subscribersBefore = (HashMap<String, Set<EventSubscriber>>) eventField.get(EventBus.getInstance());
//        Set<EventSubscriber> before = subscribersBefore.get(ImageDetector.IMAGE_EVENT);
//        Assert.assertNull(before);
//        Assert.assertTrue(imageDetector.startListening());
//
//        HashMap<String, Set<EventSubscriber>> subscribersAfter = (HashMap<String, Set<EventSubscriber>>) eventField.get(EventBus.getInstance());
//        Set<EventSubscriber> after = subscribersAfter.get(ImageDetector.IMAGE_EVENT);
//        Assert.assertEquals(1, after.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testStopListeningNoticesEventStop() throws Exception {
//        Field eventField = EventBus.class.getDeclaredField("subscribersPerTopic");
//        eventField.setAccessible(true);
        Assert.assertTrue(imageDetector.startListening());
        imageDetector.stopListening();
//        HashMap<String, Set<EventSubscriber>> subscribersAfter = (HashMap<String, Set<EventSubscriber>>) eventField.get(EventBus.getInstance());
//        Set<EventSubscriber> after = subscribersAfter.get(ImageDetector.IMAGE_EVENT);
//        Assert.assertEquals(0, after.size());
    }

    @Test
    public void testAvailabilityWhenPermissionDenied() throws Exception {
        when(mockedContext.checkPermission(eq(Manifest.permission.CAMERA), anyInt(), anyInt())).thenReturn(PackageManager.PERMISSION_DENIED);
//        ShadowApplication.getInstance().denyPermissions(Manifest.permission.CAMERA);
        Assert.assertFalse(imageDetector.isAvailable());
    }

    @Test
    public void testModelLoadedInAvailability() throws Exception {
        Assert.assertTrue(imageDetector.isAvailable());
        Field modelLoadedField = ImageDetector.class.getDeclaredField("modelLoaded");
        modelLoadedField.setAccessible(true);
        modelLoadedField.set(imageDetector, false);
        Assert.assertFalse(imageDetector.isAvailable());
    }

    @Test
    public void testAvailabilityWhenPermissionGranted() throws Exception {
        Assert.assertTrue(imageDetector.isAvailable());
    }

//    @Test
//    public void testGetFaceTask() throws Exception {
//        imageDetector = new ImageDetector(RuntimeEnvironment.application, "fakeUnique", "fakeId", eventBus) {
//            @Override
//            protected TensorFlowInferenceInterface getTensorFlowInterface() {
//                return mockedTensorFlow;
//            }
//            @Override
//            protected boolean snapImage(AsyncFaceTask asyncFaceTask) {
//                return true;
//            }
//        };
//        Method faceTask = ImageDetector.class.getDeclaredMethod("getFaceTask", boolean.class);
//        faceTask.setAccessible(true);
//        Assert.assertNotNull(faceTask.invoke(imageDetector, true));
//    }

//    @Test
//    public void testSingleImageEvent() throws Exception {
////        imageDetector.objectChanged(new EventObject(ImageDetector.IMAGE_SINGLE, null));
//        Assert.assertEquals(0, isContinousRequest);
//        verify(mockedTensorFlow).initialize(any(Context.class));
//        verify(mockedTensorFlow).initializeTensorFlow(any(AssetManager.class), anyString());
//        // TODO: Need to get the cameraInfo to store
//
//        verify(mockedAsyncFaceTask).execute();
//    }

//    @Test
//    public void testContinuousImageEvent() throws Exception {
////        imageDetector.objectChanged(new EventObject(ImageDetector.IMAGE_CONTINUOUS, null));
//        Assert.assertEquals(1, isContinousRequest);
//        verify(mockedTensorFlow).initialize(any(Context.class));
//        verify(mockedTensorFlow).initializeTensorFlow(any(AssetManager.class), anyString());
//        verify(mockedAsyncFaceTask).execute();
//    }

    @Test
    public void testStopImageCaptureEvent() throws Exception {
//        imageDetector.objectChanged(new EventObject(ImageDetector.STOP_IMAGE_CAPTURE, null));
    }

    @Test
    public void testFaceFunctions() throws Exception {
        Assert.assertEquals(0, imageDetector.getTotalFaces());
        float[] fakeWeights = new float[]{1.1f, 1.2f, 1.3f};
        imageDetector.storeFace(fakeWeights);
        imageDetector.storeFace(fakeWeights);
        Assert.assertEquals(2, imageDetector.getTotalFaces());
        imageDetector.resetTotalFaces();
        Assert.assertEquals(0, imageDetector.getTotalFaces());
    }
}
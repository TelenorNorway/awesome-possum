package com.telenor.possumlib.asynctasks;

import android.content.Context;
import android.hardware.Camera;

import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.detectors.ImageDetector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowCamera;

@RunWith(PossumTestRunner.class)
public class AsyncFaceTaskTest {
    private AsyncFaceTask asyncFaceTask;
    @Mock
    private Context mockedContext;
    @Mock
    private ImageDetector mockedImageDetector;
    private Camera camera;
    private ShadowCamera shadowCamera;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraInfo.canDisableShutterSound = true;
        ShadowCamera.addCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, cameraInfo);
        camera = ShadowCamera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        shadowCamera = Shadows.shadowOf(camera);
//        asyncFaceTask = new AsyncFaceTask(mockedContext, mockedImageDetector, camera, false);
    }

    @After
    public void tearDown() throws Exception {
        asyncFaceTask = null;
        shadowCamera = null;
        camera = null;
    }

    @Test
    public void testInit() throws Exception {
//        Assert.assertNotNull(asyncFaceTask);
    }
}
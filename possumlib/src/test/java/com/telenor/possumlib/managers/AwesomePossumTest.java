package com.telenor.possumlib.managers;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;

import com.telenor.possumlib.AwesomePossum;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.services.CollectorService;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowCamera;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(PossumTestRunner.class)
public class AwesomePossumTest {
    private SharedPreferences fakePreferences;
    @Mock
    private Context mockedContext;
    @Mock
    private PackageManager mockedPackageManager;
    @Mock
    private PackageInfo mockedPackageInfo;
    @Mock
    private LocationManager mockedLocationManager;
    @Mock
    private ConnectivityManager mockedConnectivityManager;
    @Mock
    private SensorManager mockedSensorManager;
    @Mock
    private AlarmManager mockedAlarmManager;
    @Mock
    private ActivityManager mockedActivityManager;

    @SuppressWarnings("WrongConstant")
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraInfo.canDisableShutterSound = true;
        ShadowCamera.addCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, cameraInfo);
        when(mockedContext.getSystemService(Context.ACTIVITY_SERVICE)).thenReturn(mockedActivityManager);
        when(mockedContext.getSystemService(Context.ALARM_SERVICE)).thenReturn(mockedAlarmManager);
        when(mockedContext.getSystemService(Context.LOCATION_SERVICE)).thenReturn(mockedLocationManager);
        when(mockedContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockedConnectivityManager);
        when(mockedContext.getSystemService(Context.SENSOR_SERVICE)).thenReturn(mockedSensorManager);
        when(mockedContext.getPackageManager()).thenReturn(mockedPackageManager);
        when(mockedContext.getApplicationContext()).thenReturn(mockedContext);
        when(mockedContext.getFilesDir()).thenReturn(RuntimeEnvironment.application.getFilesDir());
        when(mockedPackageManager.getPackageInfo(anyString(), eq(0))).thenReturn(mockedPackageInfo);
        when(mockedContext.getPackageName()).thenReturn(RuntimeEnvironment.application.getPackageName());
        fakePreferences = RuntimeEnvironment.application.getSharedPreferences("test", Context.MODE_PRIVATE);
        when(mockedContext.getSharedPreferences(anyString(), anyInt())).thenReturn(fakePreferences);
        AwesomePossum.terminate(mockedContext);
    }
    @After
    public void tearDown() throws Exception {
        fakePreferences.edit().clear().apply();
        ShadowCamera.clearCameraInfo();
    }

    @Test
    public void testInit() throws Exception {
//        AwesomePossum.init(mockedContext, 1, 2, 3, "test", DummyPossumActivity.class, Fragment.class);
    }

    @Test
    public void testLearningBeforeInitialization() throws Exception {
        try {
            AwesomePossum.isLearning();
            Assert.fail("Should not reach here, PreferenceUtil must be initialized first");
        } catch (Exception e) {
            Assert.assertEquals("Must initialize PreferenceUtil first", e.getMessage());
        }
    }

    @Test
    public void testStartGatherBeforeInitDoesNothing() throws Exception {
        Context mockedContext = getMockForServiceWithBackgroundRunning(false, CollectorService.class);
//        AwesomePossum.setGathering(mockedContext, true);
//        AwesomePossum.startGatherServiceIfDesired(mockedContext);
        verify(mockedContext, never()).startService(any(Intent.class));
    }

    @Test
    public void testStartGatherAfterInitStartsService() throws Exception {
        Context mockedContext = getMockForServiceWithBackgroundRunning(false, CollectorService.class);
//        AwesomePossum.init(mockedContext, 1, 1, 1, "test", DummyPossumActivity.class, Fragment.class);
//        AwesomePossum.startGatherServiceIfDesired(mockedContext);
        verify(mockedContext, times(1)).startService(any(Intent.class));
    }

    @Test
    public void testStartGatherAfterInitButBackgroundAlreadyRunningDoesNotStartService() throws Exception {
        Context mockedContext = getMockForServiceWithBackgroundRunning(true, CollectorService.class);
//        AwesomePossum.init(mockedContext, 1, 1, 1, "test", DummyPossumActivity.class, Fragment.class);
//        AwesomePossum.startGatherServiceIfDesired(mockedContext);
        verify(mockedContext, never()).startService(any(Intent.class));
    }

    private Context getMockForServiceWithBackgroundRunning(boolean runBackgroundService, Class serviceClass) {
        List<ActivityManager.RunningServiceInfo> mockedList = new ArrayList<>();
        if (runBackgroundService) {
            ActivityManager.RunningServiceInfo mockedRunningService = mock(ActivityManager.RunningServiceInfo.class);
            ComponentName mockedComponentName = mock(ComponentName.class);
            when(mockedComponentName.getClassName()).thenReturn(serviceClass.getName());
            mockedRunningService.service = mockedComponentName;
            mockedList.add(mockedRunningService);
        }
        when(mockedActivityManager.getRunningServices(Integer.MAX_VALUE)).thenReturn(mockedList);
        return mockedContext;
    }

    @Test
    public void testDangerousPermissions() throws Exception {
//        List<String> permissions = Arrays.asList(AwesomePossum.dangerousPermissions());
//        Assert.assertTrue(permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION));
//        Assert.assertTrue(permissions.contains(Manifest.permission.CAMERA));
//        Assert.assertTrue(permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE));
        // TODO: Add the below permissions a later time
//        Assert.assertTrue(permissions.contains(Manifest.permission.READ_PHONE_STATE));
//        Assert.assertTrue(permissions.contains(Manifest.permission.RECORD_AUDIO));
    }

    @Test
    public void testTerminate() throws Exception {
//        AwesomePossum.init(mockedContext, 1, 1, 1, "test", DummyPossumActivity.class, Fragment.class);
        AwesomePossum.terminate(mockedContext);
    }
}
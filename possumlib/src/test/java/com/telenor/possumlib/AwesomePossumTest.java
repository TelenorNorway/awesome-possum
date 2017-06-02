package com.telenor.possumlib;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.telenor.possumlib.exceptions.GatheringNotAuthorizedException;

import junit.framework.Assert;

import net.danlew.android.joda.JodaTimeAndroid;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowCamera;

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
        JodaTimeAndroid.init(RuntimeEnvironment.application);
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
        AwesomePossum.stopListening(mockedContext);
    }
    @After
    public void tearDown() throws Exception {
        fakePreferences.edit().clear().apply();
        ShadowCamera.clearCameraInfo();
    }

    @Test
    public void testListenBeforeAuthorized() throws Exception {
        try {
            AwesomePossum.startListening(mockedContext);
            verify(mockedContext, never()).startService(any(Intent.class));
            Assert.fail("Should not have reached this space");
        } catch (GatheringNotAuthorizedException ignore) {
        }
    }

    @Test
    public void testListenAfterAuthorized() throws Exception {
        NetworkInfo mockedNetworkInfo = mock(NetworkInfo.class);
        when(mockedConnectivityManager.getActiveNetworkInfo()).thenReturn(mockedNetworkInfo);
        when(mockedNetworkInfo.isConnected()).thenReturn(true);
        verify(mockedContext, never()).startService(any(Intent.class));
        Assert.assertTrue(AwesomePossum.authorizeGathering(mockedContext, "fakeKurt", "fakeBucketKey"));
        verify(mockedContext, times(1)).startService(any(Intent.class));
        AwesomePossum.startListening(mockedContext);
        verify(mockedContext, times(2)).startService(any(Intent.class));
    }

    @Test
    public void testDangerousPermissions() throws Exception {
        // TODO: Should I make method public to test or ignore?
//        List<String> permissions = Arrays.asList(AwesomePossum.dangerousPermissions());
//        Assert.assertTrue(permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION));
//        Assert.assertTrue(permissions.contains(Manifest.permission.CAMERA));
//        Assert.assertTrue(permissions.contains(Manifest.permission.RECORD_AUDIO));
    }
}
package com.telenor.possumlib.detectortests;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.Process;

import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.detectors.SatelliteDetector;
import com.telenor.possumlib.models.PossumBus;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(PossumTestRunner.class)
public class SatelliteDetectorTest {
    private SatelliteDetector satelliteDetector;
    private LocationManager mockedLocationManager;
    private Context mockedContext;
    private GpsStatus mockedGpsStatus;
    private PossumBus eventBus;

    @Before
    public void setUp() throws Exception {
        mockedContext = Mockito.mock(Context.class);
        eventBus = new PossumBus();
        mockedLocationManager = Mockito.mock(LocationManager.class);
        List<String> allProviders = new ArrayList<>();
        allProviders.add(LocationManager.GPS_PROVIDER);
        allProviders.add(LocationManager.NETWORK_PROVIDER);
        when(mockedLocationManager.getAllProviders()).thenReturn(allProviders);
        mockedGpsStatus = Mockito.mock(GpsStatus.class);
        setLocationDetectorWith(true, PackageManager.PERMISSION_GRANTED);
        satelliteDetector = new SatelliteDetector(mockedContext, "encryptedFake", eventBus, false);
    }

    private void setLocationDetectorWith(boolean enabled, int permission) {
        List<String> allProviders = new ArrayList<>();
        if (enabled) {
            allProviders.add(LocationManager.GPS_PROVIDER);
            allProviders.add(LocationManager.NETWORK_PROVIDER);
        }
        when(mockedLocationManager.getAllProviders()).thenReturn(allProviders);
        when(mockedContext.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, android.os.Process.myPid(), Process.myUid())).thenReturn(permission);
        when(mockedContext.getSystemService(Context.LOCATION_SERVICE)).thenReturn(mockedLocationManager);
        when(mockedLocationManager.getGpsStatus(Mockito.any(GpsStatus.class))).thenReturn(mockedGpsStatus);
    }

    @After
    public void tearDown() throws Exception {
        satelliteDetector = null;
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertNotNull(satelliteDetector);

    }

    @Test
    public void testDefaults() throws Exception {
        Assert.assertEquals(DetectorType.GpsStatus, satelliteDetector.detectorType());
        Assert.assertFalse(satelliteDetector.isEnabled());
        Assert.assertEquals("Satellites", satelliteDetector.detectorName());
        Method storeWithIntervalMethod = SatelliteDetector.class.getDeclaredMethod("storeWithInterval");
        storeWithIntervalMethod.setAccessible(true);
        Assert.assertTrue((boolean)storeWithIntervalMethod.invoke(satelliteDetector));
    }

    @Test
    public void testIsPermittedWhenNotGranted() throws Exception {
        when(mockedContext.checkPermission(eq(Manifest.permission.ACCESS_FINE_LOCATION), anyInt(), anyInt())).thenReturn(PackageManager.PERMISSION_DENIED);
        Assert.assertFalse(satelliteDetector.isPermitted());
        Assert.assertFalse(satelliteDetector.isAvailable());
    }

    @Test
    public void testIsPermittedWhenGranted() throws Exception {
        when(mockedContext.checkPermission(eq(Manifest.permission.ACCESS_FINE_LOCATION), anyInt(), anyInt())).thenReturn(PackageManager.PERMISSION_GRANTED);
        Assert.assertTrue(satelliteDetector.isPermitted());
        Assert.assertTrue(satelliteDetector.isAvailable());
    }

    @Test
    public void testNotAbleToStart() throws Exception {
        Assert.assertFalse(satelliteDetector.isListening());
        Assert.assertFalse(satelliteDetector.startListening());
        Assert.assertFalse(satelliteDetector.isListening());
    }
}
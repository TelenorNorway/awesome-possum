package com.telenor.possumlib.detectortests;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.Process;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.detectors.SatelliteDetector;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.Mockito.when;

@RunWith(PossumTestRunner.class)
public class SatelliteDetectorTest {
    private SatelliteDetector satelliteDetector;
    private LocationManager mockedLocationManager;
    private Context mockedContext;
    private GpsStatus mockedGpsStatus;
    private EventBus eventBus;
    private boolean didReceiveEvent;

    @Before
    public void setUp() throws Exception {
        mockedContext = Mockito.mock(Context.class);
        eventBus = new EventBus();
        mockedLocationManager = Mockito.mock(LocationManager.class);
        List<String> allProviders = new ArrayList<>();
        allProviders.add(LocationManager.GPS_PROVIDER);
        allProviders.add(LocationManager.NETWORK_PROVIDER);
        when(mockedLocationManager.getAllProviders()).thenReturn(allProviders);
        mockedGpsStatus = Mockito.mock(GpsStatus.class);
        didReceiveEvent = false;
        setLocationDetectorWith(true, PackageManager.PERMISSION_GRANTED);
//        satelliteDetector = new SatelliteDetector(mockedContext, "fakeUnique", "fakeId") {
//            @Override
//            public void objectChanged(Object source) {
//                super.objectChanged(source);
//                didReceiveEvent = true;
//            }
//            @Override
//            public boolean isAvailable() {
//                return true; // TODO: This shit won't stand, but it isn't used atm so fix it later
//            }
//        };
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

    @SuppressWarnings("unchecked")
    @After
    public void tearDown() throws Exception {
        satelliteDetector = null;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInit() throws Exception {
//        Assert.assertNotNull(satelliteDetector);
//        Method subscribers = EventBus.class.getDeclaredMethod("subscribers", String.class);
//        subscribers.setAccessible(true);
//        satelliteDetector.startListening();
//        Set<EventSubscriber> subscriberSet = (Set<EventSubscriber>) subscribers.invoke(EventBus.getInstance(), SatelliteDetector.SATELLITE_EVENT);
//        Assert.assertEquals(1, subscriberSet.size());
//        Assert.assertEquals(satelliteDetector, subscriberSet.iterator().next());

    }

    @Test
    public void testDefaults() throws Exception {
        Assert.assertTrue(satelliteDetector.isValidSet());
        Assert.assertEquals(DetectorType.GpsStatus, satelliteDetector.detectorType());
    }

    @Test
    public void testEnabledWithValidLocationDetector() throws Exception {
        Assert.assertTrue(satelliteDetector.isEnabled());
    }

    @Test
    public void testIsDisabledWhenNoGpsInLocationManager() throws Exception {
        setLocationDetectorWith(false, PackageManager.PERMISSION_GRANTED);
        satelliteDetector = new SatelliteDetector(mockedContext, "fakeUnique", "fakeId", eventBus);
        Assert.assertFalse(satelliteDetector.isEnabled());
        Assert.assertFalse(satelliteDetector.isAvailable()); // By default, available = enabled on startup
    }

    @Test
    public void testIsAvailableWhenValidLocationDetector() throws Exception {
        setLocationDetectorWith(true, PackageManager.PERMISSION_DENIED);
        satelliteDetector = new SatelliteDetector(mockedContext, "fakeUnique", "fakeId", eventBus);
        Assert.assertTrue(satelliteDetector.isEnabled());
        Assert.assertFalse(satelliteDetector.isAvailable());
    }

    @Test
    public void testConfirmStoreWithInterval() throws Exception {
        Method storeInterval = SatelliteDetector.class.getDeclaredMethod("storeWithInterval");
        storeInterval.setAccessible(true);
        Assert.assertTrue((boolean) storeInterval.invoke(satelliteDetector));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testTerminateRemovesEvent() throws Exception {
//        Method subscribers = EventBus.class.getDeclaredMethod("subscribers", String.class);
//        subscribers.setAccessible(true);
//        satelliteDetector.startListening();
//        Set<EventSubscriber> subscriberSet = (Set<EventSubscriber>) subscribers.invoke(EventBus.getInstance(), SATELLITE_EVENT);
//        Assert.assertEquals(1, subscriberSet.size());
//        Assert.assertEquals(satelliteDetector, subscriberSet.iterator().next());
//
//        satelliteDetector.terminate();
//        Set<EventSubscriber> terminateSet = (Set<EventSubscriber>) subscribers.invoke(EventBus.getInstance(), SATELLITE_EVENT);
//        Assert.assertEquals(0, terminateSet.size());
    }
}
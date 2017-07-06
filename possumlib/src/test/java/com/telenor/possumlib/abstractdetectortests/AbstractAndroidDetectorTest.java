package com.telenor.possumlib.abstractdetectortests;

import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.PowerManager;

import com.telenor.possumlib.FileManipulator;
import com.telenor.possumlib.JodaInit;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.abstractdetectors.AbstractAndroidDetector;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.models.PossumBus;
import com.telenor.possumlib.utils.FileUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowSensorManager;

import java.io.File;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(PossumTestRunner.class)
public class AbstractAndroidDetectorTest { // extends GeneralSensorTest
    private AbstractAndroidDetector abstractAndroidDetector;
    @Mock
    private File mockedFile;
    @Mock
    private Context mockedContext;
    @Mock
    private Sensor mockedSensor;
    @Mock
    private AlarmManager mockedAlarmManager;
    @Mock
    private PowerManager mockedPowerManager;
    private PossumBus eventBus;

    private ShadowSensorManager shadow;
    private SensorManager sensorManager;
    private boolean eventFired;
    private long storageInUpload;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        JodaInit.initializeJodaTime();
        storageInUpload = 0;
        eventBus = new PossumBus();
        when(mockedContext.getString(anyInt())).thenReturn("Accelerometer");
        when(mockedContext.checkPermission(anyString(), anyInt(), anyInt())).thenReturn(PackageManager.PERMISSION_GRANTED);
        when(mockedContext.getSystemService(Context.SENSOR_SERVICE)).thenReturn(RuntimeEnvironment.application.getSystemService(Context.SENSOR_SERVICE));
        when(mockedContext.getSystemService(Context.ALARM_SERVICE)).thenReturn(RuntimeEnvironment.application.getSystemService(Context.ALARM_SERVICE));
        when(mockedContext.getSystemService(Context.POWER_SERVICE)).thenReturn(RuntimeEnvironment.application.getSystemService(Context.POWER_SERVICE));
        eventFired = false;
        sensorManager = (SensorManager) RuntimeEnvironment.application.getSystemService(Context.SENSOR_SERVICE);
        shadow = Shadows.shadowOf(sensorManager);
        shadow.addSensor(Sensor.TYPE_ACCELEROMETER, mockedSensor);
        abstractAndroidDetector = getDetector(mockedContext, eventBus);
    }

    @After
    public void tearDown() throws Exception {
        shadow = null;
        sensorManager = null;
        abstractAndroidDetector = null;
        FileUtil.clearDirectory(RuntimeEnvironment.application);
    }

    private AbstractAndroidDetector getDetector(Context context, PossumBus eventBus) {
        return new AbstractAndroidDetector(context, Sensor.TYPE_ACCELEROMETER, "fakeUnique", eventBus, false) {
            @Override
            protected int detectorRequestCode() {
                return 0;
            }

            @Override
            public int detectorType() {
                return DetectorType.Accelerometer;
            }

            @Override
            public String detectorName() {
                return "Accelerometer";
            }

            @Override
            public String requiredPermission() {
                return null;
            }

            @Override
            protected long uploadFilesSize() {
                return storageInUpload;
            }

            @Override
            public File storedData() {
                return mockedFile;
            }
        };
    }

    @Test
    public void testInitWithSensorFound() throws Exception {
        Assert.assertTrue(abstractAndroidDetector.isEnabled());
        Assert.assertTrue(abstractAndroidDetector.startListening());
    }

    @Test
    public void testInitWithMissingSensor() throws Exception {
        shadow.addSensor(Sensor.TYPE_ACCELEROMETER, null);
        abstractAndroidDetector = getDetector(mockedContext, eventBus);
        Assert.assertFalse(abstractAndroidDetector.isEnabled());
        Assert.assertFalse(abstractAndroidDetector.startListening());
    }

    @Test
    public void testDefaults() throws Exception {
        Assert.assertTrue(abstractAndroidDetector.isAvailable());
    }

    @Test
    public void testMissingSensorManager() throws Exception {
        when(mockedContext.getSystemService(Context.SENSOR_SERVICE)).thenReturn(null);
        abstractAndroidDetector = getMockedSensor();
        Assert.assertFalse(abstractAndroidDetector.isEnabled());
    }

    private AbstractAndroidDetector getMockedSensor() {
        return new AbstractAndroidDetector(mockedContext, Sensor.TYPE_ACCELEROMETER, "fakeUnique", eventBus, false) {
            @Override
            protected int detectorRequestCode() {
                return 0;
            }

            @Override
            public int detectorType() {
                return DetectorType.Accelerometer;
            }

            @Override
            public String detectorName() {
                return "Accelerometer";
            }

            @Override
            public String requiredPermission() {
                return null;
            }

            @Override
            public File storedData() {
                return mockedFile;
            }
        };
    }

    @Test
    public void testFileSize() throws Exception {
        when(mockedFile.length()).thenReturn(160L);
        Assert.assertEquals(160L, abstractAndroidDetector.fileSize());
    }

    @Test
    public void testPowerUsage() throws Exception {
        when(mockedSensor.getPower()).thenReturn(160f);
        Assert.assertEquals(160f, abstractAndroidDetector.powerUsage(), 0);
    }

    @Test
    public void testStartStopListening() throws Exception {
        File dataDir = FileManipulator.getDataDir(RuntimeEnvironment.application);
        final File storedData = new File(dataDir.getAbsolutePath() + "/Accelerometer");
        if (storedData.exists()) {
            Assert.assertTrue(storedData.delete());
            Assert.assertTrue(storedData.createNewFile());
        }

        Assert.assertFalse(abstractAndroidDetector.isListening());
        Assert.assertTrue(abstractAndroidDetector.startListening());
        Assert.assertTrue(abstractAndroidDetector.isListening());
        abstractAndroidDetector.stopListening();
        Assert.assertFalse(abstractAndroidDetector.isListening());
    }
}
package com.telenor.possumlib.abstractdetectortests;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.PowerManager;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.FileManipulator;
import com.telenor.possumlib.JodaInit;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.abstractdetectors.AbstractAndroidDetector;
import com.telenor.possumlib.constants.DetectorType;
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
import org.robolectric.annotation.Config;
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
    private EventBus eventBus;

    private ShadowSensorManager shadow;
    private SensorManager sensorManager;
    private boolean eventFired;
    private long storageInUpload;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        JodaInit.initializeJodaTime();
        storageInUpload = 0;
        eventBus = new EventBus();
        when(mockedContext.getString(anyInt())).thenReturn("Accelerometer");
        when(mockedContext.checkPermission(anyString(), anyInt(), anyInt())).thenReturn(PackageManager.PERMISSION_GRANTED);
        when(mockedContext.getSystemService(Context.SENSOR_SERVICE)).thenReturn(RuntimeEnvironment.application.getSystemService(Context.SENSOR_SERVICE));
        when(mockedContext.getSystemService(Context.ALARM_SERVICE)).thenReturn(RuntimeEnvironment.application.getSystemService(Context.ALARM_SERVICE));
        when(mockedContext.getSystemService(Context.POWER_SERVICE)).thenReturn(RuntimeEnvironment.application.getSystemService(Context.POWER_SERVICE));
        eventFired = false;
        sensorManager = (SensorManager) RuntimeEnvironment.application.getSystemService(Context.SENSOR_SERVICE);
        shadow = Shadows.shadowOf(sensorManager);
        shadow.addSensor(Sensor.TYPE_ACCELEROMETER, mockedSensor);
        abstractAndroidDetector = new AbstractAndroidDetector(mockedContext, Sensor.TYPE_ACCELEROMETER, "fakeUnique", "fakeId", eventBus) {
            @Override
            public long guaranteedListenInterval() {
                return 0;
            }

            @Override
            public long restartInterval() {
                return 0;
            }

            @Override
            protected int detectorRequestCode() {
                return 0;
            }

            @Override
            public void detectorWakelockActivated() {

            }

            @Override
            public int detectorType() {
                return DetectorType.Accelerometer;
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

    @After
    public void tearDown() throws Exception {
        shadow = null;
        sensorManager = null;
        abstractAndroidDetector = null;
        FileUtil.clearDirectory(RuntimeEnvironment.application);
    }

    @Test
    public void testInitWithSensorFound() throws Exception {
        Assert.assertTrue(abstractAndroidDetector.isEnabled());
        Assert.assertTrue(abstractAndroidDetector.startListening());
    }

    @Test
    public void testInitWithMissingSensor() throws Exception {
        shadow.addSensor(Sensor.TYPE_ACCELEROMETER, null);
        abstractAndroidDetector = new AbstractAndroidDetector(mockedContext, Sensor.TYPE_ACCELEROMETER, "fakeUnique", "fakeId", eventBus) {
            @Override
            public long guaranteedListenInterval() {
                return 0;
            }

            @Override
            public long restartInterval() {
                return 0;
            }

            @Override
            protected int detectorRequestCode() {
                return 0;
            }

            @Override
            public void detectorWakelockActivated() {

            }

            @Override
            public int detectorType() {
                return DetectorType.Accelerometer;
            }

            @Override
            public File storedData() {
                return mockedFile;
            }
        };
        Assert.assertFalse(abstractAndroidDetector.isEnabled());
        Assert.assertFalse(abstractAndroidDetector.startListening());
    }

    @Test
    public void testDefaults() throws Exception {
        Assert.assertTrue(abstractAndroidDetector.isAvailable());
        Assert.assertTrue(abstractAndroidDetector.isValidSet());
    }

    @Test
    public void testMissingSensorManager() throws Exception {
        when(mockedContext.getSystemService(Context.SENSOR_SERVICE)).thenReturn(null);
        abstractAndroidDetector = getMockedSensor();
        Assert.assertFalse(abstractAndroidDetector.isEnabled());
    }

    private AbstractAndroidDetector getMockedSensor() {
        return new AbstractAndroidDetector(mockedContext, Sensor.TYPE_ACCELEROMETER, "fakeUnique", "fakeId", eventBus) {
            @Override
            public long guaranteedListenInterval() {
                return 0;
            }

            @Override
            public long restartInterval() {
                return 0;
            }

            @Override
            protected int detectorRequestCode() {
                return 0;
            }

            @Override
            public void detectorWakelockActivated() {

            }

            @Override
            public int detectorType() {
                return DetectorType.Accelerometer;
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

    @SuppressLint("NewApi")
    @Config(sdk = 19)
    @Test
    public void testIsWakeupSensorBelowLollipop() throws Exception {
        Assert.assertFalse(abstractAndroidDetector.isWakeUpDetector());
    }

    @Config(sdk = 19)
    @Test
    public void testWakeupForKitKat() throws Exception {

    }

    @Config(sdk = 19)
    @Test
    public void testGotoSleepForKitKat() throws Exception {

    }

    @SuppressLint("NewApi")
    @Config(sdk = 21)
    @Test
    public void testIsWakeupSensorFromLollipop() throws Exception {
        when(mockedSensor.isWakeUpSensor()).thenReturn(true);
        Assert.assertTrue(abstractAndroidDetector.isWakeUpDetector());
        when(mockedSensor.isWakeUpSensor()).thenReturn(false);
        Assert.assertFalse(abstractAndroidDetector.isWakeUpDetector());
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

    @Test
    public void testWakeUpNonSdkDependent() throws Exception {
//        EventSubscriber subscriber = eventSubscriber();
//        Assert.assertEquals(0, WakeUtil.holders());
//        Assert.assertFalse(eventFired);
//        EventBus.getInstance().subscribe(MetaDataDetector.META_EVENT, subscriber);
//        abstractAndroidDetector = getMockedSensor();
//
//        Method wakeUpMethod = AbstractAndroidDetector.class.getDeclaredMethod("wakeUp");
//        wakeUpMethod.setAccessible(true);
//        wakeUpMethod.invoke(abstractAndroidDetector);
//
//        Assert.assertTrue(eventFired);
//
//        Field isGuaranteedListening = AbstractAndroidDetector.class.getDeclaredField("isInGuaranteedListenMode");
//        isGuaranteedListening.setAccessible(true);
//        Assert.assertTrue(isGuaranteedListening.getBoolean(abstractAndroidDetector));
//
//        EventBus.getInstance().unSubscribeAll(subscriber);
    }

    @Test
    public void testGotoSleepNonSdkDependent() throws Exception {
//        EventSubscriber subscriber = eventSubscriber();
//        Assert.assertEquals(0, WakeUtil.holders());
//        Assert.assertFalse(eventFired);
//        EventBus.getInstance().subscribe(MetaDataDetector.META_EVENT, subscriber);
//
//        abstractAndroidDetector = getMockedSensor();
//
//        Method gotoSleepMethod = AbstractAndroidDetector.class.getDeclaredMethod("gotoSleep");
//        gotoSleepMethod.setAccessible(true);
//        gotoSleepMethod.invoke(abstractAndroidDetector);
//        Assert.assertTrue(eventFired);
//        Assert.assertTrue(WakeUtil.holders() <= 0);
//
//        Field isGuaranteedListening = AbstractAndroidDetector.class.getDeclaredField("isInGuaranteedListenMode");
//        isGuaranteedListening.setAccessible(true);
//        Assert.assertFalse(isGuaranteedListening.getBoolean(abstractAndroidDetector));
//
//        EventBus.getInstance().unSubscribeAll(subscriber);
    }

    @Config(sdk = 18)
    @Test
    public void testWakeupForBeforeKitkat() throws Exception {

    }

    @Config(sdk = 18)
    @Test
    public void testGotoSleepForBeforeKitkat() throws Exception {

    }

    @Config(sdk = 23)
    @Test
    public void testWakeupForNougat() throws Exception {

    }

    @Config(sdk = 23)
    @Test
    public void testGotoSleepForNougat() throws Exception {

    }

//    private EventSubscriber eventSubscriber() {
//        return new EventSubscriber() {
//            @Override
//            public void objectChanged(Object source) {
//                eventFired = true;
//            }
//
//            @Override
//            public void listChanged() {
//
//            }
//        };
//    }
}
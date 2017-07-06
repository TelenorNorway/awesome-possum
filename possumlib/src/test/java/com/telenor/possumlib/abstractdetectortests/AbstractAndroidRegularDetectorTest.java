package com.telenor.possumlib.abstractdetectortests;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;

import com.telenor.possumlib.FileManipulator;
import com.telenor.possumlib.JodaInit;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.SensorEvents;
import com.telenor.possumlib.abstractdetectors.AbstractAndroidRegularDetector;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.changeevents.PossumEvent;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.interfaces.IPossumEventListener;
import com.telenor.possumlib.models.PossumBus;

import org.joda.time.DateTime;
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
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowSensorManager;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PossumTestRunner.class)
public class AbstractAndroidRegularDetectorTest {
    @Mock
    private Context mockedContext;
    @Mock
    private SensorManager mockedSensorManager;
    @Mock
    private Sensor mockedSensor;
    @Mock
    private AlarmManager mockedAlarmManager;
    @Mock
    private PowerManager mockedPowerManager;

    private ShadowSensorManager shadow;
    private SensorManager sensorManager;

    private int requestCode = 12345;
    private int counter;
    private File fakeFile;
    private IPossumEventListener listener;
    private PossumBus eventBus;
    private AbstractAndroidRegularDetector androidRegularSensor;
    private Object sourceReceived;

    @Config(sdk = 21)
    @TargetApi(value = 19)
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        JodaInit.initializeJodaTime();
        counter = 0;
        listener = new IPossumEventListener() {
            @Override
            public void eventReceived(PossumEvent event) {
                counter++;
                Assert.assertTrue(event.message().contains("Accelerometer FIFO SIZE 5000 5000"));
            }
        };
        eventBus = new PossumBus();
        sensorManager = (SensorManager) RuntimeEnvironment.application.getSystemService(Context.SENSOR_SERVICE);
        shadow = Shadows.shadowOf(sensorManager);
        shadow.addSensor(Sensor.TYPE_ACCELEROMETER, mockedSensor);
        when(mockedSensor.getFifoMaxEventCount()).thenReturn(5000);
        when(mockedSensor.getFifoReservedEventCount()).thenReturn(5000);
        fakeFile = FileManipulator.getFileWithName(RuntimeEnvironment.application, "fakeFile");
        androidRegularSensor = getDetector(RuntimeEnvironment.application, eventBus);
    }

    @After
    public void tearDown() throws Exception {
        androidRegularSensor = null;
        sensorManager = null;
        shadow = null;
        sourceReceived = null;
        Assert.assertTrue(fakeFile.delete());
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertNotNull(androidRegularSensor);
    }

    @Test
    public void testInitFiresEventAboutFifoQueue() throws Exception {
        Assert.assertNull(sourceReceived);
        eventBus.register(listener);
        androidRegularSensor = getDetector(RuntimeEnvironment.application, eventBus);
        Assert.assertEquals(1, counter);
    }

    @Test
    public void testInvalidTimestamp() throws Exception {
        float x = 10;
        float y = 20;
        float z = 30;
        long timestamp = System.currentTimeMillis();
        SensorEvent sensorEvent = SensorEvents.createSensorEvent(mockedSensor, timestamp, 0, x, y, z);
        Field nanoField = AbstractAndroidRegularDetector.class.getDeclaredField("MIN_INTERVAL_NANO");
        nanoField.setAccessible(true);
        long nanoTimeBetweenMeasurements = nanoField.getLong(androidRegularSensor);
        long timestampAfter = timestamp + nanoTimeBetweenMeasurements + 1;
        SensorEvent sensorEventAfter = SensorEvents.createSensorEvent(mockedSensor, timestampAfter, 0, x, y, z);
        Method invalidMethod = AbstractAndroidRegularDetector.class.getDeclaredMethod("isInvalid", SensorEvent.class);
        invalidMethod.setAccessible(true);
        Assert.assertFalse((Boolean) invalidMethod.invoke(androidRegularSensor, sensorEvent));
        Assert.assertTrue((Boolean) invalidMethod.invoke(androidRegularSensor, sensorEvent));
        Assert.assertFalse((Boolean) invalidMethod.invoke(androidRegularSensor, sensorEventAfter));
    }

    @Test
    public void testStartStopListening() throws Exception {
        when(mockedContext.getSystemService(Context.SENSOR_SERVICE)).thenReturn(mockedSensorManager);
        when(mockedContext.getString(anyInt())).thenReturn("Accelerometer");
        when(mockedSensorManager.getDefaultSensor(anyInt())).thenReturn(mockedSensor);
        when(mockedContext.getSystemService(Context.ALARM_SERVICE)).thenReturn(mockedAlarmManager);
        when(mockedContext.getSystemService(Context.POWER_SERVICE)).thenReturn(mockedPowerManager);
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.WAKE_LOCK);
        androidRegularSensor = getDetector(mockedContext, eventBus);
        Assert.assertTrue(androidRegularSensor.startListening());
        Field nanoField = AbstractAndroidRegularDetector.class.getDeclaredField("MIN_INTERVAL_MICRO");
        nanoField.setAccessible(true);
        int nanoTime = (int) nanoField.getLong(androidRegularSensor);
        verify(mockedSensorManager, times(1)).registerListener(any(SensorEventListener.class), eq(mockedSensor), eq(nanoTime));

        androidRegularSensor.stopListening();
        verify(mockedSensorManager, times(1)).unregisterListener(any(SensorEventListener.class), eq(mockedSensor));
    }

    private AbstractAndroidRegularDetector getDetector(Context context, PossumBus eventBus) {
        return new AbstractAndroidRegularDetector(context, Sensor.TYPE_ACCELEROMETER, "fakeUnique", eventBus, false) {
            @Override
            protected int detectorRequestCode() {
                return requestCode;
            }

            @Override
            public String requiredPermission() {
                return null;
            }

            @Override
            public int detectorType() {
                return DetectorType.Accelerometer;
            }

            @Override
            public String detectorName() {
                return "Accelerometer";
            }
        };
    }

    @Test
    public void testTimestamp() throws Exception {
        long timestamp = DateTime.now().getMillis();
        SensorEvent event = SensorEvents.createSensorEvent(mockedSensor, timestamp, 0, 10, 10, 10);
        Method timestampMethod = AbstractAndroidRegularDetector.class.getDeclaredMethod("timestamp", SensorEvent.class);
        timestampMethod.setAccessible(true);
        long timestampOut = (long) timestampMethod.invoke(androidRegularSensor, event);
        Assert.assertTrue(timestamp < timestampOut);
    }

    @Test
    @TargetApi(value = 19)
    public void testSensorChanged() throws Exception {
        when(mockedSensor.getFifoMaxEventCount()).thenReturn(3);
        long timestamp = System.currentTimeMillis();
        SensorEvent event = SensorEvents.createSensorEvent(mockedSensor, timestamp, 0, 10, 10, 10);
        androidRegularSensor.onSensorChanged(event);
        Field storedField = AbstractDetector.class.getDeclaredField("storedValues");
        storedField.setAccessible(true);
        Assert.assertEquals(0, storedField.getInt(androidRegularSensor));
        Assert.assertTrue(androidRegularSensor.startListening());
        Assert.assertTrue(androidRegularSensor.isListening());
//        androidRegularSensor.sessionValues().add("addedFakeEvent");
        androidRegularSensor.onSensorChanged(event);
        Assert.assertEquals(1, storedField.getInt(androidRegularSensor));
//        androidRegularSensor.sessionValues().add("addedFakeEvent");
        androidRegularSensor.onSensorChanged(event);
        Assert.assertEquals(2, storedField.getInt(androidRegularSensor));
//        androidRegularSensor.sessionValues().add("addedFakeEvent");
        androidRegularSensor.onSensorChanged(event);
        Assert.assertEquals(0, storedField.getInt(androidRegularSensor));
    }

    @Test
    public void testAccuracyChanged() throws Exception {
        ShadowLog.setupLogging();
        androidRegularSensor.onAccuracyChanged(mockedSensor, 10);
        Assert.assertEquals(0, ShadowLog.getLogs().size());
    }
}
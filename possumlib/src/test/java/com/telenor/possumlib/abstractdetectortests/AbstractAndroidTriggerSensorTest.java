package com.telenor.possumlib.abstractdetectortests;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.abstractdetectors.AbstractAndroidTriggerDetector;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.detectortests.GeneralSensorTest;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.internal.Shadow;
import org.robolectric.shadows.ShadowSystemClock;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(PossumTestRunner.class)
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AbstractAndroidTriggerSensorTest extends GeneralSensorTest {
    private long restartInterval = 4000;
    private long listenInterval = 4000;
    private int requestCode = 7317;
    private boolean onTriggerFired;
    private boolean wakeLockActivated;
    private EventBus eventBus;
    private AbstractAndroidTriggerDetector androidTriggerSensor;

    @Before
    public void setUp() throws Exception {
        super.setUp(Sensor.TYPE_SIGNIFICANT_MOTION);
        onTriggerFired = false;
        eventBus = new EventBus();
        wakeLockActivated = false;
        androidTriggerSensor = getDetector(mockedContext, eventBus);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        androidTriggerSensor = null;
    }

    private AbstractAndroidTriggerDetector getDetector(Context context, EventBus eventBus) {
        return new AbstractAndroidTriggerDetector(context, Sensor.TYPE_SIGNIFICANT_MOTION, "fakeUnique", "fakeId", eventBus) {
            @Override
            public long guaranteedListenInterval() {
                return listenInterval;
            }

            @Override
            public long restartInterval() {
                return restartInterval;
            }

            @Override
            protected int detectorRequestCode() {
                return requestCode;
            }

            @Override
            public void detectorWakelockActivated() {
                wakeLockActivated = true;
            }
            @Override
            public int detectorType() {
                return DetectorType.Significant;
            }

            @Override
            public String detectorName() {
                return "FakeName";
            }

            @Override
            public void onTrigger(TriggerEvent event) {
                onTriggerFired = true;
            }
        };
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertNotNull(androidTriggerSensor);
        Field alarmManagerField = AbstractAndroidTriggerDetector.class.getDeclaredField("alarmManager");
        alarmManagerField.setAccessible(true);
        Assert.assertEquals(mockedAlarmManager, alarmManagerField.get(androidTriggerSensor));
        Assert.assertFalse(wakeLockActivated);
        Assert.assertFalse(onTriggerFired);
    }

    @Test
    public void testTimestamp() throws Exception {
        Method timestampMethod = AbstractAndroidTriggerDetector.class.getDeclaredMethod("timestamp", TriggerEvent.class);
        timestampMethod.setAccessible(true);
        TriggerEvent triggerEvent = mock(TriggerEvent.class);
        triggerEvent.sensor = mock(Sensor.class);

        long nanoTimeSinceStart = ShadowSystemClock.nanoTime();
        triggerEvent.timestamp = nanoTimeSinceStart;
        long stamp = (long)timestampMethod.invoke(androidTriggerSensor, triggerEvent);
        Assert.assertTrue(stamp > nanoTimeSinceStart);
    }

    @Test
    public void testStartListening() throws Exception {
        Assert.assertTrue(androidTriggerSensor.startListening());
        Assert.assertTrue(androidTriggerSensor.isListening());
        verify(mockedSensorManager).requestTriggerSensor(any(TriggerEventListener.class), any(Sensor.class));
    }

    @Test
    public void testStopListening() throws Exception {
        Assert.assertTrue(androidTriggerSensor.startListening());
        Assert.assertTrue(androidTriggerSensor.isListening());
        androidTriggerSensor.stopListening();
        Assert.assertFalse(androidTriggerSensor.isListening());
        verify(mockedSensorManager).cancelTriggerSensor(any(TriggerEventListener.class), any(Sensor.class));
    }

    @Test
    public void testTriggerFiring() throws Exception {
        Field listenerField = AbstractAndroidTriggerDetector.class.getDeclaredField("triggerListener");
        listenerField.setAccessible(true);
        TriggerEventListener listener = (TriggerEventListener)listenerField.get(androidTriggerSensor);
        TriggerEvent triggerEvent = Shadow.newInstanceOf(TriggerEvent.class);
        triggerEvent.timestamp = System.currentTimeMillis();
        triggerEvent.sensor = mockedSensor;
        listener.onTrigger(triggerEvent);
        Assert.assertTrue(onTriggerFired);
        verify(mockedAlarmManager).set(eq(AlarmManager.RTC_WAKEUP), anyLong(), any(PendingIntent.class));
    }
}
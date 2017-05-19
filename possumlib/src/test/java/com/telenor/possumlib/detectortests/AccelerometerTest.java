package com.telenor.possumlib.detectortests;

import android.content.Context;
import android.hardware.Sensor;
import android.support.annotation.NonNull;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.constants.ReqCodes;
import com.telenor.possumlib.detectors.Accelerometer;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.lang.reflect.Method;

import static com.telenor.possumlib.SensorEvents.createSensorEvent;

@RunWith(PossumTestRunner.class)
public class AccelerometerTest extends GeneralSensorTest {
    private Accelerometer accelerometer;
    private boolean event1;
    private boolean event2;
    private EventBus eventBus;
    @Before
    public void setUp() throws Exception {
        super.setUp(Sensor.TYPE_ACCELEROMETER);
        eventBus = new EventBus();
        event1 = false;
        event2 = false;
        accelerometer = new Accelerometer(mockedContext, "fakeUnique", "fakeId", eventBus){
            @Override
            public Context context() {
                return mockedContext;
            }
            @Override
            public boolean isEnabled() {
                return sensorIsEnabled;
            }
            // To enable timestamps in similar times from not being removed/ignored
            @Override
            protected void storeData(@NonNull File file) {
                storingData++;
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        if (accelerometer != null) {
            accelerometer.terminate();
            accelerometer = null;
        }
        storingData = 0;
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertNotNull(accelerometer);
    }

    @Test
    public void testDefaults() throws Exception {
        Assert.assertEquals(1680000, accelerometer.guaranteedListenInterval());
        Assert.assertEquals(2940000, accelerometer.restartInterval());
        Method reqCodeMethod = Accelerometer.class.getDeclaredMethod("detectorRequestCode");
        reqCodeMethod.setAccessible(true);
        Assert.assertEquals(ReqCodes.ACCELEROMETER, reqCodeMethod.invoke(accelerometer));
    }

    @Test
    public void testWhatHappensDuringWakeLockActivated() throws Exception {
//        EventSubscriber subscriberLocation = new EventSubscriber() {
//            @Override
//            public void objectChanged(Object source) {
//                event1 = true;
//            }
//
//            @Override
//            public void listChanged() {
//
//            }
//        };
//        EventSubscriber subscriberWifi = new EventSubscriber() {
//            @Override
//            public void objectChanged(Object source) {
//                event2 = true;
//            }
//
//            @Override
//            public void listChanged() {
//
//            }
//        };
//        EventBus.getInstance().subscribe(LocationDetector.LOCATION_EVENT, subscriberLocation);
//        EventBus.getInstance().subscribe(NetworkDetector.WIFI_EVENT, subscriberWifi);
        accelerometer.detectorWakelockActivated();
        Assert.assertTrue(event1);
        Assert.assertTrue(event2);

//        EventBus.getInstance().unSubscribeAll(subscriberLocation);
//        EventBus.getInstance().unSubscribeAll(subscriberWifi);
    }

    @Test
    public void testSensorAddsLines() throws Exception {
        long timestamp = DateTime.now().getMillis();
        Assert.assertEquals(0, accelerometer.sessionValues().size());
        accelerometer.onSensorChanged(createSensorEvent(mockedSensor, timestamp, 0, 0.1f, 0.1f, 0.1f));
        Assert.assertEquals(1, accelerometer.sessionValues().size());
        accelerometer.onSensorChanged(createSensorEvent(mockedSensor, timestamp, 0, 0.1f, 0.1f, 0.1f));
        Assert.assertEquals(1, accelerometer.sessionValues().size());

        accelerometer.onSensorChanged(createSensorEvent(mockedSensor, (timestamp+50000001L), 0, 0.1f, 0.1f, 0.1f));
        Assert.assertEquals(2, accelerometer.sessionValues().size());
    }
}
package com.telenor.possumlib.detectortests;

import android.content.Context;
import android.hardware.Sensor;
import android.support.annotation.NonNull;

import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.detectors.Magnetometer;
import com.telenor.possumlib.models.PossumBus;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

@RunWith(PossumTestRunner.class)
public class MagnetometerTest extends GeneralSensorTest {
    private Magnetometer magnetometer;
    private PossumBus eventBus;
    @Before
    public void setUp() throws Exception {
        super.setUp(Sensor.TYPE_MAGNETIC_FIELD);
        eventBus = new PossumBus();
        magnetometer = new Magnetometer(mockedContext, "fakeUnique", eventBus, false) {
            @Override
            public Context context() {
                return mockedContext;
            }
            @Override
            public boolean isEnabled() {
                return sensorIsEnabled;
            }
            @Override
            protected void storeData(@NonNull File file) {
                storingData++;
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        if (magnetometer != null) {
            magnetometer.terminate();
            magnetometer = null;
        }
        storingData = 0;
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertNotNull(magnetometer);
    }
    
    // Invalid due to zip
//    @Test
//    public void testSensorAddsLines() throws Exception {
//        long timestamp = DateTime.now().getMillis();
//        Assert.assertEquals(0, magnetometer.sessionValues().size());
//        magnetometer.onSensorChanged(createSensorEvent(mockedSensor, timestamp, 0, 0.1f, 0.1f, 0.1f));
//        Assert.assertEquals(1, magnetometer.sessionValues().size());
//        magnetometer.onSensorChanged(createSensorEvent(mockedSensor, timestamp, 0, 0.1f, 0.1f, 0.1f));
//        Assert.assertEquals(1, magnetometer.sessionValues().size());
//
//        magnetometer.onSensorChanged(createSensorEvent(mockedSensor, (timestamp+50000001L), 0, 0.1f, 0.1f, 0.1f));
//        Assert.assertEquals(2, magnetometer.sessionValues().size());
//    }
}
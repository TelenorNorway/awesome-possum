package com.telenor.possumlib.constants;

import android.hardware.Sensor;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class DetectorTypeTest {
    @Test
    public void testConstantsValues() throws Exception {
        Assert.assertEquals(Sensor.TYPE_ACCELEROMETER, DetectorType.Accelerometer);
        Assert.assertEquals(Sensor.TYPE_GYROSCOPE, DetectorType.Gyroscope);
        Assert.assertEquals(Sensor.TYPE_MAGNETIC_FIELD, DetectorType.Magnetometer);
        Assert.assertEquals(100, DetectorType.Wifi);
        Assert.assertEquals(101, DetectorType.Bluetooth);
        Assert.assertEquals(104, DetectorType.Position);
        Assert.assertEquals(105, DetectorType.GpsStatus);
        Assert.assertEquals(106, DetectorType.Image);
        Assert.assertEquals(107, DetectorType.Keyboard);
        Assert.assertEquals(108, DetectorType.MetaData);
        Assert.assertEquals(109, DetectorType.Gesture);
        Assert.assertEquals(110, DetectorType.Hardware);
        Assert.assertEquals(111, DetectorType.Audio);
    }
}
package com.telenor.possumlib.utiltests;

import android.content.Context;

import com.telenor.possumlib.PossumTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

@RunWith(PossumTestRunner.class)
public class SensorUtilTest {

    @Test
    public void testGetNameOfDetectorByType() throws Exception {
        Context c = RuntimeEnvironment.application;
//        Assert.assertEquals(c.getString(R.string.sensor_accelerometer), SensorUtil.detectorTypeString(c, DetectorType.Accelerometer));
//        Assert.assertEquals(c.getString(R.string.sensor_gyroscope), SensorUtil.detectorTypeString(c, DetectorType.Gyroscope));
//        Assert.assertEquals(c.getString(R.string.sensor_magnetometer), SensorUtil.detectorTypeString(c, DetectorType.Magnetometer));
//        Assert.assertEquals(c.getString(R.string.sensor_bluetooth), SensorUtil.detectorTypeString(c, DetectorType.Bluetooth));
//        Assert.assertEquals(c.getString(R.string.sensor_network), SensorUtil.detectorTypeString(c, DetectorType.Wifi));
//        Assert.assertEquals(c.getString(R.string.sensor_position), SensorUtil.detectorTypeString(c, DetectorType.Position));
//        Assert.assertEquals(c.getString(R.string.sensor_satellites), SensorUtil.detectorTypeString(c, DetectorType.GpsStatus));
//        Assert.assertEquals(c.getString(R.string.sensor_nfc), SensorUtil.detectorTypeString(c, DetectorType.Nfc));
//        Assert.assertEquals(c.getString(R.string.sensor_image), SensorUtil.detectorTypeString(c, DetectorType.Image));
//        Assert.assertEquals(c.getString(R.string.sensor_keyboard), SensorUtil.detectorTypeString(c, DetectorType.Keyboard));
//        Assert.assertEquals(c.getString(R.string.sensor_metadata), SensorUtil.detectorTypeString(c, DetectorType.MetaData));
//        Assert.assertEquals(c.getString(R.string.sensor_gesture), SensorUtil.detectorTypeString(c, DetectorType.Gesture));
//        try {
//            Assert.assertEquals(c.getString(R.string.sensor_accelerometer), SensorUtil.detectorTypeString(c, -1));
//            Assert.fail("Should not have found invalid sensor");
//        } catch (Exception e) {
//            Assert.assertEquals("Unknown detectorType:-1", e.getMessage());
//        }
    }
}
package com.telenor.possumlib.abstractdetectortests;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.PowerManager;

import com.telenor.possumlib.JodaInit;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.abstractdetectors.AbstractZippingAndroidDetector;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.models.PossumBus;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.zip.ZipOutputStream;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(PossumTestRunner.class)
public class AbstractZippingAndroidDetectorTest {
    private AbstractZippingAndroidDetector androidSensor;
    private boolean wakeLockActivated;
    private long authenticationListenInterval = 1000;
    private long sizeOfUpload;
    private File fakeFile;
    @Mock
    private Context mockedContext;
    @Mock
    private SensorManager mockedSensorManager;
    @Mock
    private Sensor mockedSensor;
    @Mock
    private PowerManager mockedPowerManager;
    @Mock
    private AlarmManager mockedAlarmManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        wakeLockActivated = false;
        sizeOfUpload = 100;
        PossumBus eventBus = new PossumBus();
        fakeFile = new File(RuntimeEnvironment.application.getFilesDir().getAbsolutePath() + "/testFile");
        if (fakeFile.exists()) {
            Assert.assertTrue(fakeFile.delete());
        }
        Assert.assertTrue(fakeFile.createNewFile());
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.WAKE_LOCK);
        JodaInit.initializeJodaTime();

        when(mockedContext.getString(anyInt())).thenReturn("Accelerometer");
        when(mockedContext.checkPermission(anyString(), anyInt(), anyInt())).thenReturn(PackageManager.PERMISSION_GRANTED);
        when(mockedContext.getSystemService(Context.SENSOR_SERVICE)).thenReturn(mockedSensorManager);
        when(mockedContext.getSystemService(Context.POWER_SERVICE)).thenReturn(mockedPowerManager);
        when(mockedContext.getSystemService(Context.ALARM_SERVICE)).thenReturn(mockedAlarmManager);
        when(mockedSensorManager.getDefaultSensor(anyInt())).thenReturn(mockedSensor);
        androidSensor = new AbstractZippingAndroidDetector(mockedContext, Sensor.TYPE_ACCELEROMETER, "fakeUnique", eventBus, false) {
            @Override
            protected int detectorRequestCode() {
                return 13371337;
            }

            @Override
            public File storedData() {
                return fakeFile;
            }

            @Override
            public boolean stageForUpload(File file) {
                return file.length() > 0;
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
            public long uploadFilesSize() {
                return sizeOfUpload;
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        androidSensor = null;
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertNotNull(androidSensor);
    }

    @Test
    public void testDefaults() throws Exception {
        Assert.assertEquals(DetectorType.Accelerometer, androidSensor.detectorType());
        Assert.assertFalse(wakeLockActivated);
    }

    @Test
    public void testStartListening() throws Exception {
        Field outerStreamField = AbstractZippingAndroidDetector.class.getDeclaredField("outerStream");
        outerStreamField.setAccessible(true);
        ZipOutputStream zipStreamBefore = (ZipOutputStream) outerStreamField.get(androidSensor);
        Assert.assertNull(zipStreamBefore);
        Assert.assertTrue(androidSensor.startListening());
        Assert.assertTrue(androidSensor.isListening());
        ZipOutputStream zipStream = (ZipOutputStream) outerStreamField.get(androidSensor);
        Assert.assertNotNull(zipStream);
    }

    @Test
    public void testStopListening() throws Exception {
        Field outerStreamField = AbstractZippingAndroidDetector.class.getDeclaredField("outerStream");
        outerStreamField.setAccessible(true);
        Assert.assertTrue(androidSensor.startListening());
        ZipOutputStream zipStream = (ZipOutputStream) outerStreamField.get(androidSensor);
        Assert.assertNotNull(zipStream);
        Assert.assertTrue(androidSensor.isListening());
        androidSensor.stopListening();
        ZipOutputStream zipStreamAfter = (ZipOutputStream) outerStreamField.get(androidSensor);
        Assert.assertNull(zipStreamAfter);
        Assert.assertFalse(androidSensor.isListening());
    }

    @Test
    public void testCreateZipStream() throws Exception {
        Method zipMethod = AbstractZippingAndroidDetector.class.getDeclaredMethod("createZipStream", OutputStream.class);
        zipMethod.setAccessible(true);
        OutputStream oStream = new FileOutputStream(fakeFile);
        ZipOutputStream zipStream = (ZipOutputStream) zipMethod.invoke(androidSensor, oStream);
        Assert.assertNotNull(zipStream);
        long fileSize = fakeFile.length();
        zipStream.write("This is a test".getBytes());
        zipStream.close();
        Assert.assertTrue(fakeFile.length() > fileSize);
    }

    @Test
    public void testFileSizeAndStoreDataDoesNotChangeWhenNotListening() throws Exception {
        Assert.assertEquals(100, androidSensor.fileSize());
        sizeOfUpload = 200;
        Assert.assertEquals(200, androidSensor.fileSize());
//        for (int i = 0; i < 10; i++) {
//            androidSensor.sessionValues().add("Test" + i);
//        }
        androidSensor.storeData();
        Assert.assertEquals(200, androidSensor.fileSize());
    }

    @Test
    public void testFileSizeAndStoreDataDoesChangeWhenListening() throws Exception {
        Assert.assertEquals(100, androidSensor.fileSize());
        Assert.assertTrue(androidSensor.startListening());
//        for (int i = 0; i < 10; i++) {
//            androidSensor.sessionValues().add("Test" + i);
//        }
        Assert.assertEquals(10, androidSensor.sessionValues().size());
        sizeOfUpload = 200;
        androidSensor.storeData();
        Assert.assertEquals(0, androidSensor.sessionValues().size());
        Assert.assertTrue(androidSensor.fileSize() > 200);
    }

    @Test
    public void testOpenStream() throws Exception {

    }

    @Test
    public void testCloseStream() throws Exception {

    }

    @Test
    public void testPrepareForUpload() throws Exception {
        Field outerStream = AbstractZippingAndroidDetector.class.getDeclaredField("outerStream");
        outerStream.setAccessible(true);
        ZipOutputStream zipStream = (ZipOutputStream) outerStream.get(androidSensor);
        Assert.assertNull(zipStream);
        Assert.assertTrue(androidSensor.startListening());
        zipStream = (ZipOutputStream) outerStream.get(androidSensor);
        Assert.assertNotNull(zipStream);

        androidSensor.prepareUpload();
        Assert.assertNotSame(outerStream.get(androidSensor), zipStream);
        // It is recreated, should not be identical
    }
}
package com.telenor.possumlib.abstractdetectortests;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.FileManipulator;
import com.telenor.possumlib.JodaInit;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.interfaces.ISensorStatusUpdate;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PossumTestRunner.class)
public class AbstractDetectorTest {
    private AbstractDetector abstractDetector;
    private boolean isActuallyEnabled;
    private boolean isActuallyAvailable;
    private File fakedStoredData;
    private boolean didChangeSensorUpdate;
    private long timestamp;
    private EventBus eventBus;

    @Before
    public void setUp() throws Exception {
        isActuallyEnabled = true;
        isActuallyAvailable = true;
        didChangeSensorUpdate = false;
        eventBus = new EventBus();
        timestamp = System.currentTimeMillis();
        JodaInit.initializeJodaTime();
        fakedStoredData = FileManipulator.getFileWithName(RuntimeEnvironment.application, "Accelerometer");
        if (fakedStoredData.exists()) {
            Assert.assertTrue(fakedStoredData.delete());
            Assert.assertTrue(fakedStoredData.createNewFile());
        }
        abstractDetector = getDetector(RuntimeEnvironment.application, eventBus, "Accelerometer");
    }

    @After
    public void tearDown() throws Exception {
        abstractDetector = null;
        if (fakedStoredData.exists()) {
            Assert.assertTrue(fakedStoredData.delete());
        }
    }

    private AbstractDetector getDetector(Context context, EventBus eventBus, final String detectorName) {
        return new AbstractDetector(context, "fakeUnique", "fakeId", eventBus) {
            @Override
            public boolean isEnabled() {
                return isActuallyEnabled;
            }

            @Override
            public boolean isAvailable() {
                return isActuallyAvailable;
            }

            @Override
            public int detectorType() {
                return DetectorType.Accelerometer;
            }

            @Override
            public String detectorName() {
                return detectorName;
            }

            @Override
            public long timestamp() {
                return timestamp;
            }
        };
    }

    @Test
    public void testNow() throws Exception {
        long present = DateTime.now().getMillis();
        Thread.sleep(2);
        Assert.assertTrue(abstractDetector.now() > present);
    }

    @Test
    public void testInitialize() throws Exception {
        Assert.assertNotNull(abstractDetector);
    }

    @Test
    public void testInvalidConstructor() throws Exception {
        try {
            abstractDetector = getDetector(null, eventBus, "Accelerometer");
            Assert.fail("Should not have been able to construct detector");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Missing context on detector:"));
        }
    }

    @Test
    public void testConfirmSessionValuesIsNotStatic() throws Exception {
        AbstractDetector detector1 = getDetector(RuntimeEnvironment.application, eventBus, "Accelerometer");
        AbstractDetector detector2 = getDetector(RuntimeEnvironment.application, eventBus, "Accelerometer");
        Assert.assertEquals(0, detector1.sessionValues().size());
        Assert.assertEquals(0, detector2.sessionValues().size());
        detector1.sessionValues().add("test");
        Assert.assertEquals(1, detector1.sessionValues().size());
        Assert.assertEquals(0, detector2.sessionValues().size());
    }

    @Test
    public void testFileSize() throws Exception {
        Assert.assertEquals(0, abstractDetector.fileSize());
        FileManipulator.fillFile(fakedStoredData);
        Assert.assertEquals(160, abstractDetector.fileSize());
    }

    @Test
    public void testStartListeningWhenEnabled() throws Exception {
        Assert.assertTrue(abstractDetector.startListening());
        Assert.assertTrue(abstractDetector.isListening());
    }

    @Test
    public void testStartListeningWhenDisabled() throws Exception {
        isActuallyEnabled = false;
        Assert.assertFalse(abstractDetector.startListening());
        Assert.assertFalse(abstractDetector.isListening());
    }

    @Test
    public void testDefaultValues() throws Exception {
        Assert.assertFalse(abstractDetector.isWakeUpDetector());
        Assert.assertTrue(abstractDetector.isPermitted());
    }

    @Test
    public void testStopListeningDisablesListeningWhenEnabled() throws Exception {
        abstractDetector.startListening();
        abstractDetector.stopListening();
        Assert.assertFalse(abstractDetector.isListening());
    }

    @Test
    public void testStopListeningStoresValidSet() throws Exception {
        abstractDetector.startListening();
        abstractDetector.sessionValues().add("test");
        abstractDetector.sessionValues().add("test2");
        abstractDetector.stopListening();
        // TODO: Fake file does not work well with storeLines() method. Either mock it up better, or find another way
    }

    @Test
    public void testStopListeningIgnoresInvalidSet() throws Exception {

    }

    @Test
    public void testTerminateStopsListening() throws Exception {
        abstractDetector.startListening();
        abstractDetector.terminate();
        Assert.assertFalse(abstractDetector.isListening());
    }

    @Test
    public void testSensorUpdateListener() throws Exception {
        ISensorStatusUpdate listener = new ISensorStatusUpdate() {
            @Override
            public void sensorStatusChanged(int sensorType) {
                didChangeSensorUpdate = true;
            }
        };
        abstractDetector.sensorStatusChanged();
        Assert.assertFalse(didChangeSensorUpdate);
        abstractDetector.addSensorUpdateListener(listener);
        abstractDetector.sensorStatusChanged();
        Assert.assertTrue(didChangeSensorUpdate);
        didChangeSensorUpdate = false;
        abstractDetector.removeSensorUpdateListener(listener);
        abstractDetector.sensorStatusChanged();
        Assert.assertFalse(didChangeSensorUpdate);
    }

    @Test
    public void testContext() throws Exception {
        Assert.assertEquals(RuntimeEnvironment.application, abstractDetector.context());
    }

    @Test
    public void testUploadedData() throws Exception {
        abstractDetector = getDetector(RuntimeEnvironment.application, eventBus, "Accelerometer");
        FileWriter writer = new FileWriter(fakedStoredData);
        for (int i = 0; i < 100; i++) {
            writer.append("test\r\n");
            abstractDetector.sessionValues().add("test");
        }
        writer.close();
        Assert.assertEquals(600, fakedStoredData.length());
        Assert.assertEquals(100, abstractDetector.sessionValues().size());
        abstractDetector.uploadedData(new Exception("Avoid completely all interaction with sensorFile"));
        Assert.assertEquals(100, abstractDetector.sessionValues().size());
        Assert.assertEquals(600, fakedStoredData.length());
        abstractDetector.uploadedData(null);
        Assert.assertFalse(fakedStoredData.exists());
        Assert.assertTrue(abstractDetector.sessionValues().isEmpty());
    }

    @Test
    public void testCompareTo() throws Exception {
        Context mockedContext = mock(Context.class);
        AbstractDetector detectorA = getDetector(mockedContext, eventBus, "Gyroscope");
        AbstractDetector detectorB = getDetector(mockedContext, eventBus, "Accelerometer");
        Assert.assertTrue(detectorA.compareTo(detectorB) > 1);
    }

    @Test
    public void testLock() throws Exception {
        Method lockMethod = AbstractDetector.class.getDeclaredMethod("lock");
        lockMethod.setAccessible(true);
        lockMethod.invoke(abstractDetector);
    }

    @Test
    public void testUnlockWhenNotLockedIsIgnored() throws Exception {
        Method unlockMethod = AbstractDetector.class.getDeclaredMethod("unlock");
        unlockMethod.setAccessible(true);
        unlockMethod.invoke(abstractDetector);
    }

    @Test
    public void testLockThenUnlock() throws Exception {
        Method lockMethod = AbstractDetector.class.getDeclaredMethod("lock");
        lockMethod.setAccessible(true);
        Method unlockMethod = AbstractDetector.class.getDeclaredMethod("unlock");
        unlockMethod.setAccessible(true);

        Field reentrantLock = AbstractDetector.class.getDeclaredField("lock");
        reentrantLock.setAccessible(true);
        ReentrantLock lock = (ReentrantLock) reentrantLock.get(abstractDetector);
        Assert.assertFalse(lock.isLocked());
        lockMethod.invoke(abstractDetector);
        lock = (ReentrantLock) reentrantLock.get(abstractDetector);
        Assert.assertTrue(lock.isLocked());
        unlockMethod.invoke(abstractDetector);
        lock = (ReentrantLock) reentrantLock.get(abstractDetector);
        Assert.assertFalse(lock.isLocked());
    }

    @SuppressWarnings("all")
    @Test
    public void testPrepareForUploadWithEmptyFile() throws Exception {
        fakedStoredData = mock(File.class);
        when(fakedStoredData.length()).thenReturn(0L);
        abstractDetector.prepareUpload();
        verify(fakedStoredData, never()).getAbsolutePath();

        Method stageMethod = AbstractDetector.class.getDeclaredMethod("stageForUpload", File.class);
        stageMethod.setAccessible(true);
        Assert.assertFalse((boolean) stageMethod.invoke(abstractDetector, fakedStoredData));
    }

    @Test
    public void testBucketKey() throws Exception {
        Method bucketKeyMethod = AbstractDetector.class.getDeclaredMethod("bucketKey");
        bucketKeyMethod.setAccessible(true);
        String bucketKey = (String) bucketKeyMethod.invoke(abstractDetector);
        Context context = RuntimeEnvironment.application;
        PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        String shouldBe = "data/" + packageInfo.versionName + "/Accelerometer/fakeUnique/fakeId/" + timestamp + ".zip";
        Assert.assertEquals(shouldBe, bucketKey);
    }

    @Test
    public void testPrepareForUploadWithFilledFile() throws Exception {
        FileManipulator.fillFile(fakedStoredData);

        Assert.assertTrue(fakedStoredData.exists());
        Assert.assertTrue(fakedStoredData.length() > 0);

        Method bucketKeyMethod = AbstractDetector.class.getDeclaredMethod("bucketKey");
        bucketKeyMethod.setAccessible(true);
        String bucketKey = (String) bucketKeyMethod.invoke(abstractDetector);
        bucketKey = bucketKey.replace("/", "#");
        File zippedFile = new File(RuntimeEnvironment.application.getFilesDir(), "data/Upload/" + bucketKey);
        Assert.assertFalse(zippedFile.exists());
        abstractDetector.prepareUpload();
        Assert.assertFalse(fakedStoredData.exists());
        Assert.assertTrue(zippedFile.exists());
    }

    @Test
    public void testTimestamp() throws Exception {
        abstractDetector = getDetector(RuntimeEnvironment.application, eventBus, "Accelerometer");
        long now = DateTime.now().getMillis();
        Method timestampMethod = AbstractDetector.class.getDeclaredMethod("timestamp");
        timestampMethod.setAccessible(true);
        long result = (long) timestampMethod.invoke(abstractDetector);
        Assert.assertEquals(timestamp, result);
        Assert.assertTrue(now >= result);
    }
}
package com.telenor.possumlib.abstractdetectortests;

import android.content.Context;

import com.google.gson.JsonArray;
import com.telenor.possumlib.AwesomePossum;
import com.telenor.possumlib.FileManipulator;
import com.telenor.possumlib.JodaInit;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.changeevents.PossumEvent;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.interfaces.ISensorStatusUpdate;
import com.telenor.possumlib.models.PossumBus;

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
import java.util.ArrayList;
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
    private PossumBus eventBus;

    @Before
    public void setUp() throws Exception {
        isActuallyEnabled = true;
        isActuallyAvailable = true;
        didChangeSensorUpdate = false;
        eventBus = new PossumBus();
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

    private AbstractDetector getDetector(Context context, PossumBus eventBus, final String detectorName) {
        return new AbstractDetector(context, eventBus) {
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
            public long now() {
                return timestamp;
            }
        };
    }

    @Test
    public void testNow() throws Exception {
        abstractDetector = new AbstractDetector(RuntimeEnvironment.application, eventBus) {
            @Override
            public int detectorType() {
                return DetectorType.Accelerometer;
            }

            @Override
            public String detectorName() {
                return "accelerometer";
            }
        };
        long present = DateTime.now().getMillis();
        Thread.sleep(2);
        Assert.assertTrue(abstractDetector.now() > present);
    }

    @Test
    public void testEventReceived() throws Exception {
        abstractDetector.eventReceived(new PossumEvent("meh", "meh"));
    }

    @Test
    public void testSetModel() throws Exception {
        abstractDetector.setModel("meh");
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
    public void testInternalList() throws Exception {
        Assert.assertTrue(abstractDetector.sessionValues() instanceof ArrayList);
    }

    @Test
    public void testSetUniqueUserId() throws Exception {
        Assert.assertTrue(abstractDetector.toJson().get("uniqueUserId").isJsonNull());
        abstractDetector.setUniqueUser("fooey");
        Assert.assertEquals("fooey", abstractDetector.toJson().get("uniqueUserId").getAsString());
    }

    @Test
    public void testEventBus() throws Exception {
        Assert.assertEquals(eventBus, abstractDetector.eventBus());
    }

    @Test
    public void testGettingJsonDataFromValues() throws Exception {
        Assert.assertTrue(abstractDetector.jsonData().size() == 0);
        abstractDetector.sessionValues().add(new JsonArray());
        Assert.assertTrue(abstractDetector.jsonData().size() == 1);
    }

    @Test
    public void testIsAuthenticating() throws Exception {
        Assert.assertFalse(abstractDetector.isAuthenticating());
        abstractDetector.setAuthenticating(true);
        Assert.assertTrue(abstractDetector.isAuthenticating());
    }

    @Test
    public void testConfirmSessionValuesIsNotStatic() throws Exception {
        AbstractDetector detector1 = getDetector(RuntimeEnvironment.application, eventBus, "Accelerometer");
        AbstractDetector detector2 = getDetector(RuntimeEnvironment.application, eventBus, "Accelerometer");
        Assert.assertEquals(0, detector1.sessionValues().size());
        Assert.assertEquals(0, detector2.sessionValues().size());
//        detector1.sessionValues().add("test");
//        Assert.assertEquals(1, detector1.sessionValues().size());
//        Assert.assertEquals(0, detector2.sessionValues().size());
    }

    @Test
    public void testFileSize() throws Exception {
        Assert.assertEquals(0, abstractDetector.fileSize());
        FileManipulator.fillFile(fakedStoredData);
        Assert.assertEquals(160, abstractDetector.fileSize());
    }

    @Test
    public void testStartListeningWhenEnabled() throws Exception {
//        Assert.assertTrue(abstractDetector.startListening());
//        Assert.assertTrue(abstractDetector.isListening());
    }

    @Test
    public void testStartListeningWhenDisabled() throws Exception {
        isActuallyEnabled = false;
        Assert.assertFalse(abstractDetector.startListening());
        Assert.assertFalse(abstractDetector.isListening());
    }

    @Test
    public void testDefaultValues() throws Exception {
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
//        abstractDetector.sessionValues().add("test");
//        abstractDetector.sessionValues().add("test2");
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
//            abstractDetector.sessionValues().add("test");
        }
        writer.close();
        Assert.assertEquals(600, fakedStoredData.length());
//        Assert.assertEquals(100, abstractDetector.sessionValues().size());
//        abstractDetector.uploadedData(new Exception("Avoid completely all interaction with sensorFile"));
//        Assert.assertEquals(100, abstractDetector.sessionValues().size());
//        Assert.assertEquals(600, fakedStoredData.length());
//        abstractDetector.uploadedData(null);
//        Assert.assertFalse(fakedStoredData.exists());
//        Assert.assertTrue(abstractDetector.sessionValues().isEmpty());
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
        String shouldBe = "possumlibdata/" + AwesomePossum.versionName(RuntimeEnvironment.application) + "/Accelerometer/fakeUnique/" + timestamp + ".zip";
//        Assert.assertEquals(shouldBe, bucketKey);
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
}
package com.telenor.possumlib.abstractdetectortests;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;

import com.telenor.possumlib.FileManipulator;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.abstractdetectors.AbstractDetectorReceiver;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.interfaces.ISensorStatusUpdate;
import com.telenor.possumlib.models.PossumBus;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PossumTestRunner.class)
public class AbstractDetectorReceiverTest {
    private AbstractDetectorReceiver abstractDetectorReceiver;
    @Mock
    private Context mockedContext;

    private boolean isEnabled;
    private PossumBus eventBus;
    private boolean sensorDidChange;
    private File fakeFile;
    private boolean onReceiveFired;
    private long storageSizeInUpload;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        eventBus = new PossumBus();
        isEnabled = true;
        storageSizeInUpload = 0;
        onReceiveFired = false;
        sensorDidChange = false;
        fakeFile = FileManipulator.getFileWithName(RuntimeEnvironment.application, "Network");
        abstractDetectorReceiver = getDetector(mockedContext, eventBus, DetectorType.Wifi, "Network");
    }

    @After
    public void tearDown() throws Exception {
        abstractDetectorReceiver = null;
    }

    private AbstractDetectorReceiver getDetector(Context context, PossumBus eventBus, final int detectType, final String detectorName) {
        return new AbstractDetectorReceiver(context, Arrays.asList("test1","test2"), "fakeUnique", eventBus, false) {
            @Override
            public void onReceive(Context context, Intent intent) {
                onReceiveFired = true;
            }

            @Override
            public int detectorType() {
                return detectType;
            }

            @Override
            public String detectorName() {
                return detectorName;
            }

            @Override
            public boolean isAvailable() {
                return true;
            }

            @Override
            public String requiredPermission() {
                return null;
            }

            @Override
            public long authenticationListenInterval() {
                return 0;
            }

            @Override
            public boolean isEnabled() {
                return isEnabled;
            }

            @Override
            protected long uploadFilesSize() {
                return storageSizeInUpload;
            }

            @Override
            public File storedData() {
                return fakeFile;
            }
        };
    }

    @Test
    public void testInvalidInit() throws Exception {
        try {
            abstractDetectorReceiver = getDetector(null, eventBus, DetectorType.Wifi, "Network");
            Assert.fail("Should not have accepted detector without context");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Missing context on detector:"));
        }
    }

    @Test
    public void testPermitted() throws Exception {
        Assert.assertTrue(abstractDetectorReceiver.isPermitted());
    }

    @Test
    public void testOnReceivePassesOnEventIfListening() throws Exception {
        abstractDetectorReceiver = getDetector(RuntimeEnvironment.application, eventBus, DetectorType.Wifi, "Network");
        Field intentFilterField = AbstractDetectorReceiver.class.getDeclaredField("intentFilter");
        intentFilterField.setAccessible(true);
        IntentFilter intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilterField.set(abstractDetectorReceiver, intentFilter);
        abstractDetectorReceiver.startListening();
        RuntimeEnvironment.application.sendBroadcast(new Intent(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        Assert.assertTrue(onReceiveFired);
    }

    @Test
    public void testOnReceivePassesOnEventIfNotListening() throws Exception {
        abstractDetectorReceiver = getDetector(RuntimeEnvironment.application, eventBus, DetectorType.Wifi, "Network");
        Field intentFilterField = AbstractDetectorReceiver.class.getDeclaredField("intentFilter");
        intentFilterField.setAccessible(true);
        IntentFilter intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilterField.set(abstractDetectorReceiver, intentFilter);
        Assert.assertFalse(abstractDetectorReceiver.isListening());
        RuntimeEnvironment.application.sendBroadcast(new Intent(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        Assert.assertFalse(onReceiveFired);
    }

    @Test
    public void testStartListeningWhileEnabled() throws Exception {
        Assert.assertFalse(abstractDetectorReceiver.isListening());
        Assert.assertTrue(abstractDetectorReceiver.startListening());
        verify(mockedContext).registerReceiver(any(BroadcastReceiver.class), any(IntentFilter.class));
        Assert.assertTrue(abstractDetectorReceiver.isListening());
    }

    @Test
    public void testStartListeningWhileDisabled() throws Exception {
        isEnabled = false;
        Assert.assertFalse(abstractDetectorReceiver.startListening());
        Assert.assertFalse(abstractDetectorReceiver.isListening());
    }

    @Test
    public void testStopListening() throws Exception {
        abstractDetectorReceiver.startListening();
        when(mockedContext.getFilesDir()).thenReturn(RuntimeEnvironment.application.getFilesDir());
        when(mockedContext.getString(anyInt())).thenReturn("DetectorReceiver");
        File detectorFile = new File(RuntimeEnvironment.application.getFilesDir().getAbsolutePath() + "/data/DetectorReceiver");
        if (detectorFile.exists()) {
            Assert.assertTrue(detectorFile.delete());
        }
        Assert.assertTrue(abstractDetectorReceiver.isListening());
        Assert.assertEquals(0, abstractDetectorReceiver.sessionValues().size());
//        abstractDetectorReceiver.sessionValues().add("test");
        Assert.assertEquals(1, abstractDetectorReceiver.sessionValues().size());
        abstractDetectorReceiver.stopListening();
        verify(mockedContext).unregisterReceiver(any(BroadcastReceiver.class));
        Assert.assertFalse(abstractDetectorReceiver.isListening());
        Assert.assertEquals(0, abstractDetectorReceiver.sessionValues().size());
        abstractDetectorReceiver.stopListening();
    }

    @Test
    public void testStoredData() throws Exception {
        abstractDetectorReceiver = getDetector(RuntimeEnvironment.application, eventBus, DetectorType.Wifi, "Network");
        File fakeFile = abstractDetectorReceiver.storedData();
        Assert.assertTrue(fakeFile.exists());
        Assert.assertEquals(0, fakeFile.length());
    }

    @Test
    public void testFileLength() throws Exception {
        Assert.assertEquals(0, abstractDetectorReceiver.fileSize());
        FileManipulator.fillFile(fakeFile);
        Assert.assertEquals(160, abstractDetectorReceiver.fileSize());
    }

    @Test
    public void testTerminateRunsStop() throws Exception {
        abstractDetectorReceiver.startListening();
        when(mockedContext.getFilesDir()).thenReturn(RuntimeEnvironment.application.getFilesDir());
        when(mockedContext.getString(anyInt())).thenReturn("DetectorReceiver");
        File detectorFile = new File(RuntimeEnvironment.application.getFilesDir().getAbsolutePath() + "/data/DetectorReceiver");
        if (detectorFile.exists()) {
            Assert.assertTrue(detectorFile.delete());
        }
        Assert.assertTrue(abstractDetectorReceiver.isListening());
        Assert.assertEquals(0, abstractDetectorReceiver.sessionValues().size());
//        abstractDetectorReceiver.sessionValues().add("test");
        Assert.assertEquals(1, abstractDetectorReceiver.sessionValues().size());
        abstractDetectorReceiver.terminate();
        verify(mockedContext).unregisterReceiver(any(BroadcastReceiver.class));
        Assert.assertFalse(abstractDetectorReceiver.isListening());
        Assert.assertEquals(0, abstractDetectorReceiver.sessionValues().size());

        abstractDetectorReceiver.stopListening();
    }

    @Test
    public void testUploadedData() throws Exception {
        Context mockedContext = mock(Context.class);
        when(mockedContext.getString(anyInt())).thenReturn("wifi");
        abstractDetectorReceiver = getDetector(mockedContext, eventBus, DetectorType.Wifi, "Network");
        FileWriter writer = null;
        try {
            writer = new FileWriter(fakeFile);
            for (int i = 0; i < 100; i++) {
                writer.append("test\r\n");
//                abstractDetectorReceiver.sessionValues().add("test");
            }
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ignore) {
                }
            }
        }
        Assert.assertEquals(600, fakeFile.length());
        Assert.assertEquals(100, abstractDetectorReceiver.sessionValues().size());
        abstractDetectorReceiver.uploadedData(new Exception("Avoid completely all interaction with sensorFile"));
        Assert.assertEquals(100, abstractDetectorReceiver.sessionValues().size());
        Assert.assertEquals(600, fakeFile.length());
        abstractDetectorReceiver.uploadedData(null);
        Assert.assertFalse(fakeFile.exists());
        Assert.assertTrue(abstractDetectorReceiver.sessionValues().isEmpty());
    }

    @Test
    public void testSensorUpdateListener() throws Exception {
        ISensorStatusUpdate listener = new ISensorStatusUpdate() {
            @Override
            public void sensorStatusChanged(int sensorType) {
                sensorDidChange = true;
                Assert.assertEquals(DetectorType.Wifi, sensorType);
            }
        };
        abstractDetectorReceiver.addSensorUpdateListener(listener);
        Assert.assertFalse(sensorDidChange);
        abstractDetectorReceiver.sensorStatusChanged();
        Assert.assertTrue(sensorDidChange);
        sensorDidChange = false;
        abstractDetectorReceiver.removeSensorUpdateListener(listener);
        abstractDetectorReceiver.sensorStatusChanged();
        Assert.assertFalse(sensorDidChange);
    }

    @Test
    public void testCompareTo() throws Exception {
        Context mockedContext = mock(Context.class);
        AbstractDetectorReceiver detectorReceiverA = getDetector(mockedContext, eventBus, DetectorType.Wifi, "Network");
        AbstractDetectorReceiver detectorReceiverB = getDetector(mockedContext, eventBus, DetectorType.MetaData, "MetaData");
        Assert.assertTrue(detectorReceiverA.compareTo(detectorReceiverB) > 0);
    }

    @Test
    public void testIntentFilter() throws Exception {
        Method intentFilterMethod = AbstractDetectorReceiver.class.getDeclaredMethod("intentFilter");
        intentFilterMethod.setAccessible(true);
        IntentFilter intentFilter = (IntentFilter)intentFilterMethod.invoke(abstractDetectorReceiver);
        Assert.assertNotNull(intentFilter);
        Assert.assertTrue(intentFilter.hasAction("test1"));
        Assert.assertTrue(intentFilter.hasAction("test2"));
    }
}
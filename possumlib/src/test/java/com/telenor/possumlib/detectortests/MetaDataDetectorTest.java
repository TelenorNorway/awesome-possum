package com.telenor.possumlib.detectortests;

import android.content.Context;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.FileManipulator;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.constants.Constants;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.detectors.MetaDataDetector;
import com.telenor.possumlib.utils.FileUtil;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

@RunWith(PossumTestRunner.class)
public class MetaDataDetectorTest {
    private MetaDataDetector metaDataDetector;
    private Context mockedContext;
    private EventBus eventBus;
    private File fakeFile;
    @Before
    public void setUp() throws Exception {
        mockedContext = Mockito.mock(Context.class);
        eventBus = new EventBus();
        File dataDir = FileManipulator.getDataDir(RuntimeEnvironment.application);
        FileUtil.clearDirectory(RuntimeEnvironment.application, dataDir);
        fakeFile = FileManipulator.getFileWithName(RuntimeEnvironment.application, "fakeish");
        Mockito.when(mockedContext.getFilesDir()).thenReturn(RuntimeEnvironment.application.getFilesDir());
        Mockito.when(mockedContext.getString(Mockito.anyInt())).thenReturn("fakeish");
        metaDataDetector = new MetaDataDetector(mockedContext, "fakeUnique", "fakeId", eventBus) {
            @Override
            public File storedData() {
                return fakeFile;
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        metaDataDetector = null;
        mockedContext = null;
        if (fakeFile.exists()) {
            Assert.assertTrue(fakeFile.delete());
        }
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertNotNull(metaDataDetector);
    }

    @Test
    public void testInvalidObject() throws Exception {
//        metaDataDetector.objectChanged("fakeOut");
        Assert.assertEquals(0, fakeFile.length());
        Assert.assertTrue(metaDataDetector.startListening());
//        metaDataDetector.objectChanged("fakeOut2");
        metaDataDetector.stopListening();
    }

    @Test
    public void testObjectChangedBeforeListening() throws Exception {
        Assert.assertEquals(0, metaDataDetector.sessionValues().size());
//        metaDataDetector.objectChanged(new EventObject(MetaDataDetector.GENERAL_EVENT, "USER UNLOCKED"));
        Assert.assertEquals(0, metaDataDetector.sessionValues().size());
    }

    @Test
    public void testObjectChangedWhileListening() throws Exception {
        Assert.assertTrue(metaDataDetector.startListening());
//        metaDataDetector.objectChanged(new EventObject(MetaDataDetector.GENERAL_EVENT, "USER UNLOCKED"));

        long fileSize = fakeFile.length();
        Assert.assertTrue(fileSize > 0);
        List<String> output = readFile();
        Assert.assertEquals(1, output.size());
        Assert.assertTrue(output.get(0).contains(" USER UNLOCKED"));

//        metaDataDetector.objectChanged(new EventObject(Constants.DAILY_TASK_EVENT, Constants.DAILY_TASK_START));
//        long fileSize2 = fakeFile.length();
//        Assert.assertTrue(fileSize2 > fileSize);
//        List<String> output2 = readFile();
//        Assert.assertEquals(2, output2.size());
//        Assert.assertTrue(output2.get(1).contains(" DAILY_TASK DAILY_STARTED"));
//
//        metaDataDetector.objectChanged(new EventObject("Bah_Humbug", false));
//        Assert.assertEquals(fileSize2, fakeFile.length());
    }

    @Test
    public void testPowerPresent() throws Exception {
        Assert.assertEquals(0, fakeFile.length());
        Assert.assertTrue(metaDataDetector.startListening());
//        metaDataDetector.objectChanged(new EventObject(MetaDataDetector.GENERAL_EVENT, "POWER_PLUGGED IN"));
        long fileSize = fakeFile.length();
        Assert.assertTrue(fileSize > 0);
        List<String> output = readFile();
        Assert.assertEquals(1, output.size());
        Assert.assertTrue(output.get(0).contains(" POWER_PLUGGED IN"));
    }

    @Test
    public void testBatteryChanged() throws Exception {
        Assert.assertEquals(0, fakeFile.length());
        Assert.assertTrue(metaDataDetector.startListening());
//        metaDataDetector.objectChanged(new EventObject(MetaDataDetector.BATTERY_CHANGED, 98));
        long fileSize = fakeFile.length();
        Assert.assertTrue(fileSize > 0);
        List<String> output = readFile();
        Assert.assertEquals(1, output.size());
        Assert.assertTrue(output.get(0).contains(" BATTERY_CHANGED 98"));
    }

    @Test
    public void testUserPausedGather() throws Exception {
        Assert.assertEquals(0, fakeFile.length());
        Assert.assertTrue(metaDataDetector.startListening());
        long timestamp = DateTime.now().getMillis();
//        metaDataDetector.objectChanged(new EventObject(MetaDataDetector.GENERAL_EVENT, "USER_PAUSED_GATHER RESTARTING AT "+timestamp));
//        metaDataDetector.objectChanged(new EventObject(MetaDataDetector.GENERAL_EVENT, "USER_PAUSED_GATHER RESTARTING NEVER"));
        long fileSize = fakeFile.length();
        Assert.assertTrue(fileSize > 0);
        List<String> output = readFile();
        Assert.assertEquals(2, output.size());
        Assert.assertTrue(output.get(0).contains(" USER_PAUSED_GATHER RESTARTING AT "+timestamp));
        Assert.assertTrue(output.get(1).contains(" USER_PAUSED_GATHER RESTARTING NEVER"));
    }

    @Test
    public void testGatherStatusChanged() throws Exception {
        Assert.assertEquals(0, fakeFile.length());
        Assert.assertTrue(metaDataDetector.startListening());
//        metaDataDetector.objectChanged(new EventObject(MetaDataDetector.GENERAL_EVENT, Constants.GATHER_STARTED));
//        metaDataDetector.objectChanged(new EventObject(MetaDataDetector.GENERAL_EVENT, Constants.GATHER_PAUSED));
        long fileSize = fakeFile.length();
        Assert.assertTrue(fileSize > 0);
        List<String> output = readFile();
        Assert.assertEquals(2, output.size());
        Assert.assertTrue(output.get(0).contains(" "+Constants.GATHER_STARTED));
        Assert.assertTrue(output.get(1).contains(" "+Constants.GATHER_PAUSED));
    }

    @Test
    public void testUserActiveChanged() throws Exception {
        Assert.assertEquals(0, fakeFile.length());
        Assert.assertTrue(metaDataDetector.startListening());
//        metaDataDetector.objectChanged(new EventObject(MetaDataDetector.GENERAL_EVENT, "USER_ACTIVE ACTIVE"));
//        metaDataDetector.objectChanged(new EventObject(MetaDataDetector.GENERAL_EVENT, "USER_ACTIVE INACTIVE"));
        long fileSize = fakeFile.length();
        Assert.assertTrue(fileSize > 0);
        List<String> output = readFile();
        Assert.assertEquals(2, output.size());
        Assert.assertTrue(output.get(0).contains(" USER_ACTIVE ACTIVE"));
        Assert.assertTrue(output.get(1).contains(" USER_ACTIVE INACTIVE"));
    }

    @Test
    public void testDefaultValues() throws Exception {
        Assert.assertTrue(metaDataDetector.isEnabled());
        Assert.assertTrue(metaDataDetector.isValidSet());
        Assert.assertTrue(metaDataDetector.isAvailable());

        Assert.assertEquals(DetectorType.MetaData, metaDataDetector.detectorType());
    }

    private List<String> readFile() throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fakeFile));
        String line;
        List<String> output = new ArrayList<>();
        while ((line = bufferedReader.readLine()) != null) {
            output.add(line);
        }
        bufferedReader.close();
        return output;
    }
}
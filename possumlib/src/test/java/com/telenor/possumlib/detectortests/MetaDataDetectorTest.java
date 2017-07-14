package com.telenor.possumlib.detectortests;

import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.changeevents.MetaDataChangeEvent;
import com.telenor.possumlib.changeevents.SatelliteChangeEvent;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.detectors.MetaDataDetector;
import com.telenor.possumlib.models.PossumBus;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

@RunWith(PossumTestRunner.class)
public class MetaDataDetectorTest {
    private MetaDataDetector metaDataDetector;
    private PossumBus eventBus;
    private File fakeFile;
    @Before
    public void setUp() throws Exception {
        eventBus = new PossumBus();
        fakeFile = new File(RuntimeEnvironment.application.getFilesDir()+"/MetaData");
        if (fakeFile.exists()) Assert.assertTrue(fakeFile.delete());
        Assert.assertTrue(fakeFile.createNewFile());
        metaDataDetector = new MetaDataDetector(RuntimeEnvironment.application, eventBus) {
            @Override
            public File storedData() {
                return fakeFile;
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        metaDataDetector = null;
        eventBus = null;
        if (fakeFile.exists()) {
            Assert.assertTrue(fakeFile.delete());
        }
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertNotNull(metaDataDetector);
    }

    @Test
    public void testInvalidEvent() throws Exception {
        Assert.assertEquals(0, fakeFile.length());
        Assert.assertEquals(0, metaDataDetector.sessionValues().size());
        Assert.assertTrue(metaDataDetector.startListening());
        eventBus.post(new SatelliteChangeEvent("invalid"));
        Assert.assertEquals(0, metaDataDetector.sessionValues().size());
        Assert.assertEquals(0, fakeFile.length());
    }

    @Test
    public void testValidEventWhileListening() throws Exception {
        Assert.assertEquals(0, fakeFile.length());
        Assert.assertTrue(metaDataDetector.startListening());
        eventBus.post(new MetaDataChangeEvent("USER UNLOCKED"));
        long fileSize = fakeFile.length();
        Assert.assertTrue(fileSize > 0);
        List<String> output = readFile();
        Assert.assertEquals(1, output.size());
        Assert.assertTrue(output.get(0).equals("USER UNLOCKED"));

        eventBus.post(new MetaDataChangeEvent("USER UNLOCKED2"));
        long fileSize2 = fakeFile.length();
        Assert.assertTrue(fileSize2 > fileSize);
        List<String> output2 = readFile();
        Assert.assertEquals(2, output2.size());
        Assert.assertTrue(output2.get(1).equals("USER UNLOCKED2"));
    }

    @Test
    public void testDefaultValues() throws Exception {
        Assert.assertTrue(metaDataDetector.isEnabled());
        Assert.assertTrue(metaDataDetector.isAvailable());
        Assert.assertEquals(DetectorType.MetaData, metaDataDetector.detectorType());
        Assert.assertEquals("MetaData", metaDataDetector.detectorName());
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
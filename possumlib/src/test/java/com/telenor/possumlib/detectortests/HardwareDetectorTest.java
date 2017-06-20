package com.telenor.possumlib.detectortests;

import com.google.common.io.CharStreams;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.detectors.HardwareDetector;
import com.telenor.possumlib.models.PossumBus;
import com.telenor.possumlib.utils.FileUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.io.FileReader;
import java.util.List;

@RunWith(PossumTestRunner.class)
public class HardwareDetectorTest {
    private HardwareDetector hardwareDetector;
    private PossumBus eventBus;
    private File fakeFile;
    @Before
    public void setUp() throws Exception {
        eventBus = new PossumBus();
        fakeFile = FileUtil.getFile(RuntimeEnvironment.application, "Hardware");
        if (fakeFile.exists()) {
            Assert.assertTrue(fakeFile.delete());
        }
        hardwareDetector = new HardwareDetector(RuntimeEnvironment.application, "id", eventBus, false);
    }

    @After
    public void tearDown() throws Exception {
        eventBus = null;
        hardwareDetector = null;
        if (fakeFile.exists()) {
            Assert.assertTrue(fakeFile.delete());
        }
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertNotNull(hardwareDetector);
    }

    @Test
    public void testDefaults() throws Exception {
        Assert.assertTrue(hardwareDetector.isEnabled());
        Assert.assertTrue(hardwareDetector.isAvailable());
        Assert.assertEquals(DetectorType.Hardware, hardwareDetector.detectorType());
        Assert.assertEquals("Hardware", hardwareDetector.detectorName());
        Assert.assertTrue(fakeFile.length() > 0);
    }

    @Test
    public void testStoredData() throws Exception {
        List<String> content = CharStreams.readLines(new FileReader(fakeFile));
        Assert.assertEquals("HARDWARE_INFO START", content.get(0));
        Assert.assertTrue(content.get(1).startsWith("Board:"));
        Assert.assertTrue(content.get(2).startsWith("Brand:"));
        Assert.assertTrue(content.get(3).startsWith("Device:"));
        Assert.assertTrue(content.get(4).startsWith("Display:"));
        Assert.assertTrue(content.get(5).startsWith("Fingerprint:"));
        Assert.assertTrue(content.get(6).startsWith("Hardware:"));
        Assert.assertTrue(content.get(7).startsWith("Host:"));
        Assert.assertTrue(content.get(8).startsWith("Id:"));
        Assert.assertTrue(content.get(9).startsWith("Manufacturer:"));
        Assert.assertTrue(content.get(10).startsWith("Model:"));
        Assert.assertTrue(content.get(11).startsWith("Product:"));
        Assert.assertTrue(content.get(12).startsWith("Serial:"));
        Assert.assertTrue(content.get(13).startsWith("Version:"));
        Assert.assertTrue(content.get(14).startsWith("SupportedABIS:"));
        Assert.assertEquals("HARDWARE_INFO STOP", content.get(15));
    }
}
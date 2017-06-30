package com.telenor.possumlib.utiltests;

import android.content.Context;

import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import com.telenor.possumlib.FileManipulator;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.utils.FileUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.util.Scheduler;
import org.robolectric.util.Transcript;
import org.robolectric.util.concurrent.RoboExecutorService;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PossumTestRunner.class)
public class FileUtilTest {
    private File dataDir;
    private final Transcript transcript = new Transcript();
    private RoboExecutorService roboExecutorService = new RoboExecutorService();
    private final Scheduler backgroundScheduler = ShadowApplication.getInstance().getBackgroundThreadScheduler();
    @Before
    public void setUp() throws Exception {
        backgroundScheduler.pause();
        dataDir = FileManipulator.getDataDir(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() throws Exception {
        FileUtil.clearDirectory(RuntimeEnvironment.application, dataDir);
    }

    @Test
    public void testDeleteFile() throws Exception {
        File mockedFile = mock(File.class);
        when(mockedFile.exists()).thenReturn(true);
        when(mockedFile.delete()).thenReturn(true);
        FileUtil.deleteFile(mockedFile);
        //noinspection ResultOfMethodCallIgnored
        Mockito.verify(mockedFile).delete();
    }

    @Test
    public void testLogMessageOnFailToDelete() throws Exception {
        File mockedFile = mock(File.class);
        when(mockedFile.delete()).thenReturn(false);
        when(mockedFile.exists()).thenReturn(true);
        FileUtil.deleteFile(mockedFile);
        Assert.assertTrue(ShadowLog.getLogs().size() == 1);
    }

    @Test
    public void testSizeOutputForLong() throws Exception {
        Assert.assertEquals("500 B", FileUtil.getSizeFromLong(500));
        Assert.assertEquals("1.5 KB", Strings.nullToEmpty(FileUtil.getSizeFromLong(1500)).replace(",", "."));
        Assert.assertTrue(Strings.nullToEmpty(FileUtil.getSizeFromLong(1500000)).replace(",", ".").equals("1.4 MB") || Strings.nullToEmpty(FileUtil.getSizeFromLong(1500000)).replace(",", ".").equals("1.5 MB"));// Rounding error?
        Assert.assertTrue(Strings.nullToEmpty(FileUtil.getSizeFromLong(1500000000L)).replace(",", ".").equals("1.4 GB") || Strings.nullToEmpty(FileUtil.getSizeFromLong(1500000000L)).replace(",", ".").equals("1.5 GB"));// Rounding error?
        Assert.assertTrue(Strings.nullToEmpty(FileUtil.getSizeFromLong(1500000000000L)).replace(",", ".").equals("1.4 TB") || Strings.nullToEmpty(FileUtil.getSizeFromLong(1500000000000L)).replace(",", ".").equals("1.5 TB"));// Rounding error?
    }

    @Test
    public void testSizeOutputForFile() throws Exception {
        File mockedSmallFile = mock(File.class);
        when(mockedSmallFile.length()).thenReturn(500L);
        when(mockedSmallFile.exists()).thenReturn(true);
        Assert.assertEquals("500 B", FileUtil.getSizeString(mockedSmallFile));
        File mockedMediumFile = mock(File.class);
        when(mockedMediumFile.length()).thenReturn(1500L);
        when(mockedMediumFile.exists()).thenReturn(true);
        Assert.assertEquals("1.5 KB", FileUtil.getSizeString(mockedMediumFile).replace(",", "."));
        File mockedLargeFile = mock(File.class);
        when(mockedLargeFile.length()).thenReturn(1500000L);
        when(mockedLargeFile.exists()).thenReturn(true);
        Assert.assertEquals("1.4 MB", FileUtil.getSizeString(mockedLargeFile).replace(",", ".")); // Rounding error?
        File mockedHugeFile = mock(File.class);
        when(mockedHugeFile.length()).thenReturn(1500000000000L);
        when(mockedHugeFile.exists()).thenReturn(true);
        Assert.assertEquals("1.4 TB", FileUtil.getSizeString(mockedHugeFile).replace(",", ".")); // Rounding error?
    }

    @Test
    public void testSizeOutputReturnsWhenInvalidInput() throws Exception {
        File mockedFile = mock(File.class);
        when(mockedFile.exists()).thenReturn(false);

        Assert.assertNull(FileUtil.getSizeString(mockedFile));
        Assert.assertNull(FileUtil.getSizeString(null));
        Assert.assertEquals("0kB", FileUtil.getSizeFromLong(0));
    }

    @Test
    public void testStoreLines() throws Exception {
        List<String> actualData = new ArrayList<>();
        Queue<String> testData = new ConcurrentLinkedQueue<>();
        actualData.add("test1");
        actualData.add("test2");
        actualData.add("test3");
        actualData.add("test4");
        testData.addAll(actualData);
        File fakeFile = new File(RuntimeEnvironment.application.getFilesDir().getAbsolutePath()+"/mockedFile");
        Assert.assertFalse(fakeFile.exists());
        Assert.assertTrue(fakeFile.createNewFile());
        Assert.assertTrue(fakeFile.length() == 0);
//        FileUtil.storeLines(fakeFile, testData);
        Assert.assertTrue(fakeFile.length() > 0);
        List<String> fileContent = CharStreams.readLines(new FileReader(fakeFile));
        Assert.assertEquals(actualData, fileContent);
        Assert.assertTrue(fakeFile.delete());
    }

    @Test
    public void testMultipleThreadsAppendingToFile() throws Exception {
        final Queue<String> longArray = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < 5000; i++) {
            longArray.add("test");
        }
        final Queue<String> longArray2 = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < 5000; i++) {
            longArray2.add("test2");
        }
        final File fakeFile = new File(RuntimeEnvironment.application.getFilesDir().getAbsolutePath()+"/fakedConcurrencyFile");
        if (fakeFile.exists()) {
            Assert.assertTrue("Unable to delete fake file", fakeFile.delete());
        }
        Assert.assertTrue("Unable to create fake file", fakeFile.createNewFile());

        Future<String> future1 = roboExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                transcript.add("background event ran: longArray");
                try {
//                    FileUtil.storeLines(fakeFile, longArray);
//                    Assert.fail("Should not have failed here");
                } catch (ConcurrentModificationException e) {
                    Assert.fail("Got a damned concurrenyFail:"+e.getMessage());
                }

            }
        }, "bg1");
        Future<String> future2 = roboExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    transcript.add("background event ran: longArray2");
//                    FileUtil.storeLines(fakeFile, longArray2);
//                    Assert.fail("Should not have failed here");
                } catch (ConcurrentModificationException e) {
                    Assert.fail("Got a damned concurrencyFail:"+e.getMessage());
                }
            }
        }, "bg2");
        transcript.assertNoEventsSoFar();
        Assert.assertFalse(future1.isDone());
        Assert.assertFalse(future2.isDone());
        ShadowApplication.runBackgroundTasks();
        transcript.assertEventsSoFar("background event ran: longArray", "background event ran: longArray2");
        Assert.assertTrue(future1.isDone());
        Assert.assertTrue(future2.isDone());
        Assert.assertEquals("bg1", future1.get());
        Assert.assertEquals("bg2", future2.get());
    }

    @Test
    public void testHasDataWhenHavingData() throws Exception {
        AbstractDetector mockedDetector = mock(AbstractDetector.class);
        File mockedFile = mock(File.class);
        when(mockedFile.length()).thenReturn(100L);
        when(mockedDetector.storedData()).thenReturn(mockedFile);
    }

    @Test
    public void testHasDataWhenHavingNoData() throws Exception {
        AbstractDetector mockedDetector = mock(AbstractDetector.class);
        File mockedFile = mock(File.class);
        when(mockedFile.length()).thenReturn(0L);
        when(mockedDetector.storedData()).thenReturn(mockedFile);
//        AwesomePossum.addAllDetectors(RuntimeEnvironment.application);
    }

    @Test
    public void testZipFile() throws Exception {
        File fakeFile = FileManipulator.getFileWithName(RuntimeEnvironment.application, "Accelerometer");
        FileManipulator.fillFile(fakeFile);
        int fileLengthBeforeZip = 160;
        Assert.assertEquals(fileLengthBeforeZip, fakeFile.length());
        File fakeZipFile = new File(RuntimeEnvironment.application.getFilesDir().getAbsolutePath()+"/fakeFileZipped.zip");
        Assert.assertTrue(fakeZipFile.length() == 0);
        fakeZipFile = FileUtil.zipFile(fakeFile, fakeZipFile);
        Assert.assertTrue(fakeZipFile.length() > 0);
        Assert.assertTrue(fakeZipFile.length() < fileLengthBeforeZip);
        Assert.assertTrue(fakeFile.delete());
    }

    @Test
    public void testGetFileWhichIsAlreadyThere() throws Exception {
        File fakeFile = FileManipulator.getFileWithName(RuntimeEnvironment.application, "Accelerometer");
        FileManipulator.fillFile(fakeFile);
        Context context = mock(Context.class);
        when(context.getString(Mockito.anyInt())).thenReturn("Accelerometer");
        when(context.getFilesDir()).thenReturn(RuntimeEnvironment.application.getFilesDir());
        File retrievedFile = FileUtil.getFile(context, "Accelerometer");
        Assert.assertEquals(fakeFile.length(), retrievedFile.length());
    }

    @Test
    public void testGetFileWhichIsNotThere() throws Exception {
        FileUtil.clearDirectory(RuntimeEnvironment.application, FileManipulator.getDataDir(RuntimeEnvironment.application));
        Context context = mock(Context.class);
        when(context.getString(Mockito.anyInt())).thenReturn("Gyroscope");
        when(context.getFilesDir()).thenReturn(RuntimeEnvironment.application.getFilesDir());

        File fakeFile = FileUtil.getFile(context, "Gyroscope");
        Assert.assertEquals("Gyroscope", fakeFile.getName());
        Assert.assertEquals(0, fakeFile.length());
    }

    @Test
    public void testInvalidFilesystemGetFile() throws Exception {
        File mockedFile = mock(File.class);
        when(mockedFile.exists()).thenReturn(false);
        when(mockedFile.getName()).thenReturn("fakeFileFtw");
        when(mockedFile.createNewFile()).thenReturn(false);
        try {
            FileUtil.createDataFile(mockedFile);
            Assert.fail("Should not have worked");
        } catch (Exception e) {
            Assert.assertEquals("Unable to create file:fakeFileFtw", e.getMessage());
        }
    }

    @Test
    public void testInvalidIOErrorOnGetFile() throws Exception {
        File mockedFile = mock(File.class);
        when(mockedFile.exists()).thenReturn(false);
        when(mockedFile.getName()).thenReturn("fakeFileFtw");
        when(mockedFile.createNewFile()).thenThrow(new IOException("FailFtw"));
        try {
            FileUtil.createDataFile(mockedFile);
            Assert.fail("Should not have worked");
        } catch (Exception e) {
            Assert.assertEquals("FailFtw", e.getMessage());
        }
    }
}
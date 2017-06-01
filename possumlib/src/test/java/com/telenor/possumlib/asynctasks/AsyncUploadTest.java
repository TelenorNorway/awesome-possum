package com.telenor.possumlib.asynctasks;

import android.content.Context;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.telenor.possumlib.FileManipulator;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.interfaces.IWrite;
import com.telenor.possumlib.utils.FileUtil;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PossumTestRunner.class)
public class AsyncUploadTest {
    private Context mockedContext;

    private AsyncUpload asyncUpload;
    private IWrite mockedListener;
    private TransferUtility mockedTransferUtility;
    private File dataDir;
    private Map<File, TransferListener> transferListeners;

    @Before
    public void setUp() throws Exception {
        mockedContext = mock(Context.class);
        mockedListener = mock(IWrite.class);

        mockedTransferUtility = mock(TransferUtility.class);
        when(mockedTransferUtility.upload(anyString(), anyString(), any(File.class))).thenReturn(mock(TransferObserver.class));

        dataDir = FileManipulator.getDataDir(RuntimeEnvironment.application);
        FileUtil.clearDirectory(RuntimeEnvironment.application, dataDir);
        transferListeners = new HashMap<>();

//        when(mockedContext.getString(R.string.upload_totalled)).thenReturn("Total : %s");
        when(mockedContext.getFilesDir()).thenReturn(RuntimeEnvironment.application.getFilesDir());
//        when(mockedContext.getString(R.string.upload_totalled)).thenReturn("Total : %s");

//        AwesomePossum.detectors().clear();
//
//        fakeFile1 = FileUtil.toUploadFile(mockedContext, KEY1);
//        mockedDetector1 = mock(AbstractDetector.class);
//        AwesomePossum.detectors().add(mockedDetector1);
//        Mockito.doAnswer(new Answer<Void>() {
//            @Override
//            public Void answer(InvocationOnMock invocation) throws Throwable {
//                FileManipulator.fillFile(fakeFile1);
//                return null;
//            }
//        }).when(mockedDetector1).prepareUpload();
//
//        fakeFile2 = FileUtil.toUploadFile(mockedContext, KEY2);
//        mockedDetector2 = mock(AbstractDetector.class);
//        AwesomePossum.detectors().add(mockedDetector2);
//        Mockito.doAnswer(new Answer<Void>() {
//            @Override
//            public Void answer(InvocationOnMock invocation) throws Throwable {
//                FileManipulator.fillFile(fakeFile2);
//                return null;
//            }
//        }).when(mockedDetector2).prepareUpload();
//
//        asyncUpload = new InstrumentedConnection();
        Robolectric.flushBackgroundThreadScheduler();
    }

    @After
    public void tearDown() throws Exception {
        asyncUpload = null;
        FileUtil.clearDirectory(RuntimeEnvironment.application, dataDir);
        Thread.sleep(100); // Fix for shadowApplication being dead on test, causing shadowAsync do give nullPointer
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertTrue(true);
    }

//    @Test
//    public void testInvalidInit() throws Exception {
//        try {
//            asyncUpload = new UploadConnection(null, mockedListener, mockedTransferUtility, null);
//            Assert.fail("Should not accept missing context");
//        } catch (Exception e) {
//            Assert.assertEquals("Missing context on asyncUpload", e.getMessage());
//        }
//    }

//    @Test
//    public void testUploadProcessSuccessAndProgress() throws Exception {
//        execute();
//        Robolectric.flushBackgroundThreadScheduler();
//
//        verify(mockedListener, times(4)).bytesWritten(anyInt());
//
//        Robolectric.flushBackgroundThreadScheduler();
//
//        verify(mockedListener).uploadComplete(isNull(Exception.class), isNotNull(String.class));
//    }

//    @Test
//    public void testDefaults() throws Exception {
//    }

//    @Test
//    public void testOnlyPartiallyUploaded() throws Exception {
//        execute();
//        Robolectric.flushBackgroundThreadScheduler();
//        verify(mockedListener).uploadComplete(isNotNull(Exception.class), isNull(String.class));
//    }

//    private void execute() {
//        asyncUpload.execute((Void) null);
//        ArgumentCaptor<Runnable> continuationCaptor = ArgumentCaptor.forClass(Runnable.class);
//        continuationCaptor.getValue().run();
//    }
}

package com.telenor.possumlib.utiltests;


import android.content.Context;
import android.content.res.Resources;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import com.telenor.possumlib.PossumTestRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PossumTestRunner.class)
public class TimeUtilTest {
    private File fakeJodaFile;

    @Before
    public void setUp() throws Exception {
        Context mockedContext = mock(Context.class);
        Resources mockedResources = mock(Resources.class);
        when(mockedContext.getResources()).thenReturn(mockedResources);
        when(mockedContext.getApplicationContext()).thenReturn(mockedContext);
        fakeJodaFile = new File(RuntimeEnvironment.application.getFilesDir().getAbsolutePath()+"jodaFile");
        if (!fakeJodaFile.exists()) {
            createFakeJodaFile();
        }
        when(mockedResources.openRawResource(Mockito.anyInt())).thenReturn(new FileInputStream(fakeJodaFile));
//        JodaTimeAndroid.init(mockedContext);
    }

    private void createFakeJodaFile() throws Exception {
        Assert.assertTrue(fakeJodaFile.createNewFile());
        FileWriter fileWriter = new FileWriter(fakeJodaFile);
        fileWriter.append("test\r\n");
        fileWriter.close();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testBeforeToday() throws Exception {
//        long timestampToday = DateTime.now().getMillis();
//        Assert.assertFalse(TimeUtil.isBeforeToday(timestampToday));
//        long timestampYesterday = timestampToday - 3600*24;
//        Assert.assertTrue(TimeUtil.isBeforeToday(timestampYesterday));
//        long timestampTomorrow = timestampToday + 3600*24;
//        Assert.assertFalse(TimeUtil.isBeforeToday(timestampTomorrow));
    }
}
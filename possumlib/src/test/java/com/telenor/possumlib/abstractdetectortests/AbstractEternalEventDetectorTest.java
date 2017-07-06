package com.telenor.possumlib.abstractdetectortests;

import android.content.Context;

import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.abstractdetectors.AbstractEternalEventDetector;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.models.PossumBus;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;

@RunWith(PossumTestRunner.class)
public class AbstractEternalEventDetectorTest {
    private AbstractEternalEventDetector eternalEventDetector;
    @Mock
    private Context mockedContext;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        eternalEventDetector = new AbstractEternalEventDetector(mockedContext, "fakeUnique", new PossumBus(), false) {
            @Override
            public String requiredPermission() {
                return null;
            }

            @Override
            public int detectorType() {
                return DetectorType.MetaData;
            }

            @Override
            public String detectorName() {
                return "MetaData";
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        eternalEventDetector = null;
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertNotNull(eternalEventDetector);
        Assert.assertTrue(eternalEventDetector.isListening());
    }

    @Test
    public void testDefaults() throws Exception {
        Assert.assertTrue(eternalEventDetector.isAvailable());
        Assert.assertTrue(eternalEventDetector.isEnabled());
        Method intervalField = AbstractEternalEventDetector.class.getDeclaredMethod("storeWithInterval");
        intervalField.setAccessible(true);
        Assert.assertFalse((boolean)intervalField.invoke(eternalEventDetector));
        Assert.assertTrue(eternalEventDetector.startListening());
    }
    @Test
    public void testStopListeningDoesNothing() throws Exception {
        Assert.assertTrue(eternalEventDetector.isListening());
        eternalEventDetector.stopListening();
        Assert.assertTrue(eternalEventDetector.isListening());
    }

    @Test
    public void testTerminateStopsListening() throws Exception {
        eternalEventDetector.terminate();
        Assert.assertFalse(eternalEventDetector.isListening());
    }
}
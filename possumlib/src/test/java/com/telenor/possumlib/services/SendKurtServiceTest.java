package com.telenor.possumlib.services;

import com.telenor.possumlib.JodaInit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ServiceController;

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class SendKurtServiceTest {
    private ServiceController<SendKurtService> serviceController;

    @Before
    public void setUp() throws Exception {
        JodaInit.initializeJodaTime();
        serviceController = Robolectric.buildService(SendKurtService.class).attach();
    }

    @After
    public void tearDown() throws Exception {
        serviceController.destroy();
    }

    @Test
    public void testInit() throws Exception {
        Assert.assertNotNull(serviceController.get());
    }
}
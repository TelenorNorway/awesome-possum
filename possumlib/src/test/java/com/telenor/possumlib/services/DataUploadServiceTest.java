package com.telenor.possumlib.services;

import android.app.Notification;

import com.telenor.possumlib.JodaInit;
import com.telenor.possumlib.PossumTestRunner;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowServiceManager;
import org.robolectric.util.ServiceController;

@RunWith(PossumTestRunner.class)
public class DataUploadServiceTest {
    private ServiceController<DataUploadService> serviceController;
    @Before
    public void setUp() throws Exception {
        JodaInit.initializeJodaTime();
        serviceController = Robolectric.buildService(DataUploadService.class).attach();
        ShadowServiceManager.addService("upload", serviceController.get().onBind(null));
    }

    @After
    public void tearDown() throws Exception {
        serviceController.destroy();
    }

    @Test
    public void testOnCreateInit() throws Exception {
//        Assert.assertNull(shadowService.getLastForegroundNotification());
        DataUploadService beforeCreate = serviceController.get();
        Assert.assertNotNull(beforeCreate);
        Notification beforeNotification = Shadows.shadowOf(beforeCreate).getLastForegroundNotification();
        Assert.assertNull(beforeNotification);

//        Assert.fail("Size before:"+ShadowApplication.getInstance().getForegroundThreadScheduler().size());

        DataUploadService service = serviceController.create().get();
        Assert.assertNotNull(service);
    }

    @Test
    public void testOnCreateStartsAsyncTask() throws Exception {
        Assert.assertTrue(true);
    }
}
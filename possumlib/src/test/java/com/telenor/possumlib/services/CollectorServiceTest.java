package com.telenor.possumlib.services;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;

import com.telenor.possumlib.AwesomePossum;
import com.telenor.possumlib.FileManipulator;
import com.telenor.possumlib.JodaInit;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.utils.FileUtil;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowService;
import org.robolectric.util.ServiceController;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PossumTestRunner.class)
public class CollectorServiceTest {
    private ServiceController<CollectorService> serviceController;
    @Mock
    private AbstractDetector mockedDetector1;
    @Mock
    private AbstractDetector mockedDetector2;
    @Mock
    private Context mockedContext;

    private File fakeFile1;
    private File fakeFile2;

    @Before
    public void setUp() throws Exception {
        // Enforce clear of EventBus static instance
        MockitoAnnotations.initMocks(this);
        JodaInit.initializeJodaTime();
        Context context = RuntimeEnvironment.application;
        PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        packageInfo.versionName = "test";

        // TODO: Ensure tests are not run in parallel, many static methods that would cause havoc
//        Field eventSubField = EventBus.class.getDeclaredField("subscribersPerTopic");
//        eventSubField.setAccessible(true);
//        @SuppressWarnings("unchecked") HashMap<String, Set<EventSubscriber>> subMap = (HashMap<String, Set<EventSubscriber>>) eventSubField.get(EventBus.getInstance());
//        subMap.clear();
        when(mockedContext.getSystemService(Context.LOCATION_SERVICE)).thenReturn(context.getSystemService(Context.LOCATION_SERVICE));
        when(mockedContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(context.getSystemService(Context.CONNECTIVITY_SERVICE));
        when(mockedContext.getApplicationContext()).thenReturn(mockedContext);
        when(mockedContext.getFilesDir()).thenReturn(context.getFilesDir());
        File dataDir = FileManipulator.getDataDir(RuntimeEnvironment.application);
        FileUtil.clearDirectory(RuntimeEnvironment.application, dataDir);
        fakeFile1 = new File(dataDir.getAbsolutePath() + "/Accelerometer");
        FileManipulator.fillFile(fakeFile1);
        fakeFile2 = new File(dataDir.getAbsolutePath() + "/Gyroscope");
        FileManipulator.fillFile(fakeFile2);
        when(mockedDetector1.storedData()).thenReturn(fakeFile1);
        when(mockedDetector2.storedData()).thenReturn(fakeFile2);
        when(mockedContext.registerReceiver(Matchers.isNull(BroadcastReceiver.class),any(IntentFilter.class))).thenReturn(null);
//        AwesomePossum.init(mockedContext, 1, 1, 1, "test", DummyPossumActivity.class, Fragment.class);
//        AwesomePossum.detectors().clear();
//        AwesomePossum.detectors().add(mockedDetector1);
//        AwesomePossum.detectors().add(mockedDetector2);
//        Assert.assertEquals(2, AwesomePossum.detectors().size());
        serviceController = Robolectric.buildService(CollectorService.class);
    }

    @After
    public void tearDown() throws Exception {
        serviceController.destroy();
    }

    @Test
    public void testOnCreateInitializesProperly() throws Exception {
        CollectorService sensorService = serviceController.attach().create().get();
        Field notificationField = CollectorService.class.getDeclaredField("notificationManager");
        notificationField.setAccessible(true);
        NotificationManager notificationManager = (NotificationManager) notificationField.get(sensorService);
        Assert.assertNotNull(notificationManager);
    }

    @Test
    public void testStartsForegroundService() {
        ShadowService shadowService = new ShadowService();
        Assert.assertNull(shadowService.getLastForegroundNotification());
        serviceController.attach().create();
        // TODO: Make work!!
//        Assert.assertNotNull(shadowService.getLastForegroundNotification());

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubscriptionOnCreateAndDestroy() throws Exception {
//        Field subField = EventBus.class.getDeclaredField("subscribersPerTopic");
//        subField.setAccessible(true);
//        serviceController.attach().create();
//        CollectorService sensorService = serviceController.get();
//        Assert.assertTrue(((HashMap<String, Set<EventSubscriber>>) subField.get(EventBus.getInstance())).get(Constants.IS_GATHERING).contains(sensorService));
//        serviceController.destroy();
//        Assert.assertFalse(((HashMap<String, Set<EventSubscriber>>) subField.get(EventBus.getInstance())).get(Constants.IS_GATHERING).contains(sensorService));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOnDestroyUnregistersFromAll() throws Exception {
//        serviceController.attach().create().startCommand(0, 0);
//        CollectorService service = serviceController.get();
//        Field subscribers = EventBus.class.getDeclaredField("subscribersPerTopic");
//        subscribers.setAccessible(true);
//        HashMap<String, Set<EventSubscriber>> subscriberMap = (HashMap<String, Set<EventSubscriber>>)subscribers.get(EventBus.getInstance());
//        Assert.assertTrue(subscriberMap.get(Constants.UPLOAD_EVENT).contains(service));
//        Assert.assertTrue(subscriberMap.get(Constants.IS_GATHERING).contains(service));
//        serviceController.destroy();
//
//        HashMap<String, Set<EventSubscriber>> subscriberMapAfterDestroy = (HashMap<String, Set<EventSubscriber>>)subscribers.get(EventBus.getInstance());
//        Assert.assertFalse(subscriberMapAfterDestroy.get(Constants.UPLOAD_EVENT).contains(service));
//        Assert.assertFalse(subscriberMapAfterDestroy.get(Constants.IS_GATHERING).contains(service));
    }

    @Test
    public void testDontDeleteAllExistingFilesOnCreateIfHasData() throws Exception {
        Assert.assertTrue(fakeFile1.exists());
        Assert.assertTrue(fakeFile2.exists());
        // Run onCreate of service
        serviceController.attach().create();
        // Files deleted
        Assert.assertTrue(fakeFile1.exists());
        Assert.assertTrue(fakeFile2.exists());
    }

    @Test
    public void testIgnoresDataIfNotThereOnCreate() throws Exception {
        Assert.assertTrue(fakeFile1.delete());
        Assert.assertTrue(fakeFile2.delete());
        serviceController.attach().create();
        Assert.assertFalse(fakeFile1.exists());
        Assert.assertFalse(fakeFile2.exists());
    }

    @Test
    public void testStartListening() throws Exception {
        CollectorService sensorService = serviceController.attach().create().get();
        Method startMethod = CollectorService.class.getDeclaredMethod("startListen");
        startMethod.setAccessible(true);
        startMethod.invoke(sensorService);
        verify(mockedDetector1).startListening();
        verify(mockedDetector2).startListening();
    }

    @Test
    public void testStopListening() throws Exception {
        CollectorService sensorService = serviceController.attach().create().get();
        Method startMethod = CollectorService.class.getDeclaredMethod("startListen");
        startMethod.setAccessible(true);
        startMethod.invoke(sensorService);

        Method stopMethod = CollectorService.class.getDeclaredMethod("stopListen");
        stopMethod.setAccessible(true);
        // TODO: Implement a way for stop not terminating the runnables twice (once in the foreach, and once in DetectorTimer.clearAll()
        stopMethod.invoke(sensorService);
        verify(mockedDetector1).stopListening();
        verify(mockedDetector2).stopListening();
    }

    @Test
    public void testStartServiceOnOnTaskRemoved() throws Exception {
        CollectorService sensorService = serviceController.attach().create().get();
        ShadowService shadowService = new ShadowService();
        Assert.assertNull(shadowService.peekNextStartedService());
        sensorService.onTaskRemoved(null);
//        Intent intent = shadowService.getNextStartedService();
//        Assert.assertEquals(CollectorService.class.getName(), intent.getComponent().getClassName());
    }

    @Test
    public void testClickOnResumeNotificationRestartsListening() throws Exception {
//        CollectorService service = serviceController.attach().create().get();

    }

    @Test
    public void testNoGodDamnAidlBindingStuff() throws Exception {
        CollectorService sensorService = serviceController.attach().create().get();
        Assert.assertNull(sensorService.onBind(null));
    }

    @Test
    public void testStartupCommandWhenRequestingRestart() throws Exception {
        AwesomePossum.authorizeGathering();
//        AwesomePossum.setGathering(RuntimeEnvironment.application, false);
//        Assert.assertFalse(AwesomePossum.isGathering());
//        serviceController.attach().create().startCommand(0, ReqCodes.BACKGROUND_SERVICE);
//        Assert.assertTrue(AwesomePossum.isGathering());
    }

    @Test
    public void testFillerMethodsForCoverage() throws Exception {
//        CollectorService service = serviceController.attach().create().get();
//        ShadowLog.setupLogging();
//        Assert.assertEquals(0, ShadowLog.getLogs().size());
//        service.listChanged();
//        Assert.assertEquals(1, ShadowLog.getLogs().size());
    }
}
package com.telenor.possumlib.services;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;

import com.telenor.possumlib.JodaInit;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowSensorManager;
import org.robolectric.util.ServiceController;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentLinkedQueue;

@RunWith(PossumTestRunner.class)
public class CollectorServiceTest {
    private ServiceController<CollectorService> serviceController;
    @Mock
    private AbstractDetector mockedDetector1;
    @Mock
    private AbstractDetector mockedDetector2;
    @Mock
    private Context mockedContext;

    @Before
    public void setUp() throws Exception {
        // Enforce clear of EventBus static instance
        MockitoAnnotations.initMocks(this);
        JodaInit.initializeJodaTime();
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION);
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.CAMERA);
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.RECORD_AUDIO);
        serviceController = Robolectric.buildService(CollectorService.class);
    }

    @After
    public void tearDown() throws Exception {
        serviceController.destroy();
    }

    @Test
    public void testOnCreateInitialization() throws Exception {
        Field receiverField = CollectorService.class.getDeclaredField("receiver");
        receiverField.setAccessible(true);
        Assert.assertNull(receiverField.get(serviceController.get()));
        CollectorService service = serviceController.create().get();
        Assert.assertNotNull(receiverField.get(service));
    }

    @Test
    public void testStartWithAllButImageDetectors() throws Exception {
        Intent intent = new Intent();
        intent.putExtra("encryptedKurt", "fakeKurt");
        intent.putExtra("secretHash", "fakeSecret");
        intent.putExtra("refusedDetectors", "Image"); // Not adding image detector since test assets are missing the model file
        Field detectorsField = CollectorService.class.getDeclaredField("detectors");
        detectorsField.setAccessible(true);
        ConcurrentLinkedQueue detectors = (ConcurrentLinkedQueue)detectorsField.get(serviceController.create().get());
        Assert.assertTrue(detectors.isEmpty());
        ShadowSensorManager shadowSensorManager = new ShadowSensorManager();
        shadowSensorManager.addSensor(Sensor.TYPE_ACCELEROMETER, shadowSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        CollectorService service = serviceController.create().withIntent(intent).startCommand(0, 0).get();
        detectors = (ConcurrentLinkedQueue)detectorsField.get(service);
        Assert.assertEquals(10, detectors.size());

        // confirm some of the detectors are started
        int startedDetectors = 0;
        for (Object obj : detectors) {
            AbstractDetector detector = (AbstractDetector)obj;
//            if (detector.detectorType() == DetectorType.Accelerometer) {
//                Accelerometer accelerometer = (Accelerometer)detector;
//                Assert.assertTrue(accelerometer.isPermitted());
//                Field sensorField = AbstractAndroidDetector.class.getDeclaredField("sensorManager");
//                sensorField.setAccessible(true);
//                SensorManager sensorManager = (SensorManager)sensorField.get(accelerometer);
//                Assert.assertNotNull(sensorManager);
//                List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
//                Assert.assertTrue(sensorList.size() > 0);
//                Assert.assertTrue(accelerometer.isAvailable());
//                Assert.assertTrue(accelerometer.isEnabled());
//                Assert.assertTrue(accelerometer.isValidSet());
//                Assert.assertFalse(accelerometer.isListening());
//            }
            if (detector.isListening()) startedDetectors++;
        }
        Assert.assertEquals(8, startedDetectors);
        // Accelerometer and GyroScope not started due to missing SensorManager

        Method clearDetectorsMethod = CollectorService.class.getDeclaredMethod("clearAllDetectors");
        clearDetectorsMethod.setAccessible(true);
        clearDetectorsMethod.invoke(service);
    }

//    @Test
//    public void testStartServiceOnOnTaskRemoved() throws Exception {
//        CollectorService sensorService = serviceController.attach().create().get();
//        ShadowService shadowService = new ShadowService();
//        Assert.assertNull(shadowService.peekNextStartedService());
//        sensorService.onTaskRemoved(null);
//        Intent intent = shadowService.getNextStartedService();
//        Assert.assertEquals(CollectorService.class.getName(), intent.getComponent().getClassName());
//    }

//    @Test
//    public void testOnCreateInitializesProperly() throws Exception {
//        CollectorService sensorService = serviceController.attach().create().get();
//        Field notificationField = CollectorService.class.getDeclaredField("notificationManager");
//        notificationField.setAccessible(true);
//        NotificationManager notificationManager = (NotificationManager) notificationField.get(sensorService);
//        Assert.assertNotNull(notificationManager);
//    }
//
//    @Test
//    public void testStartsForegroundService() {
//        ShadowService shadowService = new ShadowService();
//        Assert.assertNull(shadowService.getLastForegroundNotification());
//        serviceController.attach().create();
//        // TODO: Make work!!
////        Assert.assertNotNull(shadowService.getLastForegroundNotification());
//
//    }
//
//    @SuppressWarnings("unchecked")
//    @Test
//    public void testSubscriptionOnCreateAndDestroy() throws Exception {
////        Field subField = EventBus.class.getDeclaredField("subscribersPerTopic");
////        subField.setAccessible(true);
////        serviceController.attach().create();
////        CollectorService sensorService = serviceController.get();
////        Assert.assertTrue(((HashMap<String, Set<EventSubscriber>>) subField.get(EventBus.getInstance())).get(Constants.IS_GATHERING).contains(sensorService));
////        serviceController.destroy();
////        Assert.assertFalse(((HashMap<String, Set<EventSubscriber>>) subField.get(EventBus.getInstance())).get(Constants.IS_GATHERING).contains(sensorService));
//    }
//
//    @SuppressWarnings("unchecked")
//    @Test
//    public void testOnDestroyUnregistersFromAll() throws Exception {
////        serviceController.attach().create().startCommand(0, 0);
////        CollectorService service = serviceController.get();
////        Field subscribers = EventBus.class.getDeclaredField("subscribersPerTopic");
////        subscribers.setAccessible(true);
////        HashMap<String, Set<EventSubscriber>> subscriberMap = (HashMap<String, Set<EventSubscriber>>)subscribers.get(EventBus.getInstance());
////        Assert.assertTrue(subscriberMap.get(Constants.UPLOAD_EVENT).contains(service));
////        Assert.assertTrue(subscriberMap.get(Constants.IS_GATHERING).contains(service));
////        serviceController.destroy();
////
////        HashMap<String, Set<EventSubscriber>> subscriberMapAfterDestroy = (HashMap<String, Set<EventSubscriber>>)subscribers.get(EventBus.getInstance());
////        Assert.assertFalse(subscriberMapAfterDestroy.get(Constants.UPLOAD_EVENT).contains(service));
////        Assert.assertFalse(subscriberMapAfterDestroy.get(Constants.IS_GATHERING).contains(service));
//    }
//
//    @Test
//    public void testDontDeleteAllExistingFilesOnCreateIfHasData() throws Exception {
//        Assert.assertTrue(fakeFile1.exists());
//        Assert.assertTrue(fakeFile2.exists());
//        // Run onCreate of service
//        serviceController.attach().create();
//        // Files deleted
//        Assert.assertTrue(fakeFile1.exists());
//        Assert.assertTrue(fakeFile2.exists());
//    }
//
//    @Test
//    public void testIgnoresDataIfNotThereOnCreate() throws Exception {
//        Assert.assertTrue(fakeFile1.delete());
//        Assert.assertTrue(fakeFile2.delete());
//        serviceController.attach().create();
//        Assert.assertFalse(fakeFile1.exists());
//        Assert.assertFalse(fakeFile2.exists());
//    }
//
//    @Test
//    public void testStartListening() throws Exception {
//        CollectorService sensorService = serviceController.attach().create().get();
//        Method startMethod = CollectorService.class.getDeclaredMethod("startListen");
//        startMethod.setAccessible(true);
//        startMethod.invoke(sensorService);
//        verify(mockedDetector1).startListening();
//        verify(mockedDetector2).startListening();
//    }
//
//    @Test
//    public void testStopListening() throws Exception {
//        CollectorService sensorService = serviceController.attach().create().get();
//        Method startMethod = CollectorService.class.getDeclaredMethod("startListen");
//        startMethod.setAccessible(true);
//        startMethod.invoke(sensorService);
//
//        Method stopMethod = CollectorService.class.getDeclaredMethod("stopListen");
//        stopMethod.setAccessible(true);
//        // TODO: Implement a way for stop not terminating the runnables twice (once in the foreach, and once in DetectorTimer.clearAll()
//        stopMethod.invoke(sensorService);
//        verify(mockedDetector1).stopListening();
//        verify(mockedDetector2).stopListening();
//    }
//
//
//    @Test
//    public void testClickOnResumeNotificationRestartsListening() throws Exception {
////        CollectorService service = serviceController.attach().create().get();
//
//    }
//
//    @Test
//    public void testNoGodDamnAidlBindingStuff() throws Exception {
//        CollectorService sensorService = serviceController.attach().create().get();
//        Assert.assertNull(sensorService.onBind(null));
//    }
//
//    @Test
//    public void testStartupCommandWhenRequestingRestart() throws Exception {
//        AwesomePossum.authorizeGathering(RuntimeEnvironment.application, "fakeEncryptedKurt");
////        AwesomePossum.setGathering(RuntimeEnvironment.application, false);
////        Assert.assertFalse(AwesomePossum.isGathering());
////        serviceController.attach().create().startCommand(0, ReqCodes.BACKGROUND_SERVICE);
////        Assert.assertTrue(AwesomePossum.isGathering());
//    }
//
//    @Test
//    public void testFillerMethodsForCoverage() throws Exception {
////        CollectorService service = serviceController.attach().create().get();
////        ShadowLog.setupLogging();
////        Assert.assertEquals(0, ShadowLog.getLogs().size());
////        service.listChanged();
////        Assert.assertEquals(1, ShadowLog.getLogs().size());
//    }
}
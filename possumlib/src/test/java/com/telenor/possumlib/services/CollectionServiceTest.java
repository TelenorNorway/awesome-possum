package com.telenor.possumlib.services;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.telenor.possumlib.JodaInit;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.constants.Messaging;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.ServiceController;

import java.lang.reflect.Field;

@RunWith(PossumTestRunner.class)
public class CollectionServiceTest {
    private ServiceController<CollectionService> serviceController;
    @Mock
    private AbstractDetector mockedDetector1;
    @Mock
    private AbstractDetector mockedDetector2;
    @Mock
    private Context mockedContext;

    private boolean receivedIntent;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        JodaInit.initializeJodaTime();
        receivedIntent = false;
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION);
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.CAMERA);
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.RECORD_AUDIO);
        Intent intent = new Intent();
        intent.putExtra("uniqueUserId", "testId");
        serviceController = Robolectric.buildService(CollectionService.class, intent);
    }

    @After
    public void tearDown() throws Exception {
        serviceController.destroy();
    }

    @Test
    public void testDefaults() throws Exception {
        Field receiverField = CollectionService.class.getDeclaredField("receiver");
        receiverField.setAccessible(true);
        Assert.assertNull(receiverField.get(serviceController.get()));
        CollectionService service = serviceController.create().get();
        Assert.assertNotNull(receiverField.get(service));
    }

    @Test
    public void testReceiveIntentAndStopServiceIfMissingKurtId() throws Exception {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                receivedIntent = true;
            }
        };
        RuntimeEnvironment.application.registerReceiver(receiver, new IntentFilter(Messaging.POSSUM_MESSAGE));
        serviceController = Robolectric.buildService(CollectionService.class);
        Assert.assertFalse(receivedIntent);
        CollectionService service = serviceController.create().startCommand(0, 0).get();
        // Confirm that it does not retrieve detectors to gather from since it is destroyed when missing user id
        Assert.assertTrue(receivedIntent);
        RuntimeEnvironment.application.unregisterReceiver(receiver);
    }

//    @Test
//    public void testStartWithAllButImageDetectors() throws Exception {
//        Intent intent = new Intent();
//        intent.putExtra("uniqueUserId", "fakeKurt");
//        intent.putExtra("secretHash", "fakeSecret");
//        Field detectorsField = CollectionService.class.getDeclaredField("detectors");
//        detectorsField.setAccessible(true);
//        ConcurrentLinkedQueue detectors = (ConcurrentLinkedQueue)detectorsField.get(serviceController.create().get());
//        Assert.assertTrue(detectors.isEmpty());
//        ShadowSensorManager shadowSensorManager = new ShadowSensorManager();
//        shadowSensorManager.addSensor(Sensor.TYPE_ACCELEROMETER, shadowSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
//        CollectionService service = serviceController.create().withIntent(intent).startCommand(0, 0).get();
//        detectors = (ConcurrentLinkedQueue)detectorsField.get(service);
//        Assert.assertEquals(11, detectors.size());
//
//        // confirm some of the detectors are started
//        int startedDetectors = 0;
//        for (Object obj : detectors) {
//            AbstractDetector detector = (AbstractDetector)obj;
////            if (detector.detectorType() == DetectorType.Accelerometer) {
////                Accelerometer accelerometer = (Accelerometer)detector;
////                Assert.assertTrue(accelerometer.isPermitted());
////                Field sensorField = AbstractAndroidDetector.class.getDeclaredField("sensorManager");
////                sensorField.setAccessible(true);
////                SensorManager sensorManager = (SensorManager)sensorField.get(accelerometer);
////                Assert.assertNotNull(sensorManager);
////                List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
////                Assert.assertTrue(sensorList.size() > 0);
////                Assert.assertTrue(accelerometer.isAvailable());
////                Assert.assertTrue(accelerometer.isEnabled());
////                Assert.assertFalse(accelerometer.isListening());
////            }
//            if (detector.isListening()) startedDetectors++;
//        }
//        Assert.assertEquals(8, startedDetectors);
//        // Accelerometer and GyroScope not started due to missing SensorManager
//
//        Method clearDetectorsMethod = CollectionService.class.getDeclaredMethod("clearAllDetectors");
//        clearDetectorsMethod.setAccessible(true);
//        clearDetectorsMethod.invoke(service);
//    }

//    @Test
//    public void testStartServiceOnOnTaskRemoved() throws Exception {
//        CollectionService sensorService = serviceController.attach().create().get();
//        ShadowService shadowService = new ShadowService();
//        Assert.assertNull(shadowService.peekNextStartedService());
//        sensorService.onTaskRemoved(null);
//        Intent intent = shadowService.getNextStartedService();
//        Assert.assertEquals(CollectionService.class.getName(), intent.getComponent().getClassName());
//    }

//    @Test
//    public void testOnCreateInitializesProperly() throws Exception {
//        CollectionService sensorService = serviceController.attach().create().get();
//        Field notificationField = CollectionService.class.getDeclaredField("notificationManager");
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
////        CollectionService sensorService = serviceController.get();
////        Assert.assertTrue(((HashMap<String, Set<EventSubscriber>>) subField.get(EventBus.getInstance())).get(Constants.IS_GATHERING).contains(sensorService));
////        serviceController.destroy();
////        Assert.assertFalse(((HashMap<String, Set<EventSubscriber>>) subField.get(EventBus.getInstance())).get(Constants.IS_GATHERING).contains(sensorService));
//    }
//
//    @SuppressWarnings("unchecked")
//    @Test
//    public void testOnDestroyUnregistersFromAll() throws Exception {
////        serviceController.attach().create().startCommand(0, 0);
////        CollectionService service = serviceController.get();
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
//        CollectionService sensorService = serviceController.attach().create().get();
//        Method startMethod = CollectionService.class.getDeclaredMethod("startListen");
//        startMethod.setAccessible(true);
//        startMethod.invoke(sensorService);
//        verify(mockedDetector1).startListening();
//        verify(mockedDetector2).startListening();
//    }
//
//    @Test
//    public void testStopListening() throws Exception {
//        CollectionService sensorService = serviceController.attach().create().get();
//        Method startMethod = CollectionService.class.getDeclaredMethod("startListen");
//        startMethod.setAccessible(true);
//        startMethod.invoke(sensorService);
//
//        Method stopMethod = CollectionService.class.getDeclaredMethod("stopListen");
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
////        CollectionService service = serviceController.attach().create().get();
//
//    }
//
//    @Test
//    public void testNoGodDamnAidlBindingStuff() throws Exception {
//        CollectionService sensorService = serviceController.attach().create().get();
//        Assert.assertNull(sensorService.onBind(null));
//    }
//
//    @Test
//    public void testStartupCommandWhenRequestingRestart() throws Exception {
//        AwesomePossum.authorizeGathering(RuntimeEnvironment.application, "fakeUniqueUserId");
////        AwesomePossum.setGathering(RuntimeEnvironment.application, false);
////        Assert.assertFalse(AwesomePossum.isGathering());
////        serviceController.attach().create().startCommand(0, ReqCodes.BACKGROUND_SERVICE);
////        Assert.assertTrue(AwesomePossum.isGathering());
//    }
//
//    @Test
//    public void testFillerMethodsForCoverage() throws Exception {
////        CollectionService service = serviceController.attach().create().get();
////        ShadowLog.setupLogging();
////        Assert.assertEquals(0, ShadowLog.getLogs().size());
////        service.listChanged();
////        Assert.assertEquals(1, ShadowLog.getLogs().size());
//    }
}
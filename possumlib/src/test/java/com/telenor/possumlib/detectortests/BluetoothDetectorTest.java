package com.telenor.possumlib.detectortests;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;

import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.changeevents.BluetoothChangeEvent;
import com.telenor.possumlib.changeevents.LocationChangeEvent;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.detectors.BluetoothDetector;
import com.telenor.possumlib.models.PossumBus;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.internal.Shadow;
import org.robolectric.shadows.ShadowBluetoothAdapter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PossumTestRunner.class)
public class BluetoothDetectorTest {
    private BluetoothDetector bluetoothDetector;
    @Mock
    private BluetoothManager mockedBluetoothManager;
    @Mock
    private Context mockedContext;
    private PossumBus eventBus;
//    @Mock
//    private BluetoothAdapter mockedBluetoothAdapter;
    private ShadowBluetoothAdapter shadowBluetoothAdapter;
    private BluetoothAdapter bluetoothAdapter;
    @Mock
    private BluetoothDevice mockedBluetoothDevice;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        JodaTimeAndroid.init(RuntimeEnvironment.application);
        eventBus = new PossumBus();
        bluetoothAdapter = Shadow.newInstanceOf(BluetoothAdapter.class);
        shadowBluetoothAdapter = Shadows.shadowOf(bluetoothAdapter);
        shadowBluetoothAdapter.setEnabled(true);
        when(mockedContext.getFilesDir()).thenReturn(RuntimeEnvironment.application.getFilesDir());
        when(mockedContext.getSystemService(Context.BLUETOOTH_SERVICE)).thenReturn(mockedBluetoothManager);
        when(mockedContext.checkPermission(anyString(), anyInt(), anyInt())).thenReturn(PackageManager.PERMISSION_GRANTED);
//        when(mockedBluetoothManager.getAdapter()).thenReturn(mockedBluetoothAdapter);
        when(mockedBluetoothManager.getAdapter()).thenReturn(bluetoothAdapter);
        bluetoothDetector = new BluetoothDetector(mockedContext, "fakeUnique", eventBus, false);
    }

    @After
    public void tearDown() throws Exception {
        bluetoothDetector.terminate();
        bluetoothDetector = null;
    }

    @Test
    public void testInitWithoutAdapter() throws Exception {
        bluetoothDetector = new BluetoothDetector(Mockito.mock(Context.class), "fakeUnique", eventBus, false);
        Assert.assertNotNull(bluetoothDetector);
        Assert.assertFalse(bluetoothDetector.isAvailable());
        Assert.assertFalse(bluetoothDetector.isEnabled());
    }

    @Test
    public void testDefaults() throws Exception {
        Assert.assertEquals(DetectorType.Bluetooth, bluetoothDetector.detectorType());
        Assert.assertEquals(900000, bluetoothDetector.minimumInterval());
        Assert.assertEquals("Bluetooth", bluetoothDetector.detectorName());
        Method storeWithIntervalMethod = BluetoothDetector.class.getDeclaredMethod("storeWithInterval");
        storeWithIntervalMethod.setAccessible(true);
        Assert.assertFalse((boolean)storeWithIntervalMethod.invoke(bluetoothDetector));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Test
    public void testStopScanWhenNotScanning() throws Exception {
        BluetoothAdapter mockedBluetoothAdapter = mock(BluetoothAdapter.class);
        when(mockedBluetoothManager.getAdapter()).thenReturn(mockedBluetoothAdapter);
        bluetoothDetector = new BluetoothDetector(mockedContext, "fakeUnique", eventBus, false);
        Method stopScanMethod = BluetoothDetector.class.getDeclaredMethod("stopScan");
        stopScanMethod.setAccessible(true);
        stopScanMethod.invoke(bluetoothDetector);
        verify(mockedBluetoothAdapter, times(1)).isDiscovering();
        verify(mockedBluetoothAdapter, times(0)).cancelDiscovery();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Test
    public void testStopScanWhenScanning() throws Exception {
        BluetoothAdapter mockedBluetoothAdapter = mock(BluetoothAdapter.class);
        when(mockedBluetoothAdapter.isDiscovering()).thenReturn(true);
        when(mockedBluetoothManager.getAdapter()).thenReturn(mockedBluetoothAdapter);
        bluetoothDetector = new BluetoothDetector(mockedContext, "fakeUnique", eventBus, false);
        Method stopScanMethod = BluetoothDetector.class.getDeclaredMethod("stopScan");
        stopScanMethod.setAccessible(true);
        stopScanMethod.invoke(bluetoothDetector);
        verify(mockedBluetoothAdapter, times(1)).isDiscovering();
        verify(mockedBluetoothAdapter, times(1)).cancelDiscovery();
    }

    @Test
    public void testEventReceived() throws Exception {
        bluetoothDetector.eventReceived(new LocationChangeEvent());
        Field timerField = BluetoothDetector.class.getDeclaredField("timer");
        timerField.setAccessible(true);
        Assert.assertNull(timerField.get(bluetoothDetector));
        bluetoothDetector.eventReceived(new BluetoothChangeEvent());
        Assert.assertNotNull(timerField.get(bluetoothDetector));
    }

    @Test
    public void testTimer() throws Exception {
        Field timerField = BluetoothDetector.class.getDeclaredField("timer");
        timerField.setAccessible(true);
        Assert.assertNull(timerField.get(bluetoothDetector));
        Method reCreateTimerMethod = BluetoothDetector.class.getDeclaredMethod("reCreateTimer");
        reCreateTimerMethod.setAccessible(true);
        reCreateTimerMethod.invoke(bluetoothDetector);
        Assert.assertNotNull(timerField.get(bluetoothDetector));
    }

    @Test
    public void testScanForBluetoothBeforeMinInterval() throws Exception {
        Field lastScanField = BluetoothDetector.class.getDeclaredField("lastStart");
        lastScanField.setAccessible(true);
        lastScanField.set(bluetoothDetector, DateTime.now().getMillis()-bluetoothDetector.minimumInterval() - 10000);
        Field timerField = BluetoothDetector.class.getDeclaredField("timer");
        timerField.setAccessible(true);
        Assert.assertNull(timerField.get(bluetoothDetector));
        Method scanMethod = BluetoothDetector.class.getDeclaredMethod("scanForBluetooth");
        scanMethod.setAccessible(true);
        scanMethod.invoke(bluetoothDetector);
        Assert.assertNotNull(timerField.get(bluetoothDetector));
    }

    @Test
    public void testScanForBluetoothAfterMinInterval() throws Exception {
        Field lastScanField = BluetoothDetector.class.getDeclaredField("lastStart");
        lastScanField.setAccessible(true);
        lastScanField.set(bluetoothDetector, DateTime.now().getMillis()-10);
        Field timerField = BluetoothDetector.class.getDeclaredField("timer");
        timerField.setAccessible(true);
        Assert.assertNull(timerField.get(bluetoothDetector));
        Method scanMethod = BluetoothDetector.class.getDeclaredMethod("scanForBluetooth");
        scanMethod.setAccessible(true);
        scanMethod.invoke(bluetoothDetector);
        Assert.assertNull(timerField.get(bluetoothDetector));
    }

    @Test
    public void testRegisterReceiverOnInitialize() throws Exception {
        Field field = BluetoothDetector.class.getDeclaredField("receiver");
        field.setAccessible(true);
        BroadcastReceiver receiver = (BroadcastReceiver) field.get(bluetoothDetector);
        Assert.assertNotNull(receiver);
        verify(mockedContext).registerReceiver(Matchers.eq(receiver), any(IntentFilter.class));
        Field fieldIntentFilter = BluetoothDetector.class.getDeclaredField("intentFilter");
        fieldIntentFilter.setAccessible(true);
        IntentFilter intentFilter = (IntentFilter) fieldIntentFilter.get(bluetoothDetector);
        Assert.assertTrue(intentFilter.hasAction(BluetoothDevice.ACTION_FOUND));
        Assert.assertTrue(intentFilter.hasAction(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    @Test
    public void testOnReceive() throws Exception {
        shadowBluetoothAdapter.setEnabled(true);
        Assert.assertFalse(shadowBluetoothAdapter.isDiscovering());
        Assert.assertTrue(bluetoothDetector.isEnabled());
        Assert.assertTrue(bluetoothDetector.isAvailable());
        Assert.assertFalse(bluetoothDetector.isBLEDevice());
        verify(mockedContext, times(1)).registerReceiver(any(BroadcastReceiver.class), any(IntentFilter.class));
        bluetoothDetector.startListening();
        Intent intent = new Intent(BluetoothDevice.ACTION_FOUND);
        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, mockedBluetoothDevice);
//        ShadowApplication.getInstance().sendBroadcast(intent);
        // TODO: Find a way to shadow the bluetooth and broadcastReceiver
//        ShadowLocalBroadcastManager.getInstance(RuntimeEnvironment.application).sendBroadcast(intent);
//        Assert.assertEquals(1, bluetoothDetector.sessionValues().size());
//        bluetoothDetector.stopListening();
    }

    @Test
    public void testInitWithAdapterWithModelLoaded() throws Exception {
        Assert.assertTrue(bluetoothDetector.isAvailable());
        Assert.assertTrue(bluetoothDetector.isEnabled());
    }

    @Test
    public void testInitWithAdapterWithModelNotLoaded() throws Exception {
        Assert.assertTrue(bluetoothDetector.isAvailable());
        Assert.assertTrue(bluetoothDetector.isEnabled());
    }

    @Test
    public void testSystemMissingBluetooth() throws Exception {
        mockedContext = mock(Context.class);
        when(mockedContext.getSystemService(Context.BLUETOOTH_SERVICE)).thenReturn(null);
        bluetoothDetector = new BluetoothDetector(mockedContext, "fakeUnique", eventBus, false);
        Assert.assertFalse(bluetoothDetector.isAvailable());
        Assert.assertFalse(bluetoothDetector.isEnabled());
    }

    @Test
    public void testStartListening() throws Exception {
        Assert.assertTrue(bluetoothDetector.startListening());
        Mockito.verify(mockedContext).registerReceiver(any(BroadcastReceiver.class), any(IntentFilter.class));
        Assert.assertTrue(bluetoothDetector.isListening());
    }

    @Test
    public void testStopListening() throws Exception {
        Assert.assertTrue(bluetoothDetector.startListening());
        bluetoothDetector.stopListening();
        Assert.assertFalse(bluetoothDetector.isListening());
    }

    @Test
    public void testTerminateRemovesBroadcastReceiver() throws Exception {
        bluetoothDetector.terminate();
        Mockito.verify(mockedContext).unregisterReceiver(any(BroadcastReceiver.class));
    }

    @Test
    public void testBLEIsDisabled() throws Exception {
        Method method = BluetoothDetector.class.getDeclaredMethod("isBLEDevice");
        method.setAccessible(true);
        boolean isBle = (boolean) method.invoke(bluetoothDetector);
        Assert.assertFalse(isBle);
    }

    @Test
    public void testOnReceiveFound() throws Exception {
//        Field field = BluetoothDetector.class.getDeclaredField("receiver");
//        field.setAccessible(true);
//        BroadcastReceiver receiver = (BroadcastReceiver)field.get(bluetoothDetector);
//        Mockito.when(mockedBluetoothDevice.getType()).thenReturn(0);
//        Mockito.when(mockedBluetoothDevice.getBondState()).thenReturn(10);
//        Mockito.when(mockedBluetoothDevice.getAddress()).thenReturn("fakeAddress");
//        Intent intent = new Intent(BluetoothDevice.ACTION_FOUND);
//        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, mockedBluetoothDevice);
//        intent.putExtra(BluetoothDevice.EXTRA_RSSI, 3);
//        Assert.assertEquals(0, bluetoothDetector.sessionValues().size());
//        receiver.onReceive(mockedContext, new Intent(BluetoothDevice.ACTION_FOUND));
//        Assert.assertEquals(1, bluetoothDetector.sessionValues().size());
    }
}
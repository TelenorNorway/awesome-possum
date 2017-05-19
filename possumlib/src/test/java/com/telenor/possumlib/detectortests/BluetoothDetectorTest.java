package com.telenor.possumlib.detectortests;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.detectors.BluetoothDetector;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
    private EventBus eventBus;
    private ShadowBluetoothAdapter shadowBluetoothAdapter;
    //    private BluetoothAdapter mockedBluetoothAdapter;
    @Mock
    private BluetoothDevice mockedBluetoothDevice;
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        eventBus = new EventBus();
        BluetoothAdapter bluetoothAdapter = Shadow.newInstanceOf(BluetoothAdapter.class);
        shadowBluetoothAdapter = Shadows.shadowOf(bluetoothAdapter);
        shadowBluetoothAdapter.setEnabled(true);
        when(mockedContext.getSystemService(Context.BLUETOOTH_SERVICE)).thenReturn(mockedBluetoothManager);
//        when(mockedBluetoothManager.getAdapter()).thenReturn(bluetoothAdapter);
        int permission = PackageManager.PERMISSION_GRANTED;
        when(mockedContext.checkPermission(anyString(), anyInt(), anyInt())).thenReturn(permission);
        bluetoothDetector = new BluetoothDetector(mockedContext, "fakeUnique", "fakeId", eventBus);
    }

    @After
    public void tearDown() throws Exception {
        bluetoothDetector.terminate();
        bluetoothDetector = null;
    }

    @Test
    public void testInitWithoutAdapter() throws Exception {
//        when(mockedBluetoothManager.getAdapter()).thenReturn(null);
        bluetoothDetector = new BluetoothDetector(mockedContext, "fakeUnique", "fakeId", eventBus);
        Assert.assertNotNull(bluetoothDetector);
        Assert.assertFalse(bluetoothDetector.isAvailable());
        Assert.assertFalse(bluetoothDetector.isEnabled());
        /*        Field field = BluetoothDetector.class.getDeclaredField("bluetoothAdapter");
        field.setAccessible(true);
        Assert.assertNotNull(field.get(bluetoothDetector));
        */
    }

    @Test
    public void testDefaults() throws Exception {
        Assert.assertEquals(DetectorType.Bluetooth, bluetoothDetector.detectorType());
        Assert.assertEquals(12000, bluetoothDetector.detectInterval());
        Assert.assertEquals(900000, bluetoothDetector.minimumInterval());
    }

    @Test
    public void testRegisterReceiverOnInitialize() throws Exception {
        Field field = BluetoothDetector.class.getDeclaredField("receiver");
        field.setAccessible(true);
        BroadcastReceiver receiver = (BroadcastReceiver)field.get(bluetoothDetector);
        Assert.assertNotNull(receiver);
        verify(mockedContext).registerReceiver(Matchers.eq(receiver), any(IntentFilter.class));
        Field fieldIntentFilter = BluetoothDetector.class.getDeclaredField("intentFilter");
        fieldIntentFilter.setAccessible(true);
        IntentFilter intentFilter = (IntentFilter)fieldIntentFilter.get(bluetoothDetector);
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
        bluetoothDetector = new BluetoothDetector(mockedContext, "fakeUnique", "fakeId", eventBus);
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
        boolean isBle = (boolean)method.invoke(bluetoothDetector);
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
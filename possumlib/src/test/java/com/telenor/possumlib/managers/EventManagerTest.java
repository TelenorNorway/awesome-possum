package com.telenor.possumlib.managers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;

import com.telenor.possumlib.PossumTestRunner;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Config(sdk = 19)
@RunWith(PossumTestRunner.class)
public class EventManagerTest {
    private EventManager eventManager;

    @Before
    public void setUp() throws Exception {
        eventManager = new EventManager(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() throws Exception {
        eventManager.terminate(RuntimeEnvironment.application);
        eventManager = null;
    }

    @SuppressWarnings("WrongConstant")
    @Test
    public void testInitOnMarshallow() throws Exception {
        Assert.assertNotNull(eventManager);
        Context mockedContext = mock(Context.class);
        BatteryManager mockedBatteryManager = mock(BatteryManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when(mockedContext.getSystemService(eq(Context.BATTERY_SERVICE))).thenReturn(mockedBatteryManager);
            when(mockedBatteryManager.isCharging()).thenReturn(true);
        }
        eventManager = new EventManager(mockedContext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            verify(mockedBatteryManager).isCharging();
        } else {
            verify(mockedContext).registerReceiver(Matchers.isNull(BroadcastReceiver.class), any(IntentFilter.class));
        }
    }

    @Config(sdk = 19)
    @SuppressWarnings("WrongConstant")
    @Test
    public void testInitBelowMarshallow() throws Exception {
        Assert.assertNotNull(eventManager);
        Context mockedContext = mock(Context.class);
        eventManager = new EventManager(mockedContext);
        verify(mockedContext).registerReceiver(Matchers.isNull(BroadcastReceiver.class), any(IntentFilter.class));
    }

    @Test
    public void testOnReceiveEmptyDoesNothing() throws Exception {
        ShadowLog.setupLogging();
//        eventManager.onReceive(RuntimeEnvironment.application, null);
//        eventManager.onReceive(RuntimeEnvironment.application, new Intent());
        Assert.assertEquals(0, ShadowLog.getLogs().size());
    }

    @Test
    public void testOnReceiveUnknownIntentLogsIt() throws Exception {
        ShadowLog.setupLogging();
//        eventManager.onReceive(RuntimeEnvironment.application, new Intent("SuperFakeAction"));
        Assert.assertEquals(1, ShadowLog.getLogs().size());
        Assert.assertEquals("Unhandled action:SuperFakeAction", ShadowLog.getLogs().get(0).msg);
    }
}
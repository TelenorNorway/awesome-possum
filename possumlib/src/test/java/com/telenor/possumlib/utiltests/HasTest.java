package com.telenor.possumlib.utiltests;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.utils.Has;

import static org.mockito.Mockito.when;

@RunWith(PossumTestRunner.class)
public class HasTest {
    @Mock
    private Context mockedContext;
    @Mock
    private Context mockedApplicationContext;
    @Mock
    private ConnectivityManager mockedConnectivityManager;
    @Mock
    private WifiManager mockedWifiManager;
    @Mock
    private NetworkInfo mockedNetworkInfo;

    @SuppressLint("WifiManagerPotentialLeak")
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(mockedContext.getApplicationContext()).thenReturn(mockedApplicationContext);
        when(mockedApplicationContext.getSystemService(Context.WIFI_SERVICE)).thenReturn(mockedWifiManager);
        when(mockedApplicationContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockedConnectivityManager);
        when(mockedConnectivityManager.getActiveNetworkInfo()).thenReturn(mockedNetworkInfo);
    }

    @After
    public void tearDown() throws Exception {
        mockedContext = null;
        mockedConnectivityManager = null;
        mockedNetworkInfo = null;
    }

    @Test
    public void testHasNetworkWhenActuallyHasIt() throws Exception {
        when(mockedNetworkInfo.isConnected()).thenReturn(true);
        Assert.assertTrue(Has.network(mockedContext));
    }

    @Test
    public void testHasNetworkWhenNoActive() throws Exception {
        when(mockedNetworkInfo.isConnected()).thenReturn(false);
        Assert.assertFalse(Has.network(mockedContext));
    }

    @Test
    public void testNetworkFailedReasonWhenConnected() throws Exception {
        when(mockedNetworkInfo.isConnected()).thenReturn(true);
        Assert.assertNull(Has.networkFailedReason(mockedContext));
    }

    @Test
    public void testNetworkFailedWhenNoNetwork() throws Exception {
        when(mockedConnectivityManager.getActiveNetworkInfo()).thenReturn(null);
        Assert.assertNull(Has.networkFailedReason(mockedContext));
    }

    @Test
    public void testNetworkFailedReasonWhenDisconnected() throws Exception {
        when(mockedNetworkInfo.isConnected()).thenReturn(false);
        when(mockedNetworkInfo.getReason()).thenReturn("EpicFail");
        Assert.assertEquals("EpicFail", Has.networkFailedReason(mockedContext));
    }

    @Test
    public void testHasWifiWhenConnectedToWifi() throws Exception {
        when(mockedNetworkInfo.getType()).thenReturn(ConnectivityManager.TYPE_WIFI);
        when(mockedNetworkInfo.isConnected()).thenReturn(true);
        Assert.assertTrue(Has.wifi(mockedContext));
    }

    @Test
    public void testHasWifiWhenNotConnected() throws Exception {
        when(mockedNetworkInfo.isConnected()).thenReturn(false);
        Assert.assertFalse(Has.wifi(mockedContext));
    }

    @Test
    public void testWifiDisabled() throws Exception {
        when(mockedNetworkInfo.isConnected()).thenReturn(false);
        Assert.assertFalse(Has.wifi(mockedContext));
    }

    @Test
    public void testHasWifiWhenConnectedToMobile() throws Exception {
        when(mockedNetworkInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);
        when(mockedNetworkInfo.isConnected()).thenReturn(true);
        Assert.assertFalse(Has.wifi(mockedContext));
    }

    @Test
    public void testHasWifiConnectionWhenDisabled() throws Exception {
        when(mockedWifiManager.isWifiEnabled()).thenReturn(false);
        when(mockedNetworkInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);
        when(mockedNetworkInfo.isConnected()).thenReturn(true);
        Assert.assertFalse(Has.wifiConnection(mockedContext));
    }

    @Test
    public void testHasWifiConnectionWhenEnabled() throws Exception {
        when(mockedNetworkInfo.getType()).thenReturn(ConnectivityManager.TYPE_WIFI);
        when(mockedWifiManager.isWifiEnabled()).thenReturn(true);
        when(mockedNetworkInfo.isConnected()).thenReturn(true);
        Assert.assertTrue(Has.wifiConnection(mockedContext));
    }

    @Test
    public void testHasNoWifiConnectionWhenDisabled() throws Exception {
        when(mockedWifiManager.isWifiEnabled()).thenReturn(false);
        Assert.assertFalse(Has.wifiConnection(mockedContext));
    }

    @Test
    public void testHasNoWifiConnectionWhenEnabled() throws Exception {
        when(mockedNetworkInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);
        when(mockedWifiManager.isWifiEnabled()).thenReturn(true);
        when(mockedNetworkInfo.isConnected()).thenReturn(true);
        Assert.assertFalse(Has.wifiConnection(mockedContext));
    }
}
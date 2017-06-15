package com.telenor.possumlib.detectortests;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.telenor.possumlib.FileManipulator;
import com.telenor.possumlib.PossumTestRunner;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.detectors.NetworkDetector;
import com.telenor.possumlib.models.PossumBus;
import com.telenor.possumlib.utils.FileUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowLog;

import java.io.File;
import java.lang.reflect.Field;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PossumTestRunner.class)
public class NetworkDetectorTest {
    private NetworkDetector networkDetector;
    @Mock
    private Context mockedContext;
    @Mock
    private WifiManager mockedWifiService;
    @Mock
    private ConnectivityManager mockedConnectivityManager;
    @Mock
    private NetworkInfo mockedNetworkInfo;
    @Mock
    private Context mockedApplicationContext;
    private PossumBus eventBus;
    private boolean checkedValue;

    @SuppressLint("WifiManagerPotentialLeak")
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        checkedValue = false;
        eventBus = new PossumBus();
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = "FakeNetwork";
        File dataDir = FileManipulator.getDataDir(RuntimeEnvironment.application);
        FileUtil.clearDirectory(RuntimeEnvironment.application, dataDir);
        File fakeFile = new File(dataDir.getAbsolutePath() + "/fakeNet");
        if (fakeFile.exists()) {
            Assert.assertTrue(fakeFile.delete());
        }
        Assert.assertTrue(fakeFile.createNewFile());
        when(mockedContext.getApplicationContext()).thenReturn(mockedApplicationContext);
        when(mockedNetworkInfo.getType()).thenReturn(ConnectivityManager.TYPE_WIFI);
        when(mockedApplicationContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockedConnectivityManager);
        when(mockedConnectivityManager.getActiveNetworkInfo()).thenReturn(mockedNetworkInfo);
        when(mockedApplicationContext.getSystemService(Context.WIFI_SERVICE)).thenReturn(mockedWifiService);
        when(mockedApplicationContext.getFilesDir()).thenReturn(RuntimeEnvironment.application.getFilesDir());
        when(mockedWifiService.isWifiEnabled()).thenReturn(true);
        networkDetector = new NetworkDetector(mockedContext, "fakeUnique", eventBus, false);
    }

    @After
    public void tearDown() throws Exception {
        networkDetector = null;
    }

    @Test
    public void testIntentFilterContainsRightValues() throws Exception {
        Field field = NetworkDetector.class.getDeclaredField("intentFilter");
        field.setAccessible(true);
        IntentFilter intentFilter = (IntentFilter)field.get(networkDetector);
        Assert.assertTrue(intentFilter.hasAction(WifiManager.WIFI_STATE_CHANGED_ACTION));
        Assert.assertTrue(intentFilter.hasAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        Assert.assertFalse(intentFilter.hasAction(Intent.ACTION_BATTERY_OKAY));
    }

    @Test
    public void testIsEnabledWithWifiManagerFound() throws Exception {
        Assert.assertTrue(networkDetector.isEnabled());
    }

    @SuppressLint("WifiManagerPotentialLeak")
    @Test
    public void testIsDisabledWhenNoWifiManager() throws Exception {
        when(mockedContext.getApplicationContext()).thenReturn(mockedApplicationContext);
        when(mockedNetworkInfo.getType()).thenReturn(ConnectivityManager.TYPE_MOBILE);
        when(mockedNetworkInfo.isConnected()).thenReturn(true);
        when(mockedApplicationContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockedConnectivityManager);
        when(mockedConnectivityManager.getActiveNetworkInfo()).thenReturn(mockedNetworkInfo);
        when(mockedApplicationContext.getSystemService(Context.WIFI_SERVICE)).thenReturn(null);
        networkDetector = new NetworkDetector(mockedContext, "fakeUnique", eventBus, false);
        Assert.assertFalse(networkDetector.isEnabled());
    }

    @Test
    public void testStartListening() throws Exception {
        Assert.assertTrue(networkDetector.startListening());
        verify(mockedWifiService).startScan();
    }

    @Test
    public void testType() throws Exception {
        Assert.assertEquals(DetectorType.Wifi, networkDetector.detectorType());
    }

    @Test
    public void testIsAvailable() throws Exception {
        when(mockedWifiService.isWifiEnabled()).thenReturn(false);
        Assert.assertFalse(networkDetector.wifiAvailable());
        when(mockedWifiService.isWifiEnabled()).thenReturn(true);
        Assert.assertTrue(networkDetector.wifiAvailable());
    }

    @Test
    public void testInvalidOnReceive() throws Exception {
        try {
            networkDetector.onReceive(null, new Intent(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            Assert.fail("Invalid onReceive - missing context");
        } catch (Exception e) {
            Assert.assertEquals("Missing vitals", e.getMessage());
        }
        try {
            networkDetector.onReceive(RuntimeEnvironment.application, null);
            Assert.fail("Invalid onReceive - missing intent");
        } catch (Exception e) {
            Assert.assertEquals("Missing vitals", e.getMessage());
        }
        ShadowLog.setupLogging();
        try {
            networkDetector.onReceive(mockedContext, new Intent());
            Assert.fail("Should not get here");
        } catch (Exception e) {
            Assert.assertEquals("Missing vitals", e.getMessage());
        }
    }

//    @Test
//    public void testOnReceiveScanResults() throws Exception {
//        networkDetector = new NetworkDetector(mockedContext) {
//            @Override
//            public boolean isListening() {
//                checkedValue = true;
//                return super.isListening();
//            }
//            @Override
//            public File storedData() {
//                return fakeFile;
//            }
//        };
//        List<ScanResult> scanResults = new ArrayList<>();
//        ScanResult mockedScanResult = mock(ScanResult.class);
//        mockedScanResult.BSSID = "FakeId";
//        mockedScanResult.level = 1337;
//        scanResults.add(mockedScanResult);
//        Assert.assertEquals(0, fakeFile.length());
//        when(mockedWifiService.getScanResults()).thenReturn(scanResults);
//        Assert.assertTrue(networkDetector.startListening());
//        networkDetector.onReceive(mockedContext, new Intent(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
//        Assert.assertEquals(27, fakeFile.length());
//        BufferedReader bufferedReader = new BufferedReader(new FileReader(fakeFile));
//        List<String> fileRead = new ArrayList<>();
//        String line;
//        while ((line = bufferedReader.readLine()) != null) {
//            fileRead.add(line);
//        }
//        bufferedReader.close();
//        Assert.assertEquals(1, fileRead.size());
//        Assert.assertTrue(fileRead.get(0).contains(" FakeId 1337"));
//        Assert.assertTrue(fakeFile.delete());
//    }

    @Test
    public void testOnReceiveNetworkChange() throws Exception {
        networkDetector = new NetworkDetector(mockedContext, "fakeUnique", eventBus, false) {
            @Override
            public void sensorStatusChanged() {
                checkedValue = true;
//                Assert.assertEquals(DetectorType.Wifi, sensorType);
            }
        };
        Assert.assertFalse(checkedValue);
        when(mockedWifiService.isWifiEnabled()).thenReturn(true);
        Intent intent = new Intent(WifiManager.WIFI_STATE_CHANGED_ACTION);
        Assert.assertTrue(networkDetector.isAvailable());
        intent.putExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_ENABLED);
        networkDetector.onReceive(mockedContext, intent);
        Assert.assertTrue(networkDetector.isAvailable());
        Assert.assertTrue(checkedValue);

        intent.putExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
        networkDetector.onReceive(mockedContext, intent);
        Assert.assertFalse(networkDetector.wifiAvailable());
    }

    @Test
    public void testOnReceiveUnhandled() throws Exception {
        ShadowLog.setupLogging();
        networkDetector.onReceive(mockedContext, new Intent(WifiManager.EXTRA_NEW_RSSI));
        Assert.assertTrue(ShadowLog.getLogs().size() == 1);
        Assert.assertEquals("Unhandled action:newRssi", ShadowLog.getLogs().get(0).msg);
    }
}
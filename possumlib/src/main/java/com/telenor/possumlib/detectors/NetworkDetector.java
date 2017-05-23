package com.telenor.possumlib.detectors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.abstractdetectors.AbstractEventDrivenDetector;
import com.telenor.possumlib.changeevents.BasicChangeEvent;
import com.telenor.possumlib.changeevents.WifiChangeEvent;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.interfaces.IOnReceive;
import com.telenor.possumlib.utils.Has;

import org.joda.time.DateTime;

import java.util.List;


/***
 * Uses network mac id's, decibel levels information and other network identification to pinpoint user identity.
 */
public class NetworkDetector extends AbstractEventDrivenDetector implements IOnReceive {
    private WifiManager wifiManager;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter = new IntentFilter();
    private int wifiState = WifiManager.WIFI_STATE_DISABLED;
    private long lastUserActiveScan;
    private boolean isRegistered;
    private boolean isScanning;

    public NetworkDetector(Context context, String identification, String secretKeyHash, @NonNull EventBus eventBus) throws IllegalArgumentException {
        super(context, identification, secretKeyHash, eventBus);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                NetworkDetector.this.onReceive(context, intent);
            }
        };
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        wifiState = Has.wifi(context) ? WifiManager.WIFI_STATE_ENABLED : WifiManager.WIFI_STATE_DISABLED;
    }

    @Override
    public boolean isEnabled() {
        return wifiManager != null;
    }

    /**
     * Minimum time between scan of networks
     * @return amount of time in milliseconds
     */
    private long minimumUserActionTime() {
        return 900000; // 15 minutes
    }

    @Override
    public boolean startListening() {
        boolean listen = super.startListening();
        if (listen) {
            if (!isRegistered) {
                context().registerReceiver(receiver, intentFilter);
                isRegistered = true;
            }
            performScan();
        }
        return listen;
    }

    @Override
    public void stopListening() {
        super.stopListening();
        if (isRegistered) {
            context().unregisterReceiver(receiver);
            isRegistered = false;
        }
    }

    private void performScan() {
        if (isEnabled() && wifiAvailable() && !isScanning()) {
            wifiManager.startScan();
            isScanning = true;
        }
    }

    private boolean isScanning() {
        return isScanning;
    }

    @Override
    protected boolean storeWithInterval() {
        return false;
    }

    public boolean wifiAvailable() {
        return wifiManager != null && wifiManager.isWifiEnabled() && wifiState == WifiManager.WIFI_STATE_ENABLED;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public int detectorType() {
        return DetectorType.Wifi;
    }

    @Override
    public String detectorName() {
        return "Network";
    }

    @Override
    public void eventReceived(BasicChangeEvent object) {
        if (object instanceof WifiChangeEvent && isListening()) {
            if (object.message() == null) {
                performScan();
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null || intent.getAction() == null) throw new RuntimeException("Missing vitals");
        switch (intent.getAction()) {
            case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                if (!isListening()) return;
                List<ScanResult> scanResults = wifiManager.getScanResults();
                for (ScanResult scanResult : scanResults) {
                    sessionValues.add(DateTime.now().getMillis() + " " + scanResult.BSSID + " " + scanResult.level);
                }
                storeData();
                isScanning = false;
                break;
            case WifiManager.WIFI_STATE_CHANGED_ACTION:
                wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                isScanning = false;
                sensorStatusChanged();
                break;
            case WifiManager.RSSI_CHANGED_ACTION: // Signal strength changed
                isScanning = false;
                break;
            default:
                Log.d(tag, "Unhandled action:" + intent.getAction());
        }
    }
}
package com.telenor.possumlib.detectors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonArray;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.interfaces.IOnReceive;
import com.telenor.possumlib.models.PossumBus;
import com.telenor.possumlib.utils.Has;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/***
 * Uses network mac id's, decibel levels information and other network identification to pinpoint user identity.
 */
public class NetworkDetector extends AbstractDetector implements IOnReceive {
    private WifiManager wifiManager;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter = new IntentFilter();
    private int wifiState = WifiManager.WIFI_STATE_DISABLED;
    private boolean isRegistered;
    private boolean isScanning;

    /**
     * Constructor for NetworkDetector
     *
     * @param context a valid android context
     * @param uniqueUserId the unique user id
     * @param eventBus an event bus for internal messages
     * @param authenticating whether the detector is used for authentication or data gathering
     */
    public NetworkDetector(Context context, String uniqueUserId, @NonNull PossumBus eventBus, boolean authenticating) {
        super(context, uniqueUserId, eventBus, authenticating);
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
    protected List<JsonArray> createInternalList() {
        return new CopyOnWriteArrayList<>();
    }

    @Override
    public boolean isEnabled() {
        return wifiManager != null;
    }

    @Override
    public boolean startListening() {
        boolean listen = super.startListening();
        if (listen) {
            if (!isRegistered) {
                context().getApplicationContext().registerReceiver(receiver, intentFilter);
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
            context().getApplicationContext().unregisterReceiver(receiver);
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

    public boolean wifiAvailable() {
        return wifiManager != null && wifiManager.isWifiEnabled() && wifiState == WifiManager.WIFI_STATE_ENABLED;
    }

    @Override
    public String requiredPermission() {
        return null;
    }

    @Override
    public int detectorType() {
        return DetectorType.Wifi;
    }

    @Override
    public String detectorName() {
        return "network";
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null || intent.getAction() == null) throw new RuntimeException("Missing vitals");
        switch (intent.getAction()) {
            case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                if (!isListening()) return;
                List<ScanResult> scanResults = wifiManager.getScanResults();
                for (ScanResult scanResult : scanResults) {
                    JsonArray array = new JsonArray();
                    array.add(""+now());
                    array.add(scanResult.BSSID);
                    array.add(""+scanResult.level);
                    sessionValues.add(array);
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
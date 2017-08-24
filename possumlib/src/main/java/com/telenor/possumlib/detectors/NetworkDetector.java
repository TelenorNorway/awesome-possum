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


/***
 * Uses network mac id's, decibel levels information and other network identification to pinpoint user identity.
 */
public class NetworkDetector extends AbstractDetector implements IOnReceive {
    private WifiManager wifiManager;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;
    private int wifiState = WifiManager.WIFI_STATE_DISABLED;
    private boolean isRegistered;

    /**
     * Constructor for NetworkDetector
     *
     * @param context a valid android context
     * @param eventBus an event bus for internal messages
     */
    public NetworkDetector(Context context, @NonNull PossumBus eventBus) {
        super(context, eventBus);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                NetworkDetector.this.onReceive(context, intent);
            }
        };
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        wifiState = Has.wifi(context) ? WifiManager.WIFI_STATE_ENABLED : WifiManager.WIFI_STATE_DISABLED;
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
            List<ScanResult> scanResults = wifiManager.getScanResults();
            long latestTimeStamp = 0;
            for (ScanResult scanResult : scanResults) {
                JsonArray array = new JsonArray();
                array.add(""+now());
                array.add(scanResult.BSSID);
                array.add(""+scanResult.level);
                if (scanResult.timestamp > latestTimeStamp) {
                    latestTimeStamp = scanResult.timestamp;
                }
                sessionValues.add(array);
            }
            storeData();
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

    public boolean wifiAvailable() {
        return wifiManager != null && wifiManager.isWifiEnabled() && wifiState == WifiManager.WIFI_STATE_ENABLED;
    }

    // Do we really want to limit it only to permitted when wifi is available?
    /*@Override
    public boolean isPermitted() {
        return wifiAvailable();
    }*/

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
            case WifiManager.WIFI_STATE_CHANGED_ACTION:
                wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                sensorStatusChanged();
                break;
            case WifiManager.RSSI_CHANGED_ACTION: // Signal strength changed
                break;
            default:
                Log.d(tag, "Unhandled action:" + intent.getAction());
        }
    }
}
package com.telenor.possumlib.detectors;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonArray;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.models.PossumBus;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/***
 * Uses bluetooth to find paired devices or even regularly discovered devices to both confirm is on correct device but also that person is in relative location to usually found devices.
 */
public class BluetoothDetector extends AbstractDetector {
    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver receiver;
    private ScanCallback callback;
    private IntentFilter intentFilter;

    private boolean isBLE = false;
    private boolean isRegistered;
    private long lastStart;

    private static final String tag = BluetoothDetector.class.getName();

    /**
     * Constructor for a Bluetooth Detector
     *
     * @param context a valid android context
     * @param eventBus an event bus for internal messages
     */
    public BluetoothDetector(final Context context, @NonNull PossumBus eventBus) {
        super(context, eventBus);
        // TODO: Confirm coarse/fine location and bluetooth admin for this
        BluetoothManager bluetoothManager;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                // Device does not support bluetooth
                return;
            }
            bluetoothAdapter = bluetoothManager.getAdapter();
        } else {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if (bluetoothAdapter == null) {
            // Device does not support bluetooth
            return;
        }
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    JsonArray array = new JsonArray();
                    switch (intent.getAction()) {
                        case BluetoothDevice.ACTION_PAIRING_REQUEST:
                            array.add(""+now());
                            array.add(intent.getAction());
                            sessionValues.add(array);
                            break;
                        case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        case BluetoothDevice.ACTION_ACL_CONNECTED:
                        case BluetoothDevice.ACTION_FOUND:
                            if (!isListening()) {
                                return;
                            }
                            // bondState = 10 -> not bonded, 11 -> bonding, 12 -> bonded
                            // type = 0 -> Unknown, 1 -> classic BT, 2 -> BLE, 3 -> Dual
                            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                            short Rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                            short txPowerLevel = Short.MIN_VALUE;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                array.add(""+now());
                                array.add(intent.getAction());
                                array.add(""+device.getType());
                                array.add(device.getAddress());
                                array.add(""+Rssi);
                                array.add(""+txPowerLevel);
                                array.add(""+device.getBondState());
                                sessionValues.add(array);
                            } else {
                                array.add(""+now());
                                array.add(intent.getAction());
                                array.add("-1");
                                array.add(device.getAddress());
                                array.add(""+Rssi);
                                array.add(""+txPowerLevel);
                                array.add(""+device.getBondState());
                                sessionValues.add(array);
                            }
                            break;
                        case BluetoothAdapter.ACTION_STATE_CHANGED:
                            switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                                    BluetoothAdapter.ERROR)) {
                                case BluetoothAdapter.STATE_OFF:
                                    sensorStatusChanged();
                                    break;
                                case BluetoothAdapter.STATE_ON:
                                    sensorStatusChanged();
                                    break;
                                default:
                            }
                            break;
                        default:
                            Log.i(tag, "Unhandled intent:" + intent.toString());
                    }
                } catch (Exception ignore) {
                }
            }
        };
        intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        context().getApplicationContext().registerReceiver(receiver, intentFilter);
        isRegistered = true;
        isBLE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && bluetoothAdapter.getBluetoothLeScanner() != null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            callback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        try {
                            BluetoothDevice device = result.getDevice();
                            ScanRecord record = result.getScanRecord();
                            short txPowerLvl;
                            if (record != null) {
                                txPowerLvl = (short) record.getTxPowerLevel(); // Transmission power level in Db
                            } else txPowerLvl = Short.MIN_VALUE;
                            JsonArray array = new JsonArray();
                            array.add(""+now());
                            array.add(""+device.getType());
                            array.add(device.getAddress());
                            array.add(""+result.getRssi());
                            array.add(""+txPowerLvl);
                            array.add(""+device.getBondState());
                            sessionValues.add(array);
                        } catch (Exception ignore) {
                        }
                    }
                }
            };
        }
    }

    @Override
    protected List<JsonArray> createInternalList() {
        return new CopyOnWriteArrayList<>();
    }

    @Override
    public boolean isEnabled() {
        return bluetoothAdapter != null;
    }

    @Override
    public void terminate() {
        if (isRegistered) {
            context().getApplicationContext().unregisterReceiver(receiver);
            isRegistered = false;
        }
        super.terminate();
    }

    /**
     * Minimum interval set to 15 minutes, so it doesn't spam scan too often
     * Note: Was 1 minute, set to 15 minutes - hopefully not too long
     *
     * @return time in milliseconds
     */
    public int minimumInterval() {
        return 900000;
    }

    @Override
    public boolean startListening() {
        boolean listen = super.startListening();
        if (listen) {
            scanForBluetooth();
        }
        return listen;
    }

    private void scanForBluetooth() {
        long nowStamp = now();
        long diff = nowStamp - lastStart;
        if (diff >= minimumInterval()) {
            Log.d(tag, "Starting bluetooth scan");
            if (isBLEDevice() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Start BLE scan
                BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
                if (scanner != null) {
                    ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();
                    scanSettingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
                    scanner.startScan(null, scanSettingsBuilder.build(), callback);
                    lastStart = nowStamp;
                }
            } else {
                // Start regular scan
                if (!bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.startDiscovery();
                    lastStart = nowStamp;
                }
            }
        }
    }

    public boolean isBLEDevice() {
        return isBLE;
    }

    private void stopScan() {
        if (isBLEDevice() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
            if (scanner != null) scanner.stopScan(callback);
        } else if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    @Override
    public void stopListening() {
        if (isListening()) {
            stopScan();
        }
        super.stopListening();
    }

    @Override
    public int detectorType() {
        return DetectorType.Bluetooth;
    }

    @Override
    public String detectorName() {
        return "bluetooth";
    }

    @Override
    public boolean isAvailable() {
        return bluetoothAdapter != null && super.isAvailable(); //&& bluetoothAdapter.isEnabled()
    }

    @Override
    public String requiredPermission() {
        return Manifest.permission.BLUETOOTH_ADMIN;
    }
}
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
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.common.eventbus.EventBus;
import com.telenor.possumlib.abstractdetectors.AbstractEventDrivenDetector;
import com.telenor.possumlib.changeevents.BasicChangeEvent;
import com.telenor.possumlib.changeevents.BluetoothChangeEvent;
import com.telenor.possumlib.constants.DetectorType;

import org.joda.time.DateTime;

import java.util.Timer;
import java.util.TimerTask;

/***
 * Uses bluetooth to find paired devices or even regularly discovered devices to both confirm is on correct device but also that person is in relative location to usually found devices.
 */
public class BluetoothDetector extends AbstractEventDrivenDetector {
    private BluetoothAdapter bluetoothAdapter;
    private BroadcastReceiver receiver;
    private ScanCallback callback;
    private boolean isBLE = false;
    private boolean isRegistered;
    private long lastStart;
    private Timer timer;

    private static final String tag = BluetoothDetector.class.getName();

    public BluetoothDetector(final Context context, String identification, String secretKeyHash, @NonNull EventBus eventBus) {
        super(context, identification, secretKeyHash, eventBus);
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
                    switch (intent.getAction()) {
                        case BluetoothDevice.ACTION_PAIRING_REQUEST:
                            sessionValues.add(DateTime.now().getMillis()+" "+intent.getAction());
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
                                sessionValues.add(DateTime.now().getMillis() + " " + intent.getAction()+" "+device.getType() + " " + device.getAddress() + " " + Rssi + " " + txPowerLevel + " " + device.getBondState());
                            } else {
                                sessionValues.add(DateTime.now().getMillis() + " " + intent.getAction()+" -1 " + device.getAddress() + " " + Rssi + " " + txPowerLevel + " " + device.getBondState());
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
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        context().registerReceiver(receiver, intentFilter);
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
                            sessionValues.add(DateTime.now().getMillis() + " " + device.getType() + " " + device.getAddress() + " " + result.getRssi() + " " + txPowerLvl + " " + device.getBondState()); // + "\n"
                        } catch (Exception ignore) {
                        }
                    }
                }
            };
        }
    }

    @Override
    public boolean isEnabled() {
        return bluetoothAdapter != null;
    }

    @Override
    public void terminate() {
        if (isRegistered) {
            context().unregisterReceiver(receiver);
            isRegistered = false;
        }
        super.terminate();
    }

    private void reCreateTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        timer = new Timer();
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

    /**
     * How long it will scan for
     * @return the value in milliseconds, atm 12 seconds
     */
    public long detectInterval() {
        return 12000;
    }

    private void scanForBluetooth() {
        long nowStamp = DateTime.now().getMillis();
        long diff = nowStamp - lastStart;
        if (diff >= minimumInterval()) {
            Log.d(tag, "Starting bluetooth scan");
            reCreateTimer();
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
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    stopScan();
                }
            }, detectInterval());
        }
    }

    @Override
    public boolean isPermitted() {
        return ContextCompat.checkSelfPermission(context(), Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isBLEDevice() {
        return isBLE;
    }

    private void stopScan() {
        Log.d(tag, "Stopping bluetooth scan");
        if (isBLEDevice() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
            if (scanner != null) scanner.stopScan(callback);
        } else if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        storeData();
    }

    @Override
    public boolean isValidSet() {
        return true;
    }

    @Override
    protected boolean storeWithInterval() {
        return false;
    }

    @Override
    public void eventReceived(BasicChangeEvent object) {
        if (object instanceof BluetoothChangeEvent) {
            scanForBluetooth();
        }
    }

    @Override
    public int detectorType() {
        return DetectorType.Bluetooth;
    }

    @Override
    public boolean isAvailable() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled() && isPermitted();
    }
}
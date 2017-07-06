package com.telenor.possumlib.detectors;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonArray;
import com.telenor.possumlib.abstractdetectors.AbstractDetector;
import com.telenor.possumlib.constants.DetectorType;
import com.telenor.possumlib.models.PossumBus;

import java.util.List;

/***
 * Uses gps with network to retrieve a position regularly
 */
public class LocationDetector extends AbstractDetector implements LocationListener {
    private LocationManager locationManager;
    private boolean gpsAvailable;
    private boolean networkAvailable;
    private BroadcastReceiver providerChangedReceiver;
    private boolean isRegistered;
    private float maxSpeed;
    private List<String> providers;

    /**
     * Constructor for the location detector
     *
     * @param context a valid android context
     * @param uniqueUserId the unique user id
     * @param eventBus an event bus for internal messages
     * @param authenticating whether the detector is used for authentication or data gathering
     */
    public LocationDetector(Context context, String uniqueUserId, @NonNull PossumBus eventBus, boolean authenticating) {
        super(context, uniqueUserId, eventBus, authenticating);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Log.d(tag, "No positioning available");
            return;
        }
        providers = locationManager.getAllProviders();
        networkAvailable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        gpsAvailable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        providerChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    try {
                        int locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
                        switch (locationMode) {
                            case Settings.Secure.LOCATION_MODE_HIGH_ACCURACY:
                                // gps, bluetooth, wifi, mobile networks
                                gpsAvailable = true;
                                networkAvailable = true;
                                break;
                            case Settings.Secure.LOCATION_MODE_OFF:
                                // none
                                gpsAvailable = false;
                                networkAvailable = false;
                                break;
                            case Settings.Secure.LOCATION_MODE_SENSORS_ONLY:
                                // gps
                                gpsAvailable = true;
                                networkAvailable = false;
                                break;
                            case Settings.Secure.LOCATION_MODE_BATTERY_SAVING:
                                // Uses wifi, bluetooth & mobile networks
                                gpsAvailable = false;
                                networkAvailable = true;
                                break;
                            default:
                                Log.d(tag, "Unhandled mode:" + locationMode);
                        }
                        sensorStatusChanged();
                    } catch (Settings.SettingNotFoundException e) {
                        Log.e(tag, "Settings not found:", e);
                    }
                } else {
                    // TODO: Confirm this is correct way to find provider below api 19
                    gpsAvailable = isProviderAvailable(LocationManager.GPS_PROVIDER);
                    networkAvailable = isProviderAvailable(LocationManager.NETWORK_PROVIDER);
                }
            }
        };
        context().getApplicationContext().registerReceiver(providerChangedReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        isRegistered = true;
        maxSpeed = 0.0f;
    }

    /**
     * Confirms the right to use the fine location permission and that at least one of the providers is enabled/available
     *
     * @return whether this detector is enabled
     */
    @Override
    public boolean isEnabled() {
        return locationManager != null && (providers != null && !providers.isEmpty());
    }

    /**
     * Confirms the existence of a given provider
     * @param provider the provider to check for
     * @return true if provider is found, false if not
     */
    public boolean isProviderEnabled(String provider) {
        return locationManager != null && locationManager.getAllProviders().contains(provider);
    }

    /**
     * Confirms whether use has allowed to use a specific provider
     * @param provider the provider to check for
     * @return true if provider is permitted and available, false if not
     */
    public boolean isProviderAvailable(String provider) {
        boolean permission = isPermitted();
        switch (provider) {
            case LocationManager.GPS_PROVIDER:
                return permission && gpsAvailable;
            case LocationManager.NETWORK_PROVIDER:
                return permission && networkAvailable;
            default:
                Log.d(tag, "Unknown provider:" + provider);
                return false;
        }
    }

    @Override
    public void terminate() {
        if (isRegistered) {
            context().getApplicationContext().unregisterReceiver(providerChangedReceiver);
            isRegistered = false;
        }
        cancelScan();
        super.terminate();
    }

    private void cancelScan() {
        if (isPermitted()) {
            locationManager.removeUpdates(this);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void performScan() {
        Location lastLocation = lastLocation();
        // Only scan if enabled, a last location is missing or the lastlocation is at least 15 minutes since
        if (isEnabled()) {
            boolean canScan = false;
            if (lastLocation != null) {
                long timeLastLocation = lastLocation.getTime();
                long fifteenMinutesAgo = now() - 15*60*1000;
                if (timeLastLocation < fifteenMinutesAgo) {
                    canScan = true;
                } else {
                    onLocationChanged(lastLocation); // Using the last known location since it is relatively short time since its update
                }
            } else {
                canScan = true;
            }
            if (canScan && isPermitted()) {
                if (isProviderAvailable(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, Looper.getMainLooper());
                }
                if (isProviderAvailable(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, Looper.getMainLooper());
                }
            }
        } else {
            Log.w(tag, "Cannot scan for position, not enabled");
        }
    }

    /**
     * Finds the last recorded scanResult
     * @return the last known location or null
     */
    @SuppressWarnings("MissingPermission")
    private Location lastLocation() {
        if (locationManager != null) {
            if (isPermitted()) {
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation == null) {
                    lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                if (lastLocation != null) {
                    return lastLocation;
                }
            }
        }
        return null;
    }

    @Override
    public boolean startListening() {
        boolean listen = super.startListening();
        if (listen) {
            performScan();
        }
        return listen;
    }

    @Override
    public void onLocationChanged(Location location) {
        JsonArray array = new JsonArray();
        array.add(""+location.getTime());
        array.add(""+location.getLatitude());
        array.add(""+location.getLongitude());
        array.add(""+location.getAltitude());
        array.add(""+location.getAccuracy());
        array.add(location.getProvider());
        sessionValues.add(array);
        float speed = location.getSpeed();
        if (speed > maxSpeed) {
            maxSpeed = speed;
        }
        storeData();
    }

    @Override
    public void stopListening() {
        super.stopListening();
        cancelScan();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                switch (provider) {
                    case LocationManager.GPS_PROVIDER:
                        gpsAvailable = true;
                        break;
                    case LocationManager.NETWORK_PROVIDER:
                        networkAvailable = true;
                        break;
                    default:
                }
                break;
            case LocationProvider.OUT_OF_SERVICE:
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                switch (provider) {
                    case LocationManager.GPS_PROVIDER:
                        gpsAvailable = false;
                        break;
                    case LocationManager.NETWORK_PROVIDER:
                        networkAvailable = false;
                        break;
                    default:
                }
        }
        sensorStatusChanged();
    }

    @Override
    public void onProviderEnabled(String provider) {
        switch (provider) {
            case LocationManager.GPS_PROVIDER:
                gpsAvailable = true;
                break;
            case LocationManager.NETWORK_PROVIDER:
                networkAvailable = true;
                break;
            default:
        }
        sensorStatusChanged();
    }

    @Override
    public void onProviderDisabled(String provider) {
        switch (provider) {
            case LocationManager.GPS_PROVIDER:
                gpsAvailable = false;
                break;
            case LocationManager.NETWORK_PROVIDER:
                networkAvailable = false;
                break;
            default:
        }
        sensorStatusChanged();
    }

    @Override
    public int detectorType() {
        return DetectorType.Position;
    }

    @Override
    public String detectorName() {
        return "position";
    }

    @Override
    public boolean isAvailable() {
        return (gpsAvailable || networkAvailable) && super.isAvailable();
    }

    @Override
    public String requiredPermission() {
        return Manifest.permission.ACCESS_FINE_LOCATION;
    }
}
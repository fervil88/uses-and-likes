package com.harriague.curso.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Emi on 04/10/2016.
 */

public class GPSTrackerService extends Service implements LocationListener {
    private static final String TAG = "GPSTrackerService";
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_GPS_UPDATES = 10;
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_GPS_UPDATES = 1000 * 60 * 1;

    private final Context context;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    boolean canGetLocation = false;
    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    // Declaring a Location Manager
    protected LocationManager locationManager;

    public GPSTrackerService(Context context) {
        this.context = context;
    }


    public Location getLocation() {
        try {
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                   // Think about that
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    // Think about that
                }
            }

        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
        }

        return location;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
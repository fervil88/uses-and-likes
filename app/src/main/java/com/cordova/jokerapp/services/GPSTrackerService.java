package com.cordova.jokerapp.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

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
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);


            // Get location from Network Provider
            if (isNetworkEnabled) {
                this.canGetLocation = true;
                // Think about enabled permission
             /*   locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_GPS_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_GPS_UPDATES, this); */
                if (locationManager != null) {
                    // Think about enabled permission
                    /*  location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); */
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }

            }
            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                this.canGetLocation = true;
                if (location == null) {
                    // Think about enabled permission
                  /*  locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_GPS_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_GPS_UPDATES, this); */
                    Log.d("GPS Enabled", "GPS Enabled");
                    if (locationManager != null) {
                        // Think about enabled permission
                        /* location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); */
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
        }

        return location;
    }


    public void stopUsingGPS(){
        if(locationManager != null){
            // Think about enabled permission
            /*locationManager.removeUpdates(GPSTrackerService.this);*/
        }
    }

    public String getAddress() {
        String result = "";
        if (location != null && location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {
            Log.d(TAG, "loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0");
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Log.d(TAG, "!list.isEmpty()");
                    Address address = list.get(0);
                    result = address.getAddressLine(0);

                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return result;
    }

    public void setLocation(Location location) {

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
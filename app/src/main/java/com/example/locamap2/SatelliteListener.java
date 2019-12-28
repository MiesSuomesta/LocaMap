package com.example.locamap2;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class SatelliteListener implements LocationListener {

    private MapsActivity mMainUI;
    private double last_latitude;
    private double last_longitude;

    @Override
    public void onLocationChanged(Location location) {

        last_latitude =  (location.getLatitude());
        last_longitude = (location.getLongitude());

        mMainUI.iGPSFix = true;

        Log.i("Geo_Location", "Latitude: " + last_latitude + ", Longitude: " + last_longitude);
        mMainUI.GPSLocation_changed();
    }

    public LatLng getLastPoint() {
        if (mMainUI.iGPSFix)
            return new LatLng(last_latitude, last_longitude);
        else
            return null;
    }

    @Override
    public void onProviderDisabled(String provider) {
        mMainUI.iGPSFix = false;
        mMainUI.GPSLocation_changed();
    }

    @Override
    public void onProviderEnabled(String provider) {
        mMainUI.iGPSFix = true;
        mMainUI.GPSLocation_changed();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void setUiComponent(MapsActivity mapsAct) {
        mMainUI = mapsAct;
        mMainUI.iGPSFix = false;
    }
}

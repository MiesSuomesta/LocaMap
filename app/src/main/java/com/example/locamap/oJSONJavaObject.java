package com.example.locamap;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class oJSONJavaObject {


    public    String              mQuakeplace = "";
    public    String              mQuakeurl = "";
    public    String              mQuakemag = "";
    public    String              mQuaketime = "";
    public    double              mLongitude = 0;
    public    double              mLatitude = 0;
    public    double              mDeviceLongitude = 0;
    public    double              mDeviceLatitude = 0;

    public   Intent              mIntent = null;
    public    PendingIntent       mPendingIntent = null;
    public   IntentFilter        mIntentFilter = null;
    public    myProximityAlert    mProximityAlarm = null;
    public    Marker              mMarker = null;
    public    MarkerOptions       mMarkerOptions = null;
    public    GoogleMap           mMap = null;
    public    Intent              registeredRecvIntent = null;
    public    LocationManager     mLocman = null;
    public    Boolean             mPermitted = false;
    public    String              mTitle = "";
    public    String              mSnippet = "";
    public    DateFormat          dateFormatter = null;
    public    MapsActivity        mMyMapsActivity;


    public oJSONJavaObject(
             MapsActivity    mapsActivity,
             GoogleMap googleMap,
             String pquakeplace,
             String pquakeurl,
             String pquakemag,
             String pquaketime,
             double pLongitude,
             double pLatitude,
             boolean pPermitted,
             LocationManager pLocman)
     {
        // setup variables
         mMap = googleMap;
         mQuakeplace = pquakeplace;
         mQuakeurl = pquakeurl;
         mQuakemag = pquakemag;
         mQuaketime = pquaketime;
         mLongitude = pLongitude;
         mLatitude = pLatitude;
         mMyMapsActivity = mapsActivity;

         dateFormatter = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss.SSSXXX");
         dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

         // Setup intent tp alarm too near
         mLocman = pLocman;

         if ( pPermitted ) {
             mIntent = new Intent("com.example.locaman.myProximityAlert");
             mPendingIntent = PendingIntent.getBroadcast( mMyMapsActivity, 0, mIntent, 0);
             mLocman.addProximityAlert(mLatitude, mLongitude, 50000, -1, mPendingIntent);
             mIntentFilter = new IntentFilter("com.example.locaman.myProximityAlert");
             mProximityAlarm = new myProximityAlert();
             registeredRecvIntent =  mMyMapsActivity.registerReceiver(mProximityAlarm, mIntentFilter);
         }
     }

    public Marker getMarkerForQuake()
    {
        LatLng quakePoint = new LatLng(mLatitude, mLongitude);

        if ( mMarkerOptions == null )
        {
            mMarkerOptions = new MarkerOptions();
        }

        Date   datequaketime = new Date( Long.parseLong(mQuaketime) );
        String formattedDate = dateFormatter.format(datequaketime);
        mSnippet = "magnitude " + mQuakemag + " at " + formattedDate;

        mMarkerOptions.position(quakePoint);
        mMarkerOptions.title(mTitle);
        mMarkerOptions.snippet(mSnippet);

        mMarker = mMap.addMarker(mMarkerOptions);

        return mMarker;
    }

}

package com.example.locamap;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class oJSONJavaObject implements GoogleMap.OnMarkerClickListener
{


    public    String              mQuakeplace = "";
    public    String              mQuakeurl = "";
    public    String              mQuakemag = "";
    public    String              mQuaketime = "";
    public    double              mLongitude = 0;
    public    double              mLatitude = 0;
    public    double              mDeviceLongitude = 0;
    public    double              mDeviceLatitude = 0;

    public    Intent              mIntent = null;
    public    PendingIntent       mPendingIntent = null;
    public    IntentFilter        mIntentFilter = null;
    public    myProximityAlert    mProximityAlarm = null;
    public    Marker              mMarker = null;
    public    MarkerOptions       mMarkerOptions = null;
    public    GoogleMap           mMap = null;
    public    Intent              registeredRecvIntent = null;
    public    LocationManager     mLocman = null;
    public    Boolean             mPermitted = false;
    public    String              mTitle = "";
    public    String              mSnippet = "";
    public    String              mSnippetExtra = "";
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

         setmTitle(pquakeplace);

         mMap.setOnMarkerClickListener(this);

     }

        public double distance(double lat1,
                               double lon1, double lat2,
                               double lon2)
        {

            // The math module contains a function
            // named toRadians which converts from
            // degrees to radians.
            lon1 = Math.toRadians(lon1);
            lon2 = Math.toRadians(lon2);
            lat1 = Math.toRadians(lat1);
            lat2 = Math.toRadians(lat2);

            // Haversine formula
            double dlon = lon2 - lon1;
            double dlat = lat2 - lat1;
            double a = Math.pow(Math.sin(dlat / 2), 2)
                    + Math.cos(lat1) * Math.cos(lat2)
                    * Math.pow(Math.sin(dlon / 2),2);

            double c = 2 * Math.asin(Math.sqrt(a));

            // Radius of earth in kilometers. Use 3956
            // for miles
            double r = 6371;

            // calculate the result
            return(c * r);
        }

    public double getMarkerDistanceToDevice(LatLng pLatLngDevice)
    {
        double ret = 0;
        if (pLatLngDevice != null) {
            double devLat = pLatLngDevice.latitude;
            double devLng = pLatLngDevice.longitude;

            ret = this.distance(mLatitude, mLongitude, devLat, devLng);
        }

        return ret;
    }

    public void setmQuakeplace(String mQuakeplace) {
        this.mQuakeplace = mQuakeplace;
    }

    public void setmQuakeurl(String mQuakeurl) {
        this.mQuakeurl = mQuakeurl;
    }

    public void setmQuakemag(String mQuakemag) {
        this.mQuakemag = mQuakemag;
    }

    public void setmQuaketime(String mQuaketime) {
        this.mQuaketime = mQuaketime;
    }

    public void setmLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }

    public void setmLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public void setmMap(GoogleMap mMap) {
        this.mMap = mMap;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setmSnippet(String mSnippet) {
        this.mSnippet = mSnippet;
    }

    public void setmSnippetExtra(String mSnippetExtra) {
        this.mSnippetExtra = mSnippetExtra;
    }

    public void setDateFormatter(DateFormat dateFormatter) {
        this.dateFormatter = dateFormatter;
    }

    public String getmQuakeplace() {
        return mQuakeplace;
    }

    public String getmQuakeurl() {
        return mQuakeurl;
    }

    public String getmQuakemag() {
        return mQuakemag;
    }

    public String getmQuaketime() {
        return mQuaketime;
    }

    public double getmLongitude() {
        return mLongitude;
    }

    public double getmLatitude() {
        return mLatitude;
    }

    public double getmDeviceLongitude() {
        return mDeviceLongitude;
    }

    public double getmDeviceLatitude() {
        return mDeviceLatitude;
    }

    public GoogleMap getmMap() {
        return mMap;
    }

    public LocationManager getmLocman() {
        return mLocman;
    }

    public Boolean getmPermitted() {
        return mPermitted;
    }

    public String getmTitle() {
        return mTitle;
    }

    public String getmSnippet() {
        return mSnippet;
    }

    public String getmSnippetExtra() {
        return mSnippetExtra;
    }

    public MapsActivity getmMyMapsActivity() {
        return mMyMapsActivity;
    }

    public Marker getmMarker() {
        return mMarker;
    }
    public Marker setmMarker(Marker foo) {
        mMarker = foo;
        return mMarker;
    }

    public Marker getMarkerForQuake()
    {
        LatLng quakePoint = new LatLng(mLatitude, mLongitude);

        setmMarker(null);

        if ( mMarkerOptions == null )
        {
            mMarkerOptions = new MarkerOptions();
        }

        Date   datequaketime = new Date( Long.parseLong(getmQuaketime()) );
        String formattedDate = dateFormatter.format(datequaketime);
        setmSnippet("magnitude " + mQuakemag + " at " + formattedDate);

        mMarkerOptions.position(quakePoint);
        mMarkerOptions.title(getmTitle());
        mMarkerOptions.snippet(getmSnippet() + getmSnippetExtra());

        return setmMarker(mMap.addMarker(mMarkerOptions));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        String Extra = getmSnippetExtra();
        String UIStr = marker.getTitle() + "\n" + marker.getSnippet() + Extra;
        Toast.makeText(getmMyMapsActivity(), UIStr, Toast.LENGTH_LONG).show();

        return false;
    }
}

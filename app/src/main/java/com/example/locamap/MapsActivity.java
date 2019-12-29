package com.example.locamap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback {

    private GoogleMap mMap;
    public LocationManager locationManager = null;
    Boolean PermissionsAndLocmanOK = false;
    private Boolean iMapShowed = false;
    public Boolean iGPSFix = false;
    private SatelliteListener mGnssListener;

    private ArrayList<oJSONJavaObject> maJavaObjects = null;

    private LatLng lastGpsPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mGnssListener = new SatelliteListener();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 1, mGnssListener);

        PermissionsAndLocmanOK =
                ((checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                        (locationManager != null));

        mGnssListener.setUiComponent(this);

        maJavaObjects = new ArrayList<oJSONJavaObject>();

        /*Ugly hack .. properway would be to use asynctask */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        this.fillGoogleMap(mMap);
        iMapShowed = true;
    }

    public void deleteArrayObjects()
    {

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void fillGoogleMap(GoogleMap googleMap) {

        JSONArray featuresArr = null;

        String jsonurl =
                "https://prod-earthquake.cr.usgs.gov/earthquakes/feed/v1.0/summary/all_day.geojson";

        oJsonToObject jsonToJavaObj = new oJsonToObject();
        JSONObject quakejsonobj = jsonToJavaObj.jsonObjectFromUrl(jsonurl);

        mMap.setMyLocationEnabled(true);
        maJavaObjects.clear();

        try {
            featuresArr = quakejsonobj.getJSONArray("features");
            int alen = featuresArr.length();

            for (int i = 0; i < alen; i++) {

                JSONObject featureObj = featuresArr.getJSONObject(i);

                oJSONJavaObject jObj = jsonToJavaObj.jsonObjectToJavaObject(this, mMap,
                        featureObj,PermissionsAndLocmanOK,locationManager);

                jObj.getMarkerForQuake();

                maJavaObjects.add(jObj);

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        featuresArr = null;
        quakejsonobj = null;
        jsonToJavaObj = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void refillGoogleMap(GoogleMap googleMap) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);


        mMap.setMyLocationEnabled(true);

        try {
            for (oJSONJavaObject tmp : maJavaObjects)
            {
                double dist = tmp.getMarkerDistanceToDevice(lastGpsPoint);

                tmp.setmSnippetExtra("\n" + df.format(dist) + " km away");

                tmp.getMarkerForQuake();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void logText(String tag, String message, int color) {
        String composedTag = "MainActivity." + tag;
        Log.i(composedTag, message);
    }

    public void logUserLocation()
    {

        lastGpsPoint = null;
        lastGpsPoint = mGnssListener.getLastPoint();

        this.refillGoogleMap(mMap);
    }

    public void GPSLocation_changed() {
        if ( ! iMapShowed )
            return;

        if (! iGPSFix )
            return;

        logUserLocation();
    }
}

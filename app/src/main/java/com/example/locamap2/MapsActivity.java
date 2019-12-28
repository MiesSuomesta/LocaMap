package com.example.locamap2;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static android.util.Log.d;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback{

    private GoogleMap mMap;
    private MarkerOptions mapMarkOptionsGPS = null;
    private Marker mapMarkGPS = null;
    private Boolean iMapShowed = false;
    public Boolean iGPSFix = false;
    private SatelliteListener mGnssListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mGnssListener = new SatelliteListener();

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mGnssListener);

        mGnssListener.setUiComponent(this);

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

    public JSONObject dl_quake_json() {
        JSONObject retobj = null;
        try {

            URL dlurl = new URL("https://prod-earthquake.cr.usgs.gov/earthquakes/feed/v1.0/summary/all_day.geojson");
            URLConnection dlurlconn = dlurl.openConnection();
            dlurlconn.connect();

            InputStream iStream = dlurlconn.getInputStream();
            BufferedReader iStreamReader = new BufferedReader(new InputStreamReader(iStream, StandardCharsets.UTF_8));
            StringBuilder responseStrBuilder = new StringBuilder();
            String inputStr;
            while ((inputStr = iStreamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
            retobj = new JSONObject(responseStrBuilder.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retobj;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void fillGoogleMap(GoogleMap googleMap) {
        JSONObject quakejsonobj = dl_quake_json();

        JSONArray featuresArr = null;

        DateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss.SSSXXX");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            featuresArr = quakejsonobj.getJSONArray("features");
            LocationManager locman = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            Boolean PermissionsAndLocmanOK =
                    ((checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                            (locman != null));

            for (int i = 0; i < featuresArr.length(); i++) {

                JSONObject featureObj = featuresArr.getJSONObject(i);
                JSONObject featurePropObj = featureObj.optJSONObject("properties");
                JSONArray featureLocObj = featureObj.optJSONObject("geometry").getJSONArray("coordinates");
                String quakeplace = featurePropObj.getString("place");
                String quakeurl = featurePropObj.getString("url");
                String quakemag = featurePropObj.getString("mag");
                String quaketime = featurePropObj.getString("time");
                double Longitude = featureLocObj.getDouble(0);
                double Latitude = featureLocObj.getDouble(1);

                if ( PermissionsAndLocmanOK )
                {
                    Intent intent = new Intent("com.example.quakemap.ProximityAlert");
                    PendingIntent proxIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
                    locman.addProximityAlert(Latitude, Longitude, 50000, -1, proxIntent);
                    IntentFilter filter = new IntentFilter("com.example.quakemap.ProximityAlert");
                    registerReceiver(new myProximityAlert(), filter);
                }

                Date datequaketime = new Date( Long.parseLong(quaketime) );
                String formattedDate = dateFormatter.format(datequaketime);
                LatLng quakePoint = new LatLng(Latitude, Longitude);
                String snippetStr = "magnitude " + quakemag + " at " + formattedDate;

                Marker mapMark = getMarkerQuakeFor(Latitude, Longitude, quakeplace , snippetStr);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void logText(String tag, String message, int color) {
        String composedTag = "MainActivity." + tag;
        Log.i(composedTag, message);
    }

    private class myProximityAlert extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            Boolean getting_closer = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false);
            if (getting_closer)
                d("Radius", "Hey, you just entered 50km from earthquake!");
            else
                d("Radius", "Hey, you just exit earthquake area!");
        }
    }

    public Marker getMarkerQuakeFor(double lat, double lon, String title, String snippet)
    {
        LatLng quakePoint = new LatLng(lat, lon);
        MarkerOptions quakeMarkerops = new MarkerOptions();
        quakeMarkerops.position(quakePoint);
        quakeMarkerops.title(title);
        quakeMarkerops.snippet(snippet);

        return mMap.addMarker(quakeMarkerops);
    }

    public Marker getMarkerSelfFor()
    {

        LatLng gpsPoint = mGnssListener.getLastPoint();
        MarkerOptions mOptionsGPS = null;

        if (mapMarkGPS != null)
        {
            mapMarkGPS = null;

        }

        if (mapMarkOptionsGPS != null)
        {
            mapMarkOptionsGPS = null;
        }

        mapMarkOptionsGPS = new MarkerOptions();
        mapMarkOptionsGPS = mapMarkOptionsGPS;

        mapMarkOptionsGPS.position(gpsPoint);
        mapMarkOptionsGPS.title("Device GPS location");
        mapMarkOptionsGPS.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

        mapMarkGPS = mMap.addMarker(mapMarkOptionsGPS);

        return mapMarkGPS;
    }

    public void GPSLocation_changed() {
        if ( ! iMapShowed )
            return;

        if (! iGPSFix )
            return;

        getMarkerSelfFor();
    }
}

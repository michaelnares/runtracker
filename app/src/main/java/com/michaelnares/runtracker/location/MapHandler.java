package com.michaelnares.runtracker.location;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.michaelnares.runtracker.R;
import com.michaelnares.runtracker.data.RunPoint;
import com.michaelnares.runtracker.utils.Constants;
import com.michaelnares.runtracker.utils.Utils;

import java.util.ArrayList;

public class MapHandler implements OnMapReadyCallback {
    private Location location;
    private ArrayList<RunPoint> runPointsList;
    private AppCompatActivity appCompatActivity;

    public MapHandler(Location location, ArrayList<RunPoint> runPointsList, AppCompatActivity appCompatActivity) {
        this.location = location;
        this.runPointsList = runPointsList;
        this.appCompatActivity = appCompatActivity;
    }

    public final void populateMap() {
        final SupportMapFragment mapFragment = (SupportMapFragment) appCompatActivity.getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    // It can be suppressed because the permissions check happens elsewhere.
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        googleMap.setMyLocationEnabled(true);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15.0f)); // 2 is the minimum zoom, 21 is the maximum.
        Log.d(Utils.getPackageName(appCompatActivity.getApplicationContext()),
                "onMapReady called with run points list size of " + runPointsList.size());
        if (runPointsList != null && runPointsList.size() > 0) {
            final LatLngBounds.Builder latLngBoundsBuilder = new LatLngBounds.Builder();
            for (LatLng latLng : getLatLngsList()) {
                latLngBoundsBuilder.include(latLng); // Including all the points within the area.
            } // The foreach loop ends here
            final LatLngBounds bounds = latLngBoundsBuilder.build();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, Constants.MAP_OFFSET_PIXELS));
            final PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.addAll(getLatLngsList());
            polylineOptions
                    .width(5)
                    .color(Color.RED);
            googleMap.addPolyline(polylineOptions);
        } // The if block ends here.
    } // The onMapReady() method ends here.

    private ArrayList<LatLng> getLatLngsList() {
        final ArrayList<LatLng> latLngsList = new ArrayList<>();
        for (RunPoint runPoint : runPointsList) {
            latLngsList.add(new LatLng(runPoint.getLatitude(), runPoint.getLongitude()));
        }
        return latLngsList;
    }
} // The MapHandler class ends here.

package com.michaelnares.runtracker.utils;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.michaelnares.runtracker.R;
import com.michaelnares.runtracker.data.RunPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Utils {
    public static final void generateSingleButtonAlert(final AppCompatActivity activity, final int stringID) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity).
                        setTitle(getStringFromXML(activity, R.string.alert)).setMessage(getStringFromXML(activity, stringID))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialogBuilder.create().show();
            }
        });
    }

    private static final String getStringFromXML(final AppCompatActivity activity, final int stringID) // To get a string reference from the strings file.
    {
        return activity.getApplicationContext().getResources().getString(stringID);
    }

    public static final String[] getPermissions() {
        final String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        return permissions;
    }

    public static final boolean allPermissionsEnabled(final Context context, final String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.d(getPackageName(context), "Permission not granted for " + permission);
                return false;
            }
        }
        return true;
    }

    public static final boolean hasGPS(final Context context) {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static final boolean hasInternet(final Context context) {
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static final String getPackageName(final Context context) {
        return context.getPackageName(); // Returning the package name of the application.
    }

    public static final double getSpeed(final RunPoint firstRunPoint, final RunPoint secondRunPoint) {
        final double time = (secondRunPoint.getTimestamp() / 1000) - (firstRunPoint.getTimestamp() / 1000);
        final double distance = getDistance(firstRunPoint.getLatitude(), firstRunPoint.getLongitude(), secondRunPoint.getLatitude(), secondRunPoint.getLongitude());
        return distance / time * 2.236936; // The value is to convert mps to mph.
    }

    private static double getDistance(double firstLat, double firstLon, double secondLat, double secondLon) { // https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude
        // 1.1515 to convert from nm to miles
        final double latDifference = firstLat = secondLat;
        final double lonDifference = firstLon - secondLon;
        final double sinMultiple = Math.sin(deg2Rad(firstLat)) * Math.sin(deg2Rad(secondLat));
        final double cosMultiple = Math.cos(deg2Rad(firstLat)) * Math.cos(deg2Rad(secondLat)) * Math.cos(deg2Rad(lonDifference));
        ;
        double distance = sinMultiple + cosMultiple;
        distance = Math.acos(distance);
        distance = distance * 60 * 1.1515; // These numbers are required to convert the value into miles.  1.1515 is the required value to convert from nautical miles into miles.
        return distance * 1609.344; // Distance in m.
    }

    private static double deg2Rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    public static String getHumanReadableTime(final long timestamp) {
        // Takes a date from the timestamp and formats it.
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(timestamp));
    }

    public static String getStringFromXML(final Context context, final int id) {
        return context.getResources().getString(id);
    }

    public static ArrayList<RunPoint> generateRunPointsListFromRunAsString(final String runAsString) throws JSONException {
        final ArrayList<RunPoint> result = new ArrayList<>();
        final JSONObject runObject = new JSONObject(runAsString);
        final JSONArray runPointsArray = runObject.getJSONArray("points");
        for (int i = 0; i < runPointsArray.length(); i++) {
            final JSONObject runPointAsJson = runPointsArray.getJSONObject(i);
            final double latitude = runPointAsJson.getDouble("lat");
            final double longitude = runPointAsJson.getDouble("lon");
            final long timestamp = runPointAsJson.getLong("timestamp");
            final RunPoint runPoint = new RunPoint(latitude, longitude, timestamp);
            result.add(runPoint);
        } // The for loop finishes here.
        return result;
    }
}

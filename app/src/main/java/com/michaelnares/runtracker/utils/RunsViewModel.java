package com.michaelnares.runtracker.utils;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.michaelnares.runtracker.R;
import com.michaelnares.runtracker.data.RunPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class RunsViewModel extends ViewModel {
    private String CAN_TRACK_LOCATION = "canTrackLocation";
    private String RUNS_FILE_PATH = "runs.json";
    private String CURRENT_RUN = "currentRun";
    private String CURRENT_LATITUDE = "currentLatitude";
    private String CURRENT_LONGITUDE = "currentLongitude";
    private String SELECTED_RUN = "selectedRun";

    private AppCompatActivity actvity;
    private Context context;


    public RunsViewModel(Context context) {
        this.context = context;
    }

    public final boolean canTrackLocation() { // To check whether to track a user's location or not.
        // The contains key method is necessary because if it is checked without containing the key, a crash occurs.
        if (getSharedPreferences().contains(CAN_TRACK_LOCATION)) {
            return getSharedPreferences().getBoolean(CAN_TRACK_LOCATION, true);
        } else {
            return false;
        }
    }

    public final void setCanTrackLocation(final boolean canTrackLocation) {
        getSharedPreferences().edit().putBoolean(CAN_TRACK_LOCATION, canTrackLocation).apply();
    }

    private final SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(Utils.getPackageName(context), Context.MODE_PRIVATE);
    }

    public final void setActivity(AppCompatActivity activity) {
        this.actvity = activity;
    }

    public final void setContext(Context context) {
        this.context = context;
    }

    // To return the runs file path, the path of the file within the phone's storage that has the runs, stored in JSON format.
    private final String getRunsPath() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        buffer.append("/");
        buffer.append(RUNS_FILE_PATH);
        return buffer.toString(); // Using a StringBuffer because it's more efficient than creating separate string objects.
    }

    // Gets the start and end time for each run as a string, e.g. 28/09/2019 09:34 - 28/09/2019 10:48.
    public ArrayList<String> getStartAndEndTimes() throws IOException, JSONException {
        final ArrayList<String> runsList = new ArrayList<>();
        final String runsAsString = getRunsAsString();
        if (runsAsString == null || runsAsString.equals("")) {
            Utils.generateSingleButtonAlert(actvity, R.string.runs_not_found);
            return null;
        } else {
            final JSONArray runsAsJsonArray = new JSONArray(runsAsString);
            // Looping through the for loop to get the start and end time.
            for (int i = 0; i < runsAsJsonArray.length(); i++) {
                final JSONObject runObject = runsAsJsonArray.getJSONObject(i);
                final long start = runObject.getLong("start");
                final long end = runObject.getLong("end");
                final String startAsString = Utils.getHumanReadableTime(start);
                final String endAsString = Utils.getHumanReadableTime(end);
                final StringBuffer buffer = new StringBuffer();
                buffer.append(startAsString);
                buffer.append(" - ");
                buffer.append(endAsString);
                runsList.add(buffer.toString()); // Using a StringBuffer rather then creating a new String each time, as it's more efficient.
            }
            return runsList;
        }
    }

    public final Location getLatestLocation() {
        final Location location = new Location("currentUserLocation");
        final double latitude = getDoubleFromPreferences(CURRENT_LATITUDE);
        final double longitude = getDoubleFromPreferences(CURRENT_LONGITUDE);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    public final void saveLatestLocation(final Location location) {
        setDoubleToPreferences(CURRENT_LATITUDE, location.getLatitude());
        setDoubleToPreferences(CURRENT_LONGITUDE, location.getLongitude());
    }

    private final double getDoubleFromPreferences(final String key) {
        final SharedPreferences sharedPreferences = getSharedPreferences();
        return Double.longBitsToDouble(sharedPreferences.getLong(key, Double.doubleToLongBits(Constants.DEFAULT_LATITUDE_AND_LONGITUDE)));

    }

    // Casting a double to a float can result in overflow and underflow - ref https://stackoverflow.com/questions/16319237/cant-put-double-sharedpreferences
    private final void setDoubleToPreferences(final String key, final double value) {
        getSharedPreferences().edit().putFloat(key, Double.doubleToRawLongBits(value)).apply(); // Apply is asynchronous, commit is not.
    }

    @SuppressLint("LongLogTag")
    public final void saveCurrentRun() throws JSONException, IOException {
        final ArrayList<RunPoint> runPointsList = getCurrentRun();
        logRunPointsList(runPointsList);
        if (runPointsList == null || runPointsList.size() == 0) {
            return; // Do nothing for an empty array.
        }
        final JSONObject runObject = new JSONObject();
        runObject.put("start", runPointsList.get(0).getTimestamp());
        runObject.put("end", runPointsList.get(runPointsList.size() - 1).getTimestamp());
        final JSONArray pointsArray = new JSONArray();
        for (RunPoint runPoint : runPointsList) {
            final JSONObject pointsObject = new JSONObject();
            pointsObject.put("latitude", runPoint.getLatitude());
            pointsObject.put("longitude", runPoint.getLongitude());
            pointsObject.put("timestamp", runPoint.getTimestamp());
            pointsArray.put(pointsObject);
        }
        runObject.put("points", pointsArray);

        final File runsFile = new File(getRunsPath());
        if (!runsFile.exists()) {
            runsFile.createNewFile();
        }
        JSONArray runsArray = null;
        final String runs = getRunsAsString();
        if (runs.equals("")) {
            runsArray = new JSONArray();
        } else {
            runsArray = new JSONArray(runs);
        }
        runsArray.put(runObject);
        final BufferedWriter writer = new BufferedWriter(new FileWriter(getRunsPath(), false));
        writer.write(runsArray.toString());
        writer.close();
        clearCurrentRun();
    }

    @SuppressLint("LongLogTag")
    private final void logRunPointsList(final ArrayList<RunPoint> runPointsList) {
        for (RunPoint runPoint : runPointsList) {
            Log.d(Utils.getPackageName(context), runPoint.toString());
        }
    }

    @SuppressLint("LongLogTag")
    public final String getRunsAsString() throws IOException {
        String line;
        final StringBuffer buffer = new StringBuffer();
        final File runsFile = new File(getRunsPath());
        if (!runsFile.exists()) {
            return null;
        }
        final FileInputStream fileInputStream = new FileInputStream(runsFile);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
            buffer.append(System.getProperty("line.separator"));
        }
        reader.close();
        return buffer.toString(); // Using a StringBuffer because it's more efficient then creating separate String objects.
    }

    public final ArrayList<RunPoint> getCurrentRun() throws JSONException {
        final String currentrunAsString = getSharedPreferences().getString(CURRENT_RUN, null);
        if (currentrunAsString == null) {
            Log.e(Utils.getPackageName(actvity.getApplicationContext()), "Current run returning null at this point.");
            return null;
        }
        final JSONObject currentrunAsJSON = new JSONObject(currentrunAsString);
        final ArrayList<RunPoint> result = new ArrayList<>();
        final JSONArray pointsArray = currentrunAsJSON.getJSONArray("points");
        for (int i = 0; i < pointsArray.length(); i++) {
            final JSONObject pointObject = pointsArray.getJSONObject(0);
            final double lat = pointObject.getDouble("lat");
            final double lon = pointObject.getDouble("lon");
            final long timestamp = pointObject.getLong("timestamp");
            final RunPoint runPoint = new RunPoint(lat, lon, timestamp);
            result.add(runPoint);
        }
        return result;
    }

    public final void clearCurrentRun() {
        getSharedPreferences().edit().putString(CURRENT_RUN, null).apply();
    }

    // Appends it to the current run.
    public final void appendRunPoint(final RunPoint runPoint) throws JSONException {
        final String appPackage = Utils.getPackageName(actvity.getApplicationContext());
        Log.d(Utils.getPackageName(actvity.getApplicationContext()), "appendrunPoint() method called");
        ArrayList<RunPoint> currentRun = getCurrentRun();
        if (currentRun == null) {
            currentRun = new ArrayList<>();
        }
        currentRun.add(runPoint);
        Log.d(appPackage, "run point = " + runPoint);
        Log.d(appPackage, "run points list length = " + currentRun.size());
        setCurrentRun(currentRun);
    }

    public final void setCurrentRun(final ArrayList<RunPoint> currentRun) throws JSONException {
        getSharedPreferences().edit().putString(CURRENT_RUN, runsPointsListToJSONObject(currentRun).toString()).apply();
    }

    private final JSONObject runsPointsListToJSONObject(final ArrayList<RunPoint> runPointsList) throws JSONException {
        final JSONObject runPointsObject = new JSONObject();
        runPointsObject.put("start", runPointsList.get(0).getTimestamp());
        runPointsObject.put("end", runPointsList.get(runPointsList.size() - 1).getTimestamp());
        final JSONArray pointsArray = new JSONArray();
        for (RunPoint runPoint : runPointsList) {
            final JSONObject locationObject = new JSONObject();
            locationObject.put("lat", runPoint.getLatitude());
            locationObject.put("lon", runPoint.getLongitude());
            locationObject.put("timestamp", runPoint.getTimestamp());
            pointsArray.put(locationObject);
        } // The for each loop ends here.
        runPointsObject.put("points", pointsArray);
        return runPointsObject;
    }

    public final String getSelectedRun() {
        return getSharedPreferences().getString(SELECTED_RUN, null);
    }

    public final void setSelectedRun(final String selectedRun) {
        getSharedPreferences().edit().putString(SELECTED_RUN, selectedRun).apply(); // apply() is asynchronous, commit is not
    }
}

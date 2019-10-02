package com.michaelnares.runtracker.async;

import android.arch.lifecycle.ViewModelProviders;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.michaelnares.runtracker.data.RunPoint;
import com.michaelnares.runtracker.location.MapHandler;
import com.michaelnares.runtracker.utils.RunsViewModel;
import com.michaelnares.runtracker.utils.Utils;

import org.json.JSONException;

import java.io.IOException;

public class UpdateMapThread extends Thread {
    private AppCompatActivity activity;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private RunsViewModel runsViewModel;

    public UpdateMapThread(AppCompatActivity activity) {
        this.activity = activity;
        runsViewModel = ViewModelProviders.of(activity).get(RunsViewModel.class);
    }

    @Override
    public void run() {
        while (true) {
            runsViewModel.setActivity(activity);
            if (runsViewModel.canTrackLocation()) {

                final Location location = runsViewModel.getLatestLocation();
                try {
                    runsViewModel.appendRunPoint(new RunPoint(location.getLatitude(), location.getLongitude(), System.currentTimeMillis()));
                } catch (JSONException e) {
                    Log.e(Utils.getPackageName(activity.getApplicationContext()), "A JSONException occurred whilst trying to add a run point.");
                    e.printStackTrace();
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new MapHandler(location, runsViewModel.getCurrentRun(), activity).populateMap();
                        } catch (JSONException e) {
                            Log.e(Utils.getPackageName(activity.getApplicationContext()), "A JSONException occurred whilst trying to use the MapHandler.");
                            e.printStackTrace();
                        }
                    }
                });
            }// ends the if block
            else { // What's going on here is when the user decides to stop tracking the location, it's treated as a new run, so the current run is saved, and then cleared.
                if (runsViewModel == null) {
                    runsViewModel = ViewModelProviders.of(activity).get(RunsViewModel.class);
                    try {
                        // Saving the runPointsList to the local filesystem, and creating a new one, as if a user wants to stop tracking, that is the end of a run.
                        if (runsViewModel.getCurrentRun().size() > 0) {
                            runsViewModel.saveCurrentRun();
                        }
                    } catch (JSONException e) {
                        Log.e(Utils.getPackageName(activity), "There was a JSONException whilst trying to save the run");
                        e.printStackTrace();
                    } catch (IOException e) {
                        Log.e(Utils.getPackageName(activity), "There was an IOException whilst trying to save the run");
                        e.printStackTrace();
                    }
                    runsViewModel.clearCurrentRun();
                }
            } // The else block ends here, so the else block for if the checkbox is not checked.
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } // ends the while(true) code
    } // ends the run() code
} // ends the class
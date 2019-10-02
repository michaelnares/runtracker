package com.michaelnares.runtracker.utils;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.michaelnares.runtracker.R;
import com.michaelnares.runtracker.async.UpdateLocationThread;
import com.michaelnares.runtracker.async.UpdateMapThread;
import com.michaelnares.runtracker.async.UpdateSpeedThread;

public class PermissionsHandler implements  ActivityCompat.OnRequestPermissionsResultCallback
{
    private RunsViewModel runsViewModel;
    private AppCompatActivity activity;
    private Context context;
    private int responseCode = 101; // The response code for onRequestPermissionsGranted().

    public PermissionsHandler(final AppCompatActivity activity, final Context initialContext)
    {
        this.activity = activity;
        this.context = context;
        runsViewModel = ViewModelProviders.of(activity).get(RunsViewModel.class);
    }

    public final void checkForPermissions()
    {
        if (!Utils.allPermissionsEnabled(context, Utils.getPermissions()))
        {
            ActivityCompat.requestPermissions(activity, Utils.getPermissions(), responseCode); // Request all the necessary permissions.
        }
        else
        {
            startTrackingRuns();
        }
    }

    public final boolean allConditionsMet()
    {
        if (!Utils.allPermissionsEnabled(context, Utils.getPermissions()))
        {
            return false; // I.e. I don't want it checking for GPS and internet without the necessary permissions having been granted.
        }
        if (!Utils.hasInternet(context) && !Utils.hasGPS(context))
        {
            Utils.generateSingleButtonAlert(activity, R.string.gps_and_internet_not_found);
        }
        return true;
    }

    public final void startTrackingRuns()
    {
        if (runsViewModel.canTrackLocation())
        {
            final UpdateLocationThread updateLocationThread = new UpdateLocationThread(activity, runsViewModel);
            updateLocationThread.start();
            final UpdateMapThread updateMapThread = new UpdateMapThread(activity);
            updateMapThread.start();
            final UpdateSpeedThread updateSpeedThread = new UpdateSpeedThread(activity);
            updateSpeedThread.start();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        if (Utils.allPermissionsEnabled(context, permissions) && allConditionsMet() && requestCode == responseCode)
        {
            startTrackingRuns();
        }
    }
}

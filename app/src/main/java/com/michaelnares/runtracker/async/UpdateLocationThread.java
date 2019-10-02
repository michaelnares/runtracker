package com.michaelnares.runtracker.async;

import android.annotation.SuppressLint;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.michaelnares.runtracker.utils.RunsViewModel;
import com.michaelnares.runtracker.utils.Utils;

public class UpdateLocationThread extends Thread {
    private AppCompatActivity activity;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private RunsViewModel runsViewModel;

    public UpdateLocationThread(AppCompatActivity activity, RunsViewModel runsViewModel) {
        this.activity = activity;
        this.runsViewModel = runsViewModel;
    }

    @SuppressLint("MissingPermission")
    // Because this method is only started when the required permissions are enabled.
    @Override
    public void run() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        final LocationRequest locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER).setInterval(1000).setFastestInterval(1000);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, null); // The second argument is for a PendingIntent, which we don't need.
        while (true) {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    final Location location = task.getResult();
                    runsViewModel.saveLatestLocation(location);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.e(Utils.getPackageName(activity.getApplicationContext()), "There was an InterruptedException during the run() method of the UpdateLocationThread class");
                        e.printStackTrace();
                    }
                } // The onComplete() method ends here.
            }); // The addOnCompleteListener() method ends here.
        } // The while loop ends here.
    } // The run method ends here.
} // The class ends here.



package com.michaelnares.runtracker.async;

import android.arch.lifecycle.ViewModelProviders;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.michaelnares.runtracker.R;
import com.michaelnares.runtracker.data.RunPoint;
import com.michaelnares.runtracker.utils.RunsViewModel;
import com.michaelnares.runtracker.utils.Utils;

public class UpdateSpeedThread extends Thread {
    private AppCompatActivity activity;
    private RunsViewModel runsViewModel;

    public UpdateSpeedThread(AppCompatActivity activity) {
        this.activity = activity;
    }

    @Override
    public void run() {

        while (true) {
            runsViewModel = ViewModelProviders.of(activity).get(RunsViewModel.class);
            final Location firstLocation = runsViewModel.getLatestLocation();
            final RunPoint firstRunPoint = new RunPoint(firstLocation.getLatitude(), firstLocation.getLongitude(), System.currentTimeMillis());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.e(Utils.getPackageName(activity.getApplicationContext()), "There was an InterruptedException during the UpdateSpeedThread method.");
                e.printStackTrace();
            }
            final Location secondLocation = runsViewModel.getLatestLocation();
            final RunPoint secondRunPoint = new RunPoint(secondLocation.getLatitude(), secondLocation.getLongitude(), System.currentTimeMillis());
            final double speed = Utils.getSpeed(firstRunPoint, secondRunPoint);

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final TextView speedTextView = activity.findViewById(R.id.currentSpeedText);
                    final String speedEquals = Utils.getStringFromXML(activity.getApplicationContext(), R.string.speed_equals);
                    final StringBuffer speedStringBuffer = new StringBuffer();
                    speedStringBuffer.append(speedEquals);
                    speedStringBuffer.append(" ");
                    speedStringBuffer.append(speed);
                    speedStringBuffer.append(" mph");
                    speedTextView.setText(speedStringBuffer.toString());
                }
            });
        } // The while loop ends here.
    } // The run() method ends here.
} // The class ends here.

package com.michaelnares.runtracker.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.michaelnares.runtracker.data.RunPoint;
import com.michaelnares.runtracker.location.MapHandler;
import com.michaelnares.runtracker.utils.RunsViewModel;
import com.michaelnares.runtracker.utils.Utils;

import org.json.JSONException;

import java.util.ArrayList;

public class IndividualRunActivity extends AppCompatActivity {
    private RunsViewModel runsViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        runsViewModel = ViewModelProviders.of(this).get(RunsViewModel.class);
        try {
            final ArrayList<RunPoint> selectedRun = Utils.generateRunPointsListFromRunAsString(runsViewModel.getSelectedRun());
            new MapHandler(runsViewModel.getLatestLocation(), selectedRun, this).populateMap();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

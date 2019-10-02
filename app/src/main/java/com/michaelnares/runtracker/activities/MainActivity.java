package com.michaelnares.runtracker.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.michaelnares.runtracker.R;
import com.michaelnares.runtracker.data.RunPoint;
import com.michaelnares.runtracker.utils.PermissionsHandler;
import com.michaelnares.runtracker.utils.RunsViewModel;
import com.michaelnares.runtracker.utils.Utils;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<RunPoint> runPointsList = new ArrayList<>();
    private RunsViewModel runsViewModel;
    private AppCompatActivity activity = this;
    private PermissionsHandler permissionsHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionsHandler = new PermissionsHandler(this, MainActivity.this);
        setContentView(R.layout.activity_main);
        permissionsHandler.checkForPermissions();
        runsViewModel = ViewModelProviders.of(this).get(RunsViewModel.class);
        runsViewModel.setActivity(this);
        runsViewModel.setContext(getApplicationContext());

        final Button goToListButton = findViewById(R.id.goToList);
        goToListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(MainActivity.this, RunsListActivity.class);
                startActivity(intent);
            }
        }); // The setOnClickListener() block ends here.

        final Switch locationSwitch = findViewById(R.id.locationSwitch);
        locationSwitch.setChecked(runsViewModel.canTrackLocation());
        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                runsViewModel.setCanTrackLocation(isChecked);
                if (isChecked && permissionsHandler.allConditionsMet()) {
                    permissionsHandler.startTrackingRuns();
                } else { // So save the current run, if the user switches the toggle off.
                    saveCurrentRun();
                }
            }
        });
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keeps the screen on always, when the app is running.
    } // The onCreate() method ends here


    private void saveCurrentRun() {

        if (runsViewModel == null) {
            runsViewModel = ViewModelProviders.of(this).get(RunsViewModel.class);
        }
        try {
            runsViewModel.saveCurrentRun(); // Saving them to external storage.
        } catch (JSONException e) {
            Log.e(Utils.getPackageName(MainActivity.this), "There was a JSONException whilst trying to save the journey points.");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(Utils.getPackageName(MainActivity.this), "There was an IOException whilst trying to save the journey points.");
            e.printStackTrace();
        }
    }
} // The main class ends here.

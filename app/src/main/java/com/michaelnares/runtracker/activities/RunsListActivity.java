package com.michaelnares.runtracker.activities;

import android.app.Activity;
import android.app.ListActivity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.michaelnares.runtracker.R;
import com.michaelnares.runtracker.utils.RunsViewModel;
import com.michaelnares.runtracker.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class RunsListActivity extends AppCompatActivity {
    private RunsViewModel runsViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.runs_list);
        ArrayList<String> startAndEndTimes;
        runsViewModel = ViewModelProviders.of(this).get(RunsViewModel.class);
        final ListView runsListView = findViewById(R.id.runsListView);
        try {
            startAndEndTimes = runsViewModel.getStartAndEndTimes();
            if (startAndEndTimes == null) {
                return;
            }
            final ArrayAdapter<String> runsListAdapter = new ArrayAdapter<String>(this, R.layout.row, R.id.startAndEnd, startAndEndTimes);
            runsListView.setAdapter(runsListAdapter);


        } catch (IOException e) {
            Utils.generateSingleButtonAlert(this, R.string.problem_loading_runs);
            e.printStackTrace();
        } catch (JSONException e) {
            Utils.generateSingleButtonAlert(this, R.string.problem_loading_runs);
            e.printStackTrace();
        }

        runsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    final JSONArray runsAsJson = new JSONArray(runsViewModel.getRunsAsString());
                    final JSONObject runObject = runsAsJson.getJSONObject(position);
                    runsViewModel.setSelectedRun(runObject.toString());
                    final Intent intent = new Intent(RunsListActivity.this, IndividualRunActivity.class);
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}

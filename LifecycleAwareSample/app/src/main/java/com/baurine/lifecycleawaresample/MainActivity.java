package com.baurine.lifecycleawaresample;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.widget.Chronometer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Chronometer chronometer = (Chronometer) findViewById(R.id.chronometer);
        ChronometerViewModel chronometerViewModel =
                ViewModelProviders.of(this).get(ChronometerViewModel.class);
        if (chronometerViewModel.getStartDate() == null) {
            long startTime = SystemClock.elapsedRealtime();
            chronometerViewModel.setStartDate(startTime);
            chronometer.setBase(startTime);
        } else {
            chronometer.setBase(chronometerViewModel.getStartDate());
        }

        chronometer.start();
    }
}

package com.baurine.lifecycleawaresample;

import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.TextView;

public class MainActivity extends LifecycleActivity {
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

        LiveDataTimerViewModel liveDataTimerViewModel =
                ViewModelProviders.of(this).get(LiveDataTimerViewModel.class);
        liveDataTimerViewModel.getElapsedTime().observe(this, new Observer<Long>() {
            @Override
            public void onChanged(@Nullable Long aLong) {
                ((TextView) findViewById(R.id.timer_textview)).setText(aLong + " seconds elapsed");
                Log.i("Timer", "Update timer");
            }
        });
    }
}

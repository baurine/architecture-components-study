package com.baurine.lifecycleawaresample;

import android.app.Activity;
import android.arch.lifecycle.LifecycleActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by baurine on 6/23/17.
 */

public class LocationActivity extends LifecycleActivity {
    public static void launch(Activity from) {
        Intent intent = new Intent(from, LocationActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
    }
}

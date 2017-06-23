package com.baurine.lifecycleawaresample;

import android.app.Activity;
import android.arch.lifecycle.LifecycleActivity;
import android.content.Intent;
import android.os.Bundle;

public class SeekbarActivity extends LifecycleActivity {
    public static void launch(Activity from) {
        Intent intent = new Intent(from, SeekbarActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seekbar);
    }
}

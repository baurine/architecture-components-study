package com.baurine.roomsample;

import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

public class LoansActivity extends LifecycleActivity {

    private TextView tvLoans;
    private CustomResultViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loans);

        tvLoans = (TextView) findViewById(R.id.tv_loans);
        viewModel = ViewModelProviders.of(this).get(CustomResultViewModel.class);
    }

    public void onRefresh(View view) {
        viewModel.createDb();
        viewModel.loansStr.observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                tvLoans.setText(s);
            }
        });
    }
}

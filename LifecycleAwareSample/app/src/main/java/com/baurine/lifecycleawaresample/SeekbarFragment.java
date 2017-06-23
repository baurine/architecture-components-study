package com.baurine.lifecycleawaresample;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

/**
 * Created by baurine on 6/23/17.
 */

public class SeekbarFragment extends Fragment {
    private SeekBar mSeekbar;
    private SeekbarViewModel mSeekbarViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_seekbar, container, false);
        mSeekbar = (SeekBar) root.findViewById(R.id.seekBar);
        mSeekbarViewModel = ViewModelProviders.of(getActivity()).get(SeekbarViewModel.class);
        subscribeSeekbar();
        return root;
    }

    private void subscribeSeekbar() {
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // to avoid infinite loop
                if (fromUser) {
                    mSeekbarViewModel.seekbarValue.setValue(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSeekbarViewModel.seekbarValue.observe((LifecycleOwner) getActivity(), new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if (integer != null) {
                    mSeekbar.setProgress(integer);
                }
            }
        });
    }
}

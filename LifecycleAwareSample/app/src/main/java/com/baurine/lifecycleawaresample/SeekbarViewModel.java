package com.baurine.lifecycleawaresample;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

/**
 * Created by baurine on 6/23/17.
 */

public class SeekbarViewModel extends ViewModel {
    public MutableLiveData<Integer> seekbarValue = new MutableLiveData<>();
}

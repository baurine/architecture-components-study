package com.baurine.lifecycleawaresample;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

/**
 * Created by baurine on 6/23/17.
 */

public class ChronometerViewModel extends ViewModel {
    @Nullable
    private Long startDate;

    @Nullable
    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }
}

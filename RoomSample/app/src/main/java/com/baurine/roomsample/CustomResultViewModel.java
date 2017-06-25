package com.baurine.roomsample;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;

import com.baurine.roomsample.db.AppDatabase;
import com.baurine.roomsample.db.LoanWithUserAndBook;
import com.baurine.roomsample.db.utils.DatabaseInitializer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by baurine on 6/25/17.
 */

public class CustomResultViewModel extends AndroidViewModel {

    public LiveData<String> loansStr;
    private AppDatabase appDb;

    public CustomResultViewModel(Application application) {
        super(application);
    }

    public void createDb() {
        appDb = AppDatabase.getInMemoryDatabase(this.getApplication());
        DatabaseInitializer.populateAsync(appDb);

        LiveData<List<LoanWithUserAndBook>> loans =
                appDb.loanModel().findLoansByNameAfter("Mike", getYesterday());
        loansStr = Transformations.map(loans, new Function<List<LoanWithUserAndBook>, String>() {
            @Override
            public String apply(List<LoanWithUserAndBook> loansWithUserAndBook) {
                StringBuilder sb = new StringBuilder();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm",
                        Locale.US);

                for (LoanWithUserAndBook loan : loansWithUserAndBook) {
                    sb.append(String.format("%s\n  (Returned: %s)\n",
                            loan.bookTitle,
                            simpleDateFormat.format(loan.endTime)));
                }
                return sb.toString();
            }
        });
    }

    private Date getYesterday() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Date yesterday = calendar.getTime();
        return yesterday;
    }
}

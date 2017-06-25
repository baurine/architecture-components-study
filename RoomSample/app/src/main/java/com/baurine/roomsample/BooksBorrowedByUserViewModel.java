package com.baurine.roomsample;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.baurine.roomsample.db.AppDatabase;
import com.baurine.roomsample.db.Book;
import com.baurine.roomsample.db.utils.DatabaseInitializer;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by baurine on 6/25/17.
 */

public class BooksBorrowedByUserViewModel extends AndroidViewModel {

    public final LiveData<List<Book>> books;
    private AppDatabase appDb;

    public BooksBorrowedByUserViewModel(Application application) {
        super(application);

        createDb();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Date yesterday = calendar.getTime();
        books = appDb.bookModel().findBooksBorrowedByNameAfter("Mike", yesterday);
    }

    public void createDb() {
        appDb = AppDatabase.getInMemoryDatabase(this.getApplication());
        DatabaseInitializer.populateAsync(appDb);
    }
}

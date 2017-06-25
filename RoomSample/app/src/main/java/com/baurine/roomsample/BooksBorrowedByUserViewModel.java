package com.baurine.roomsample;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.baurine.roomsample.db.AppDatabase;
import com.baurine.roomsample.db.Book;
import com.baurine.roomsample.db.utils.DatabaseInitializer;

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
        books = appDb.bookModel().findBooksBorrowedByName("Mike");
    }

    public void createDb() {
        appDb = AppDatabase.getInMemoryDatabase(this.getApplication());
        DatabaseInitializer.populateAsync(appDb);
    }
}

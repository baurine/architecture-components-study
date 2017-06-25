package com.baurine.roomsample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.baurine.roomsample.db.AppDatabase;
import com.baurine.roomsample.db.Book;
import com.baurine.roomsample.db.User;
import com.baurine.roomsample.db.utils.DatabaseInitializer;

import java.util.List;
import java.util.Locale;

public class UsersActivity extends AppCompatActivity {

    private TextView tvUsers, tvBooks;
    private AppDatabase appDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        tvUsers = (TextView) findViewById(R.id.tv_users);
        tvBooks = (TextView) findViewById(R.id.tv_books);
        appDb = AppDatabase.getInMemoryDatabase(getApplicationContext());

        populateDb();
        fetchData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppDatabase.destroyInstance();
    }

    private void populateDb() {
        DatabaseInitializer.populateSync(appDb);
    }

    private void fetchData() {
        List<User> youngUsers = appDb.userModel().findYoungerThanSolution(35);
        showUsers(youngUsers, tvUsers);
        List<Book> books = appDb.bookModel().findBooksBorrowedByNameSync("Mike");
        showBooks(books, tvBooks);
    }

    private static void showUsers(final List<User> users, final TextView textView) {
        StringBuilder sb = new StringBuilder();
        for (User user : users) {
            sb.append(String.format(Locale.US,
                    "%s, %s (%d)\n", user.lastName, user.name, user.age));
        }
        textView.setText(sb.toString());
    }

    private static void showBooks(final @NonNull List<Book> books,
                                  final TextView booksTextView) {
        StringBuilder sb = new StringBuilder();
        for (Book book : books) {
            sb.append(book.title);
            sb.append("\n");
        }
        booksTextView.setText(sb.toString());
    }
}

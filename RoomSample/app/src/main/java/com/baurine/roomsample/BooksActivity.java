package com.baurine.roomsample;

import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.baurine.roomsample.db.Book;

import java.util.List;

public class BooksActivity extends LifecycleActivity {

    private TextView tvBooks;
    private BooksBorrowedByUserViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books);

        tvBooks = (TextView) findViewById(R.id.tv_books);
        viewModel = ViewModelProviders.of(this).get(BooksBorrowedByUserViewModel.class);

        viewModel.books.observe(this, new Observer<List<Book>>() {
            @Override
            public void onChanged(@Nullable List<Book> books) {
                showBooks(books, tvBooks);
            }
        });
    }

    public void onRefresh(View view) {
        viewModel.createDb();
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

package com.baurine.roomsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.baurine.roomsample.db.AppDatabase;
import com.baurine.roomsample.db.User;
import com.baurine.roomsample.db.utils.DatabaseInitializer;

import java.util.List;
import java.util.Locale;

public class UsersActivity extends AppCompatActivity {

    private TextView tvUsers;
    private AppDatabase appDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        tvUsers = (TextView)findViewById(R.id.tv_users);
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
        StringBuilder sb = new StringBuilder();
        List<User> youngUsers = appDb.userModel().findYoungerThanSolution(35);
        for (User youngUser : youngUsers) {
            sb.append(String.format(Locale.US,
                    "%s, %s (%d)\n", youngUser.lastName, youngUser.name, youngUser.age));
        }
        tvUsers.setText(sb);
    }
}

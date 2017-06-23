package com.baurine.roomsample.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by baurine on 6/23/17.
 */

@Entity
public class User {
    public @PrimaryKey String id;
    public String name;
    @ColumnInfo(name="last_name")
    public String lastName;
    public int age;
}

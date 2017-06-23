package com.baurine.roomsample.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by baurine on 6/23/17.
 */

@Entity
public class Book {
    public @PrimaryKey String id;
    public String title;
}

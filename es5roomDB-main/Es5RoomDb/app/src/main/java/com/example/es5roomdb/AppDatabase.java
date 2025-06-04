package com.example.es5roomdb;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {User.class, Product.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract ProductDao productDao();
}
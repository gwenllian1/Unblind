package com.example.unblind;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {UIElement.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DB_NAME = "UIElement_db";
    private static AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DB_NAME).build();
        return instance;
    }

    public abstract UIElementDao getUIElementDao();

}

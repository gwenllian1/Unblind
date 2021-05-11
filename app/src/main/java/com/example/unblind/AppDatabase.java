package com.example.unblind;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {UIElement.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UIElementDao uiElementDao();

}

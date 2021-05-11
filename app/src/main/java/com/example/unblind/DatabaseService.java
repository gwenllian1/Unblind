package com.example.unblind;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.room.Room;

import java.util.List;


public class DatabaseService extends Service {
    private final IBinder binder = new LocalBinder();
    AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "UIElement-Database").build();
    UIElementDao uiElementDao = db.uiElementDao();

    public class LocalBinder extends Binder {
        DatabaseService getService() {
            return DatabaseService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // Client methods go below

    public List<UIElement> queryDatabase() {
        return uiElementDao.getAll();
    }

    public void insertDatabase(String iconHash, String altText) {
        uiElementDao.insertAll(new UIElement(iconHash, altText));
    }

}

package com.example.unblind;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.room.Room;


public class DatabaseService extends Service {
    private final IBinder binder = new LocalBinder();
    AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "UIElement-Database").build();

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

    public void queryDatabase() {

    }

    public void insertDatabase() {

    }

}

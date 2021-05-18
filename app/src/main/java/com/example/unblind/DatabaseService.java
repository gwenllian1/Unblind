package com.example.unblind;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.room.Room;

import java.util.List;


public class DatabaseService extends Service {
    private final IBinder binder = new LocalBinder();
    private static final String TAG = "UnBlindDatabaseService";
//     AppDatabase db = AppDatabase.getInstance(this);
//     UIElementDao uiElementDao = db.getUIElementDao();

    public class LocalBinder extends Binder {
        public DatabaseService getService() {
            return DatabaseService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "bound");
        return binder;
    }

    // Client methods go below

//    public List<UIElement> queryDatabase() {
//        Log.e(TAG, "query all");
//        return uiElementDao.getAll();
//    }
//
//    public void insertDatabase(String iconHash, String altText) {
//        Log.e(TAG, "insert all");
//        uiElementDao.insertAll(new UIElement(iconHash, altText));
//    }


}

package com.example.unblind;

import android.app.Service;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.content.SharedPreferences;

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
        setSharedData("testing", "image1", "label1");
        Log.e(TAG, "bound-after");
        getSharedData("testing", "image1");
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

    public void setSharedData(String prefName, String imageKey, String label)
    {
        SharedPreferences sp = getSharedPreferences(prefName, MODE_PRIVATE);
        SharedPreferences.Editor spEdit = sp.edit();

        spEdit.putString(imageKey, label);
        Log.e(TAG, "Edwin: Saved to SP");
        spEdit.commit();
    }

    public String getSharedData(String prefName, String imageKey)
    {
        SharedPreferences sp = getSharedPreferences(prefName, MODE_PRIVATE);

        // default string none
        String label = sp.getString(imageKey, "None");
        Log.e(TAG, "Edwin: From sp database: " + label);
        return label;
    }

}

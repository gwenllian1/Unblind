package com.example.unblind;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;


public class DatabaseService extends Service {
    private final IBinder binder = new LocalBinder();
    private static final String TAG = "UnBlindDatabaseService";
    private final UnblindMediator unblindMediator = new UnblindMediator();

    public class LocalBinder extends Binder {
        public DatabaseService getService() {
            return DatabaseService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
//        Log.e(TAG, "bound");
//        setSharedData("testing", "image1", "label1");
//        Log.e(TAG, "Unblindbound-after");
//        getSharedData("testing", "image1");
        return binder;
    }

    // Client methods go below

    public void setSharedData(String prefName, String imageKey, String label)
    {
        SharedPreferences sp = getSharedPreferences(prefName, MODE_PRIVATE);
        SharedPreferences.Editor spEdit = sp.edit();

        spEdit.putString(imageKey, label);
        Log.v(TAG, "Edwin: Saved to SP");
        spEdit.commit();
    }

    public String getSharedData(String prefName, String imageKey)
    {
        SharedPreferences sp = getSharedPreferences(prefName, MODE_PRIVATE);

        // default string null
        String label = sp.getString(imageKey, null);
        Log.v(TAG, "Edwin: Retrieved from SP: " + label);
        return label;
    }

    public UnblindMediator getUnblindMediator() {
        return unblindMediator;
    }
}

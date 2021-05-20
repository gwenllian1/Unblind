package com.example.unblind;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.Nullable;

public class ModelService extends Service implements ColleagueInterface {
    public static final String TAG = "ModelService";
    DatabaseService mService;
    UnblindMediator mediator;
    Pair<String, String> currentElement;
    boolean mBound = false;
    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            DatabaseService.LocalBinder binder = (DatabaseService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            Log.e(TAG, "databaseServiceConnected");
            // get mediator
            Log.e(TAG, "bound, getting mediator");
            mediator = mService.getUnblindMediator();
            mediator.addObserver((ColleagueInterface) getSelf());

        }

        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "databaseServiceDisconnected");
            mBound = false;
        }
    };

    @Override
    public void update() {
        currentElement = mediator.getElement();
        Log.e(TAG, "updating element");
        // call deeplearning function
    }

    public ModelService getSelf() {
        return this;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "Model service started");
        super.onStartCommand(intent, flags, startId);
        // bind to DatabaseService
        Intent newIntent = new Intent(this, DatabaseService.class);
        startService(newIntent);
        bindService(newIntent, mConnection, Context.BIND_AUTO_CREATE);
        return START_NOT_STICKY;
    }

    // Client methods go here


}

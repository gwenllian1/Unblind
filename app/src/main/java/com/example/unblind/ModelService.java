package com.example.unblind;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.unblind.model.TfliteClassifier;


public class ModelService extends Service implements ColleagueInterface {
    public static final String TAG = "ModelService";
    private final IBinder binder = new LocalBinder();
    DatabaseService databaseService;
    UnblindMediator mediator;
    UnblindDataObject currentElement = null;
    boolean dbBound = false;
    boolean batch = false;

    TfliteClassifier tfliteClassifier;

    @SuppressLint("StaticFieldLeak")
    private class GetClassifier extends AsyncTask<String, Integer, TfliteClassifier> {

        @Override
        protected TfliteClassifier doInBackground(String... strings) {
            return new TfliteClassifier(getOuter());
        }
        @Override
        protected void onPostExecute(TfliteClassifier result) {
            Log.e(TAG, "onPostExecute: ");
            setClassifier(result);
        }

        public ModelService getOuter() {
            return ModelService.this;
        }
    }

    private void setClassifier(TfliteClassifier tfliteClassifier) {
        Log.e(TAG, "classifier set");
        this.tfliteClassifier = tfliteClassifier;
    }

    private final ServiceConnection dbConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            DatabaseService.LocalBinder binder = (DatabaseService.LocalBinder) service;
            databaseService = binder.getService();
            dbBound = true;
            Log.d(TAG, "databaseServiceConnected");
            // get mediator
            Log.d(TAG, "bound, getting mediator");
            mediator = databaseService.getUnblindMediator();
            batch = mediator.checkModelServiceObserver();
            mediator.addObserver((ColleagueInterface) getSelf());
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "databaseServiceDisconnected");
            dbBound = false;
        }
    };

    public class LocalBinder extends Binder {
        public ModelService getService() { return ModelService.this; }
    }

    @Override
    public void update() {
        if (batch) {
            if (mediator.checkIncomingBatchQueueEmpty()) {
                return;
            }
            if (currentElement != null) {
                Log.v(TAG, "ModelService is deferring processing...");
                // When the current image has been processed,
                // it will set currentElement to null and call this update method
                return;
            }
            currentElement = mediator.serveElementFromIncomingBatchQueue();
            Log.v(TAG, "BatchService is running prediction");
        }
        else {
            if (mediator.checkIncomingImmediateQueueEmpty()) {
                return;
            }
            if (currentElement != null) {
                Log.v(TAG, "ModelService is deferring processing...");
                // When the current image has been processed,
                // it will set currentElement to null and call this update method
                return;
            }
            currentElement = mediator.serveElementFromIncomingImmediateQueue();
            Log.v(TAG, "ModelService is running prediction");
        }
        runPredication();
    }

    public ModelService getSelf() {
        return this;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // execute classifier
        new GetClassifier().execute();

        // bind to DatabaseService
        Intent newIntent = new Intent(this, DatabaseService.class);
        bindService(newIntent, dbConnection, Context.BIND_AUTO_CREATE);
    }

        @Override
    public void onDestroy() {
        unbindService(dbConnection);
    }

    public void runPredication() {
        String result = tfliteClassifier.predict(currentElement.iconImage);     // predict the bitmap
        Log.d("Team 3 Model Result", result);
        currentElement = new UnblindDataObject(currentElement.iconImage, result, currentElement.batchStatus);

        // store classified pair into cache
        Log.v(TAG, "setting in SP");
        byte[] base64EncodedBitmap = UnblindMediator.bitmapToBytes(currentElement.iconImage);
        databaseService.setSharedData(UnblindMediator.TAG, base64EncodedBitmap, result);

        // only push element to mediator if immediate processing
        if (!batch) {
            mediator.pushElementToOutgoingImmediateQueue(currentElement);
        }

        currentElement = null;
        mediator.notifyObservers();
    }
}

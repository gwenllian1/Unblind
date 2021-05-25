package com.example.unblind;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.Nullable;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.unblind.model.Classifier;
import com.example.unblind.model.Utils;

public class ModelService extends Service implements ColleagueInterface {
    public static final String TAG = "ModelService";
    DatabaseService mService;
    UnblindMediator mediator;
    WorkManager mWorkManager;
    Pair<Bitmap, String> currentElement = new Pair<Bitmap, String>(null, "");
    boolean mBound = false;
    static final String MODEL_NAME = "labeldroid.pt";

    Classifier classifier;

    private class GetClassifier extends AsyncTask<String, Integer, Classifier> {

        @Override
        protected Classifier doInBackground(String... strings) {
            String absolutePath = Utils.assetFilePath(getOuter(), strings[0]); //get absolute path
            return new Classifier(absolutePath);
        }

        @Override
        protected void onPostExecute(Classifier result) {
            Log.e(TAG, "onPostExecute: ");
            setClassifier(result);
        }

        public ModelService getOuter() {
            return ModelService.this;
        }
    }

    private void setClassifier(Classifier classifier) {
        Log.e(TAG, "classifier set");
        this.classifier = classifier;
    }

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
        if (currentElement.first != mediator.getElement().first){
            currentElement = mediator.getElement();
            runPredication();
        }
        Log.e(TAG, "updating element");
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
//        loadClassifier();
        new GetClassifier().execute(MODEL_NAME);
        // bind to DatabaseService
        Intent newIntent = new Intent(this, DatabaseService.class);
        startService(newIntent);
        bindService(newIntent, mConnection, Context.BIND_AUTO_CREATE);
        return START_NOT_STICKY;
    }


    // Client methods go here
    public void loadClassifier(){
        // use the function provided by Utils class
        String absolutePath = Utils.assetFilePath(this, "labeldroid.pt"); //get absolute path
        classifier = new Classifier(absolutePath);
    }


    public void runPredication(){
        String result = classifier.predict(currentElement.first);     // predict the bitmap
        Log.d("Team 3 Model Result", result);
        currentElement = new Pair<Bitmap, String>(currentElement.first, result);
        mediator.setElement(currentElement);
    }

}

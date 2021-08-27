package com.example.unblind;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
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
        if ((!mediator.checkIncomingEmpty()) && (currentElement.first != mediator.getElementFromIncoming().first)){
            currentElement = mediator.serveElementFromIncoming();
            runPredication();
        }
        Log.e(TAG, "updating element on model");
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
    public void onCreate() {
        super.onCreate();
        startNotification();
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
        if(intent.getAction() != null && intent.getAction().equals(getString(R.string.turn_off))) {
            stopForeground(true);
        }
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
        mediator.pushElementToOutgoing(currentElement);
        mediator.notifyObservers();
    }

    private void startNotification() {
        CharSequence text = getText(R.string.example_service_running);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        Intent stopSelf = new Intent(this, ModelService.class);
        stopSelf.setAction(getString(R.string.turn_off));
        PendingIntent pStopSelf = PendingIntent.getService(this, 0, stopSelf,PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_android_black_24dp)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getText(R.string.example_service_label))
                .setContentText(text)
                .setContentIntent(contentIntent)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .addAction(R.drawable.ic_android_black_24dp, getString(R.string.turn_off), pStopSelf)
                .build();

        startForeground(1, notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager){
        String channelId = "exampleChannel";
        String channelName = "Example Channel";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        // omitted the LED color
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }

}

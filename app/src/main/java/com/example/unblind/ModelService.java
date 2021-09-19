package com.example.unblind;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

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
            Log.e(TAG, "databaseServiceConnected");
            // get mediator
            Log.e(TAG, "bound, getting mediator");
            mediator = databaseService.getUnblindMediator();
            batch = mediator.checkModelServiceObserver();
            mediator.addObserver((ColleagueInterface) getSelf());
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "databaseServiceDisconnected");
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
        startNotification();
    }


    public void runPredication(){
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

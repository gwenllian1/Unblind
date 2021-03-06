//        Copyright 2021 Project 3

//        This file is part of UnBlind.
//
//        UnBlind is free software: you can redistribute it and/or modify
//        it under the terms of the GNU General Public License as published by
//        the Free Software Foundation, either version 3 of the License, or
//        (at your option) any later version.
//
//        UnBlind is distributed in the hope that it will be useful,
//        but WITHOUT ANY WARRANTY; without even the implied warranty of
//        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//        GNU General Public License for more details.
//
//        You should have received a copy of the GNU General Public License
//        along with UnBlind.  If not, see <https://www.gnu.org/licenses/>.

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
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.unblind.model.TfliteClassifier;
import com.example.unblind.model.TflitePrediction;


/**
 * A service that provides access to the deep learning model.
 * Generates labels when updated by the mediator.
 */
public class ModelService extends Service implements ColleagueInterface {
    public static final String TAG = "ModelService";
    private final IBinder binder = new LocalBinder();
    DatabaseService databaseService;
    UnblindMediator mediator;
    UnblindDataObject currentElement = null;
    boolean dbBound = false;

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
    /**
     * Implementation of the ColleagueInterface method update()
     * Prioritises classification of icon buttons which are selected, then the batch processing icons
     */
    public void update() {
        if (mediator.checkIncomingBatchQueueEmpty()) {
            return;
        }
        if (currentElement != null) {
            Log.v(TAG, "ModelService is deferring processing...");
            // When the current image has been processed,
            // it will set currentElement to null and call this update method
            return;
        }
        if (!mediator.checkIncomingImmediateQueueEmpty()) {
            currentElement = mediator.serveElementFromIncomingImmediateQueue();
            Log.v(TAG, "Immediate prediction running");
        } else if (!mediator.checkIncomingBatchQueueEmpty()) {
            currentElement = mediator.serveElementFromIncomingBatchQueue();
            Log.v(TAG, "Batch prediction(s) running");
        }
        runPredication();
    }

    /**
     * Getter for this ModelService instance
     * @return this ModelService instance
     */
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startNotification();
        if (intent.getAction() != null && intent.getAction().equals(getString(R.string.turn_off))) {
            stopForeground(true);
        }
        return START_NOT_STICKY;
    }



        @Override
    public void onDestroy() {
        unbindService(dbConnection);
    }

    /**
     * Classifies the the icon in the currentElement.
     * Doesn't push batch processed icons to the outgoing queue
     */
    public void runPredication() {
        String label = tfliteClassifier.predict(currentElement.iconImage);     // predict the bitmap
        Log.d("Team 3 Model Result", label);
        currentElement = new UnblindDataObject(currentElement.iconImage, label, currentElement.batchStatus);

        // store classified pair into cache
        Log.v(TAG, "setting in SP");
        byte[] base64EncodedBitmap = UnblindMediator.bitmapToBytes(currentElement.iconImage);
        databaseService.setSharedData(UnblindMediator.TAG, base64EncodedBitmap, label);

        // only push element to mediator if immediate processing
        if (!currentElement.batchStatus) {
            mediator.pushElementToOutgoingImmediateQueue(currentElement);
        }

        currentElement = null;
        mediator.notifyObservers();
    }

    private void startNotification() {
        CharSequence text = getText(R.string.example_service_running);

        // Setting up accessibility settings click action (Main notification click action)
        Intent accessSettings = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        PendingIntent pAccessSettings = PendingIntent.getActivity(this, 0, accessSettings, 0);

        // Setting up remove notification action
        Intent stopSelf = new Intent(this, ModelService.class);
        stopSelf.setAction(getString(R.string.turn_off));
        PendingIntent pStopSelf = PendingIntent.getService(this, 0, stopSelf, PendingIntent.FLAG_CANCEL_CURRENT);

        // Setting up redirect to Unblind app click action
        Intent redirectToApp = new Intent(this, MainActivity.class);
        accessSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        accessSettings.setAction(getString(R.string.redir_app));
        PendingIntent pRedirectToApp = PendingIntent.getActivity(this, 0, redirectToApp,PendingIntent.FLAG_CANCEL_CURRENT);


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_android_black_24dp)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getText(R.string.example_service_label))
                .setContentText(text)
                .setContentIntent(pAccessSettings) // sends user back to app when notification is clicked
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .addAction(R.drawable.ic_android_black_24dp, getString(R.string.turn_off), pStopSelf)
                .addAction(R.drawable.ic_android_black_24dp, getString(R.string.redir_app), pRedirectToApp)
                .build();

        startForeground(1, notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager) {
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

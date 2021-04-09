package com.example.unblind;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class ExampleService extends Service {
//    private NotificationManager exampleNM;
    private int NOTIFICATION = R.string.example_service_running;

//    public class ExampleBinder extends Binder {
//        ExampleService getService() {
//            return ExampleService.this;
//        }
//    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.i("LocalService", "Received start id " + startId + ": " + intent);
//        showNotification();
//        return START_NOT_STICKY;
//        exampleNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        exampleNM.cancel(NOTIFICATION);
//        Toast.makeText(this, R.string.example_service_stopped, Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
//        return exampleBinder;
    }

//    private final IBinder exampleBinder = new ExampleBinder();

    private void showNotification() {
        CharSequence text = getText(R.string.example_service_running);

        // Launch activity when notification clicked
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        Notification notification = new NotificationCompat.Builder(this, ExampleApp.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_android_black_24dp)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getText(R.string.example_service_label))
                .setContentText(text)
                .setContentIntent(contentIntent)
                .build();

//        exampleNM.notify(NOTIFICATION, notification);
        startForeground(1,notification);
    }

}

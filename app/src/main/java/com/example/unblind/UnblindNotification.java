package com.example.unblind;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class UnblindNotification extends Service {
    private final IBinder binder = new  LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public UnblindNotification getService() { return UnblindNotification.this; }
    }

    @Override
    public void onCreate() {
        super.onCreate();
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

    public void startNotification() {
        CharSequence text = getText(R.string.example_service_running);

        // Setting up accessibility settings click action (Main notification click action)
        Intent accessSettings = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        PendingIntent pAccessSettings = PendingIntent.getActivity(this, 0, accessSettings, 0);

        // Setting up remove notification action
        Intent stopSelf = new Intent(this, UnblindNotification.class);
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

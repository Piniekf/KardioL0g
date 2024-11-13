package com.party.kardiol0g;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.Manifest;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class NotificationHelper {

    public static void createNotificationChannel(Context context, String channelId, String channelName) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public static void showNotification(Context context, String channelId, int notificationId, String title, String message) {
        // Utwórz intencję dla powiadomienia
        Intent notificationIntent = new Intent(context, MoreFragment.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent;

        // Sprawdź dostępność uprawnień dla PendingIntent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Sprawdź uprawnienia
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }


        // Utwórz obiekt NotificationCompat.Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent);

        // Pobierz menedżer powiadomień
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Wyślij powiadomienie
        notificationManager.notify(notificationId, builder.build());
    }




    public static void cancelNotification(Context context, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }
}

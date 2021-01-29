package com.MennoSpijker.kentekenscanner.Factory;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.MennoSpijker.kentekenscanner.R;
import com.MennoSpijker.kentekenscanner.View.MainActivity;

public class NotifcationFactory {
    public static final String CHANNEL_ID = "Kenteken Scanner";
    private Context context;

    public NotifcationFactory(Context ctx) {
        context = ctx;
    }

    public void createNotificationWithExtra(String key, String content) {
        int notificationId = 1;

        System.out.println("Creating notification: " + CHANNEL_ID + " " + notificationId);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(key, content);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_icon)
                .setContentTitle("test")
                .setContentText("Lorem ipsum")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }
}

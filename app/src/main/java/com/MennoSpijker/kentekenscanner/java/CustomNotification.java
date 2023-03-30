package com.MennoSpijker.kentekenscanner.java;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static com.MennoSpijker.kentekenscanner.Factory.NotificationFactory.CHANNEL_ID;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.MennoSpijker.kentekenscanner.R;
import com.MennoSpijker.kentekenscanner.activity.MainActivity;

public class CustomNotification extends Worker {
    private final Context context;

    public CustomNotification(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        createNotification();
        return Result.success();
    }

    private void createNotification() {
        String title = getInputData().getString("title");
        String text = getInputData().getString("text");
        String kenteken = getInputData().getString("kenteken");
        int uuid = getInputData().getInt("uuid", 1);

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("kenteken", kenteken);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_180)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(uuid, builder.build());
    }
}

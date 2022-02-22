package com.MennoSpijker.kentekenscanner.Factory;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.MennoSpijker.kentekenscanner.R;
import com.MennoSpijker.kentekenscanner.java.CustomNotification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class NotificationFactory {
    private static final String TAG = "NotificationFactory";
    public static final String CHANNEL_ID = "KentekenScanner";

    private final Context context;

    public NotificationFactory(Context context) {
        Log.d(TAG, "NotificationFactory: New notifcationFactory generated");

        this.context = context;
        this.createNotificationChannel();
    }
    Date getDate(String dateString) throws ParseException {
        return new SimpleDateFormat("dd-MM-yy", Locale.GERMANY).parse(dateString);
    }

    public long calculateNotifcationTime(String dateString) {
        try {
            Date date = this.getDate(dateString);
            Log.d(TAG, "onCreate: " + date.toString());

            Calendar c = Calendar.getInstance();
            c.setTimeZone(TimeZone.getTimeZone("Europe/Amsterdam"));
            c.setTime(date);
            c.add(Calendar.DAY_OF_YEAR, 0);
            c.add(Calendar.HOUR_OF_DAY, 12);
            c.add(Calendar.MINUTE, 0);

            Log.d(TAG, "onCreate: " + System.currentTimeMillis());
            Log.d(TAG, "onCreate: " + c.getTimeInMillis());

            long currentTime = System.currentTimeMillis();
            long specificTimeToTrigger = c.getTimeInMillis();
            long delayToPass = specificTimeToTrigger - currentTime;

            Log.d(TAG, "onCreate: " + delayToPass);

            return delayToPass;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void planNotification(String notificationTitle, String notificationText, long notificationDate) {
        Data.Builder data = new Data.Builder();
        data.putString("title", notificationTitle);
        data.putString("text", notificationText);

        Log.d(TAG, "planNotification: " + notificationDate);

        OneTimeWorkRequest compressionWork =
                new OneTimeWorkRequest.Builder(CustomNotification.class)
                        .setInitialDelay(notificationDate, TimeUnit.MILLISECONDS)
                        .setInputData(data.build())
                        .build();

        WorkManager.getInstance().enqueue(compressionWork);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        CharSequence name = context.getString(R.string.channel_name);
        String description = context.getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Log.d(TAG, "createNotificationChannel: Notification channel created...");
    }
}

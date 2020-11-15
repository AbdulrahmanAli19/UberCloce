package com.example.uber;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.uber.Model.DriverInfoModel;

public class Common {
    public static final String DRIVER_INFO_REFERENCE = "DriverInfo";
    public static final String DRIVER_LOCATION_REFERENCE = "DriversLocation";
    public static final String TOKEN_REFERENCE = "Token";
    public static final String NOTI_FILE = "Title";
    public static final String NOTI_CONTECT = "Contact";

    public static DriverInfoModel currentUser;

    public static String buildWelocmeMessage() {
        if (currentUser != null)
            return new StringBuilder("Welocme ")
                    .append(currentUser.getFistName())
                    .append(" ")
                    .append(currentUser.getLastName())
                    .toString();
        else
            return "";
    }

    public static void showNotification(Context context, int id, String title, String contact, Intent intent) {
        PendingIntent pendingIntent = null;
        String NOTIFICATION_CHANNEL = "thisIsMyChannel1";

        if (intent != null)
            pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notiManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL,
                    "Uber", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Uber remake");
            channel.enableLights(true);
            channel.setLightColor(Color.WHITE);
            channel.setVibrationPattern(new long[]{0, 1000, 5000, 1000});
            channel.enableVibration(true);

            notiManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL);
        builder.setContentTitle(title)
                .setContentText(contact)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.drawable.ic_round_directions_car_24)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_round_directions_car_24));

        if (pendingIntent != null)
            builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();
        notiManager.notify(id, notification);

    }
}

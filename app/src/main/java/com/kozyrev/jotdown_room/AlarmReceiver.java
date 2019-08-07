package com.kozyrev.jotdown_room;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import static android.content.Context.NOTIFICATION_SERVICE;

public class AlarmReceiver extends BroadcastReceiver {

    public static final int NOTIFICATION_ID = 5453;
    public static final String CHANNEL_ID = "1234";

    public static final String EXTRA_NOTE_ID = "noteId";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_CONTENT_TEXT = "contentText";
    public static final String EXTRA_URI = "uri";


    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "ТЕСТИРУЕМ +|+ " + intent.getStringExtra(EXTRA_CONTENT_TEXT) +  " +|+ ПАЦАНЫ", Toast.LENGTH_LONG).show();
        showNotification(context,
                intent.getStringExtra(EXTRA_TITLE),
                intent.getStringExtra(EXTRA_CONTENT_TEXT),
                intent.getStringExtra(EXTRA_URI),
                intent.getIntExtra(EXTRA_NOTE_ID,0));
    }

    private void showNotification(Context context, String title, String contentText, String imageUriString, int noteId){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Notify channel", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel description");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);

            builder = new NotificationCompat.Builder(context, CHANNEL_ID);
            setBuilderParams(builder,
                    title,
                    contentText,
                    imageUriString,
                    R.mipmap.ic_launcher_round);
        }
        else {
            builder = new NotificationCompat.Builder(context);
            setBuilderParams(builder,
                    title,
                    contentText,
                    imageUriString,
                    R.mipmap.ic_launcher);
        }

        Intent actionIntent = new Intent(context, DetailNoteActivity.class);
        actionIntent.putExtra(DetailNoteActivity.EXTRA_NOTE_ID, noteId);
        PendingIntent actionPendingIntent = PendingIntent.getActivity(context, 0, actionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(actionPendingIntent);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void setBuilderParams(NotificationCompat.Builder builder, String title, String contentText, String imageUriString, int icon){
        Bitmap bitmap = null;/*
        try {
            bitmap = Picasso.get().load(Uri.parse(imageUriString)).get();
        } catch (IOException ex){
            ex.printStackTrace();
        }*/

        builder.setSmallIcon(icon)
                .setLargeIcon(bitmap)
                .setContentTitle(title)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setWhen(SystemClock.currentThreadTimeMillis())
                .setVibrate(new long[]{0, 1000})
                .setAutoCancel(true);
    }
}

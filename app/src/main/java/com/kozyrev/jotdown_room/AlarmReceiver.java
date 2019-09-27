package com.kozyrev.jotdown_room;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

public class AlarmReceiver extends BroadcastReceiver {

    public static final int NOTIFICATION_ID = 5453;
    public static final String CHANNEL_ID = "1234";

    public static final String EXTRA_NOTE_ID = "noteId", EXTRA_TITLE = "title", EXTRA_CONTENT_TEXT = "contentText";


    @Override
    public void onReceive(Context context, Intent intent) {
        showNotification(
                context,
                intent.getStringExtra(EXTRA_TITLE),
                intent.getStringExtra(EXTRA_CONTENT_TEXT),
                intent.getIntExtra(EXTRA_NOTE_ID,0));
    }

    private void showNotification(Context context, String title, String contentText, int noteId){
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
                    R.mipmap.ic_launcher_round);
        }
        else {
            builder = new NotificationCompat.Builder(context);
            setBuilderParams(builder,
                    title,
                    contentText,
                    R.mipmap.ic_launcher);
        }

        Intent actionIntent = new Intent(context, DetailNoteActivity.class);
        actionIntent.putExtra(DetailNoteActivity.EXTRA_NOTE_ID, noteId);
        PendingIntent actionPendingIntent = PendingIntent.getActivity(context, 0, actionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(actionPendingIntent);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void setBuilderParams(NotificationCompat.Builder builder, String title, String contentText, int icon){

        builder.setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(contentText)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[]{0, 1000})
                .setAutoCancel(true);
    }
}
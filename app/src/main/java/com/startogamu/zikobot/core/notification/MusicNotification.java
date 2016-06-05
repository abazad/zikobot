package com.startogamu.zikobot.core.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.startogamu.zikobot.R;
import com.startogamu.zikobot.core.receiver.StopReceiver;
import com.startogamu.zikobot.core.utils.REQUEST;

/**
 * Created by josh on 10/04/16.
 */
public class MusicNotification {

    private static PendingIntent intentClear;

    private static String title;
    private static Context context;
    private static NotificationManager notificationManager;


    public static void init(final Context context) {
        MusicNotification.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /***
     */
    public static void show(final String title) {
        MusicNotification.title = title;

        Intent clearIntent = new Intent();
        clearIntent.setAction(StopReceiver.TAG);
        intentClear = PendingIntent.getBroadcast(context, REQUEST.NOTIFICATION_CLEAR, clearIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(context)
                .setContentTitle(title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .addAction(R.drawable.ic_clear, context.getString(R.string.stop), intentClear)
                .build();

        notificationManager.notify(0, notification);
    }

    public static void cancel() {
        notificationManager.cancel(0);
    }
}
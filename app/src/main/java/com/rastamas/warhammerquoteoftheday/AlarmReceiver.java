package com.rastamas.warhammerquoteoftheday;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Created by Rasta on 1/16/2017.
 */

public class AlarmReceiver extends BroadcastReceiver {
    int MID = 0;
    @Override
    public void onReceive(Context context, Intent intent) {

        long when = System.currentTimeMillis();
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);



        android.support.v4.app.NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_noti_icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_transparent))
                .setContentTitle("Emperor Quote of the Day")
                .setContentText("The God-Emperor of Mankind has spoken!")
                .setWhen(when)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setLights(context.getResources().getColor(R.color.bloodRavenPrimary), 1000, 1000);
        Notification notification = notifyBuilder.build();

        hideSmallIconFromNotificationView(notification, context);

        notificationManager.notify(MID++, notification);
    }

    private void hideSmallIconFromNotificationView(Notification notif, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int smallIconViewId = context.getResources().getIdentifier("right_icon", "id", android.R.class.getPackage().getName());

            if (smallIconViewId != 0) {
                if (notif.contentIntent != null)
                    notif.contentView.setViewVisibility(smallIconViewId, View.INVISIBLE);

                if (notif.headsUpContentView != null)
                    notif.headsUpContentView.setViewVisibility(smallIconViewId, View.INVISIBLE);

                if (notif.bigContentView != null)
                    notif.bigContentView.setViewVisibility(smallIconViewId, View.INVISIBLE);
            }
        }

    }
}

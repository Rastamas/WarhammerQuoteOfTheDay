package com.rastamas.warhammerquoteoftheday;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

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
                .setSmallIcon(R.drawable.ic_settings) //TODO create pretty icon
                .setContentTitle("Emperor Quote of the Day")
                .setContentText("The God-Emperor of Mankind has spoken!")
                .setWhen(when)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setLights(context.getResources().getColor(R.color.bloodRavenPrimary), 1000, 1000);
        notificationManager.notify(MID++, notifyBuilder.build());
    }
}

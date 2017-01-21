package com.rastamas.warhammerquoteoftheday;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Display;
import android.view.WindowManager;

import java.util.Date;

/**
 * Created by Rasta on 1/21/2017.
 */

final class Helper {
    public static void contactDeveloper() {
        //TODO
    }

    static IntTuple getScreenSize(WindowManager windowManager){
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        return new IntTuple(width, height);
    }

    static String createDateKey(Date date) {
        String[] dateStringParts = date.toString().split(" ");
        //example: "Sun Dec 08 04:54:14 GMT+01:00 2016"
        return dateStringParts[5] + dateStringParts[1] + dateStringParts[2].replaceFirst("^0", "");
    }

     static boolean isServerAccessible(Context context) {
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } catch (Exception e) {
            return false;
        }
    }
}

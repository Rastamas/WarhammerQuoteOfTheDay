package com.rastamas.warhammerquoteoftheday;

import android.app.Application;
import android.content.Context;

import java.lang.ref.WeakReference;

/**
 * Created by Rasta on 02/04/2017.
 */

public class App extends Application {
    private static WeakReference<Context> mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = new WeakReference<Context>(this);
    }

    public static Context getContext(){
        return mContext.get();
    }
}

package com.pycitup.pyc;

import android.app.Application;
import android.os.Looper;

import com.parse.Parse;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.PushService;

/**
 * Created by rishabhpugalia on 02/09/14.
 */
public class PycApplication extends Application {

    public static String PUBNUB_PUB_KEY = "pub-c-dbf49973-307a-4b0d-852f-6f5f05446b5e";
    public static String PUBNUB_SUB_KEY = "sub-c-43fccdc4-6636-11e4-90a5-02ee2ddab7fe";
    public static String PUBNUB_SECRET_KEY = "sec-c-NTlhMWUzMjktZTc2Yy00YzMzLTgzMzctNzlhODVkODJiY2Nh";

    @Override
    public void onCreate() {
        // Initializing Parse
        Parse.initialize(this, "VO5bEiSi2sD7TW3HnQ5mYipbodRAyJst6NCh8l7d", "sKEmfYHJtPe1nzuM4zRhBDaMyJg5YQgdd1Tn5lVc");

        // Specify activity to handle push notifications
        PushService.setDefaultPushCallback(this, PushActivity.class);
    }

    public static boolean isMainThread() {
        // return Looper.myLooper() == Looper.getMainLooper();
        return Looper.getMainLooper().getThread() == Thread.currentThread(); // can also do this
    }
}

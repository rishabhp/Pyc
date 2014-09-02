package com.pycitup.pyc;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by rishabhpugalia on 02/09/14.
 */
public class PycApplication extends Application {

    @Override
    public void onCreate() {
        // Initializing Parse
        Parse.initialize(this, "VO5bEiSi2sD7TW3HnQ5mYipbodRAyJst6NCh8l7d", "sKEmfYHJtPe1nzuM4zRhBDaMyJg5YQgdd1Tn5lVc");
    }
}

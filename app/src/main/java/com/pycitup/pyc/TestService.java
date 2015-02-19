package com.pycitup.pyc;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;

import com.pycitup.pyc.aidl.IBoundService;

public class TestService extends IntentService {

    public TestService(String name) {
        // Used to name the worker thread
        // Important only for debugging
        super(TestService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Invoked on the worker thread
        // Do some work in background without affecting the UI thread
    }
}

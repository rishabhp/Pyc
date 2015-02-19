package com.pycitup.pyc;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.AsyncQueryHandler;
import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObservable;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.gesture.Gesture;
import android.net.Uri;
import android.os.*;
import android.os.Process;
import android.provider.UserDictionary;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.Property;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.pubnub.api.Callback;
import com.pubnub.api.PnGcmMessage;
import com.pubnub.api.PnMessage;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.pycitup.pyc.aidl.IBoundService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity implements ActionBar.TabListener {

    public String TAG = MainActivity.class.getSimpleName();

    HomePagerAdapter mHomePagerAdapter;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get current user
        ParseUser currentUser = ParseUser.getCurrentUser();

        if (currentUser == null) {
            // It's an anonymous user, hence show the login screen
            navigateToLogin();
        }
        else {
            // The user is logged in, yay!!
            // Log.i(TAG, currentUser.getUsername());
        }

        // Setup the action bar for tabs
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setTitle("Pyc");


        // == Setting up the ViewPager ==

        mHomePagerAdapter = new HomePagerAdapter(getFragmentManager(), this);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mHomePagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });


        // == Setting up the tabs ==

        String[] tabs = {
                "Home", "Photos", "Sent to me"
        };
        ActionBar.Tab tab;
        for (int i = 0; i < tabs.length; i++) {
            tab = actionBar
                    .newTab()
                    .setText(tabs[i])
                    .setTabListener(this);
            actionBar.addTab(tab);
        }
    }

    private void navigateToLogin() {
        // Launch the login activity

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.action_settings:
                intent = new Intent(this, GestureActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_conversations:
                intent = new Intent(this, ConversationsListActivity.class);
                startActivity(intent);
                break;
            case R.id.action_contacts:
                intent = new Intent(this, ContactsActivity.class);
                // intent = new Intent(this, SearchResultsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_login:
                navigateToLogin();
                break;
            case R.id.action_logout:
                ParseUser.logOut();
                navigateToLogin();
                break;
            /*case R.id.action_signup:
                intent = new Intent(this, SignupActivity.class);
                startActivity(intent);
                break;*/
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        int position = tab.getPosition();

        // Code changed after implementing ViewPager
        mViewPager.setCurrentItem(position);

        // I keep the below old code for future reference

        // Create a new fragment instance
        /*Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new ActivityFeedFragment();
                break;
            case 1:
                fragment = new PhotoGalleryFragment();
                break;
            case 2:
                fragment = new PhotoReceivedFragment();
        }*/

        // `replace` will remove all currently added fragments
        // and then add. so it'll replace any existing fragment
        // that was added to the container
        //getFragmentManager().beginTransaction()
        //        .replace(android.R.id.content, fragment)
        //        .commit();
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

}

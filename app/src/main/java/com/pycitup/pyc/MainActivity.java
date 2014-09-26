package com.pycitup.pyc;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.UserDictionary;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;


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
            Log.i(TAG, currentUser.getUsername());
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
        for (int i = 0; i < 3; i++) {
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
                return true;
            case R.id.action_conversations:
                intent = new Intent(this, ConversationsActivity.class);
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

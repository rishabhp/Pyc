package com.pycitup.pyc;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.util.Log;

/**
 * Created by rishabhpugalia on 20/08/14.
 */
public class HomePagerAdapter extends FragmentPagerAdapter {

    protected Context mContext;

    public HomePagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    // This method returns the fragment associated with
    // the specified position.
    //
    // It is called when the Adapter needs a fragment
    // and it does not exists.
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new ActivityFeedFragment();
            case 1:
                return new PhotoGalleryFragment();
            case 2:
                return new PhotoReceivedFragment();
        }

        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }
}

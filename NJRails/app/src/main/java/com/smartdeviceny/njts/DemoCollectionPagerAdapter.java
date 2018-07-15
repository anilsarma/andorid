package com.smartdeviceny.njts;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;


/**
 * Created by asarma on 11/3/2017.
 */

public class DemoCollectionPagerAdapter extends FragmentStatePagerAdapter {
    int count = 5   ;
    public DemoCollectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new MainActivityFragment();
        Bundle args = new Bundle();
        // Our object is just an integer :-P
        args.putInt(MainActivityFragment.ARG_OBJECT, i );
        fragment.setArguments(args);
        return fragment;
    }

    void setCount(int value)
    {
        count = value;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position );
    }
}
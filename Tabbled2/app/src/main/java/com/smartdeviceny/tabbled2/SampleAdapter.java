package com.smartdeviceny.tabbled2;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SampleAdapter extends FragmentPagerAdapter {
    public SampleAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0: return new FragmentOne();
            case 1: return new FragmentOne();
            case 2: return new FragmentOne();
        }
      return null;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch(position) {
            case 0: return "Tab 1 item";
            case 1: return "Tab 2 item";
            case 2: return "Tab 3 item";
        }
        return "";
    }

    @Override
    public int getCount() {
        return 3;
    }
}

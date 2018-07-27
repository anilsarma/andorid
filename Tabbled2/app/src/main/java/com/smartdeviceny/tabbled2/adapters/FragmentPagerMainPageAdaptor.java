package com.smartdeviceny.tabbled2.adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.smartdeviceny.tabbled2.fragments.FragmentAlertWeb;
import com.smartdeviceny.tabbled2.fragments.FragmentDepartureVisionWeb;
import com.smartdeviceny.tabbled2.fragments.FragmentRouteSchedule;

public class FragmentPagerMainPageAdaptor extends FragmentPagerAdapter {
    public FragmentPagerMainPageAdaptor(FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0: return new FragmentDepartureVisionWeb();
            case 1: return new FragmentRouteSchedule();
            case 2: return new FragmentAlertWeb();
        }
      return null;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch(position) {
            case 0: return "Vision";
            case 1: return "Schedule";
            case 2: return "Alerts";
        }
        return "";
    }

    @Override
    public int getCount() {
        return 3;
    }
}

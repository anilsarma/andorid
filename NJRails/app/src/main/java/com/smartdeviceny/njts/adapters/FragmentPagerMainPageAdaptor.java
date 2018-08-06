package com.smartdeviceny.njts.adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.smartdeviceny.njts.fragments.FragmentAlertWeb;
import com.smartdeviceny.njts.fragments.FragmentDepartureVisionWeb;
import com.smartdeviceny.njts.fragments.FragmentRouteSchedule;
import com.smartdeviceny.njts.fragments.FragmentSettings;

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
            case 3: return new FragmentSettings();
            //case 4: return new FragmentSettings2();
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
            case 3: return "Settings";
            //case 4: return "Settings Experiment";
        }
        return "";
    }

    @Override
    public int getCount() {
        return 4;
    }
}

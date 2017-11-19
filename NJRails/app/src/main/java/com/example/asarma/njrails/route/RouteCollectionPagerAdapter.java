package com.example.asarma.njrails.route;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.asarma.njrails.ParcelResult;


/**
 * Created by asarma on 11/3/2017.
 */

public class RouteCollectionPagerAdapter extends FragmentStatePagerAdapter {
    FragmentManager fm;
    ParcelResult data;
    public RouteCollectionPagerAdapter(FragmentManager fm, ParcelResult data) {
        super(fm);
        this.fm = fm;
        this.data = data;
    }

    @Override
    public Fragment getItem(int i) {
        //switch(i)
        {
        //    default:

            {
                Fragment fragment = new RouteGoogleMapFragment();

                Bundle args = new Bundle();
                // Our object is just an integer :-P
                args.putInt(RouteGoogleMapFragment.ARG_OBJECT, i );
                args.putParcelable("data", this.data);
                fragment.setArguments(args);
                return fragment;
            }
           // case 1:
              //  break;
        }
     //   return fragment;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position );
    }
}
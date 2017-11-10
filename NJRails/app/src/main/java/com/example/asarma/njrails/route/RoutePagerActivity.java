package com.example.asarma.njrails.route;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.example.asarma.njrails.DemoCollectionPagerAdapter;
import com.example.asarma.njrails.MainActivityFragment;
import com.example.asarma.njrails.R;

public class RoutePagerActivity extends FragmentActivity {

    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    RouteCollectionPagerAdapter mDemoCollectionPagerAdapter;
    ViewPager mViewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_pager);
        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mDemoCollectionPagerAdapter =  new RouteCollectionPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.route_pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
        mViewPager.postDelayed(new Runnable() {
            @Override
            public void run() {
                mViewPager.setCurrentItem(MainActivityFragment.FIRST_PAGE);
                Toast.makeText(mViewPager.getContext(), "Current Act"+ mViewPager.getCurrentItem(), Toast.LENGTH_LONG).show();
            }
        }, 5000);
    }


}

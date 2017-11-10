package com.example.asarma.pager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by asarma on 11/3/2017.
 */

public  class DemoObjectFragment extends Fragment {
    public static final String ARG_OBJECT = "object";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(R.layout.activity_pager, container, false);
        Bundle args = getArguments();
        ((TextView) rootView.findViewById(R.id.pager_text)).setText("Pager " + Integer.toString(args.getInt(ARG_OBJECT)));
        return rootView;
    }
}
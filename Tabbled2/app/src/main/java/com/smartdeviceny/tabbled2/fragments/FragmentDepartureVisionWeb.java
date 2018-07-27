package com.smartdeviceny.tabbled2.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.smartdeviceny.tabbled2.R;
import com.smartdeviceny.tabbled2.SystemService;
import com.smartdeviceny.tabbled2.adapters.ServiceConnected;

public class FragmentDepartureVisionWeb extends Fragment implements ServiceConnected {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_departure_vision_web, container, false);
            //WebView webview = (WebView)view.findViewById(R.id.nj_map_view_layout);
            //webview.getSettings().setJavaScriptEnabled(true);
            return view;
        }catch (Exception e) {
            return null;
        }
        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onTimerEvent(SystemService systemService) {

    }

    @Override
    public void onSystemServiceConnected(SystemService systemService) {

    }

    @Override
    public void onDepartureVisionUpdated(SystemService systemService) {

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        WebView web = getActivity().findViewById(R.id.nj_map_view_layout);
        web.loadUrl("http://dv.njtransit.com/mobile/tid-mobile.aspx?sid=NY");
        web.getSettings().setBuiltInZoomControls(true);
        web.getSettings().setLoadWithOverviewMode(true);
        web.getSettings().setUseWideViewPort(true);

        super.onViewCreated(view, savedInstanceState);
        //Toast.makeText(getActivity().getApplicationContext(), "OnViewCreated", Toast.LENGTH_LONG).show();

    }
}

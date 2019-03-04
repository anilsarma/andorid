package com.smartdeviceny.njts.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.smartdeviceny.njts.MainActivity;
import com.smartdeviceny.njts.R;
import com.smartdeviceny.njts.SystemService;
import com.smartdeviceny.njts.adapters.ServiceConnected;

public class FragmentDepartureVisionWeb extends Fragment implements ServiceConnected {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            return inflater.inflate(R.layout.fragment_departure_vision_web, container, false);
        } catch (Exception e) {
            return null;
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        WebView web = getActivity().findViewById(R.id.depart_vision_web_view);
        web.loadUrl("http://dv.njtransit.com/mobile/tid-mobile.aspx?sid=" + ((MainActivity)getActivity()).getStationCode());
        web.getSettings().setBuiltInZoomControls(true);
        web.getSettings().setLoadWithOverviewMode(true);
        web.getSettings().setUseWideViewPort(true);
        web.getSettings().setJavaScriptEnabled(false);

        SwipeRefreshLayout swipeRefreshLayout = getActivity().findViewById(R.id.departure_vision_swipe_view);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SwipeRefreshLayout swipeRefreshLayout = getActivity().findViewById(R.id.departure_vision_swipe_view);
                WebView web = getActivity().findViewById(R.id.depart_vision_web_view);
                web.reload();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        super.onViewCreated(view, savedInstanceState);
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
    public void configChanged(SystemService systemService) {
        WebView web = getActivity().findViewById(R.id.depart_vision_web_view);
        if(web != null) {
            web.loadUrl("http://dv.njtransit.com/mobile/tid-mobile.aspx?sid=" + ((MainActivity) getActivity()).getStationCode());
        }
    }
}

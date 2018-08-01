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

import com.smartdeviceny.njts.R;
import com.smartdeviceny.njts.SystemService;
import com.smartdeviceny.njts.adapters.ServiceConnected;

public class FragmentAlertWeb extends Fragment implements ServiceConnected {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_alert_web, container, false);
            WebView webview = (WebView)view.findViewById(R.id.web_view);
            webview.getSettings().setJavaScriptEnabled(true);
            webview.getSettings().setSupportZoom(true);
            webview.getSettings().setBuiltInZoomControls(true);
            return view;
        }catch (Exception e) {
            return null;
        }
        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        WebView web = getActivity().findViewById(R.id.web_view);
        web.loadUrl("https://www.njtransit.com/sa/sa_servlet.srv?hdnPageAction=TravelAlertsTo");
        web.getSettings().setSupportZoom(true);
//        web.getSettings().setBuiltInZoomControls(true);
//        web.getSettings().setLoadWithOverviewMode(true);
//        web.getSettings().setUseWideViewPort(true);

        SwipeRefreshLayout swipeRefreshLayout = getActivity().findViewById(R.id.alert_swipe_view);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SwipeRefreshLayout swipeRefreshLayout = getActivity().findViewById(R.id.alert_swipe_view);
                WebView web = getActivity().findViewById(R.id.web_view);
                web.getSettings().setJavaScriptEnabled(false);
                
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

    }
}

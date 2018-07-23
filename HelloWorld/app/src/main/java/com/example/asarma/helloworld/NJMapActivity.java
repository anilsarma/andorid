package com.example.asarma.helloworld;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebView;

import com.example.asarma.helloworld.R;

public class NJMapActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nj_map_view_layout);

        WebView webView = (WebView) findViewById(R.id.nj_map_view_layout);
        if (webView==null) {
            webView = (WebView)getLayoutInflater().inflate(R.layout.nj_map_view_layout, null);

        }


// Fetch the picture in your folders using HTML
        String htmlData = "<img src=\"NJ_Transit_Rail_System_Map.jpg\">";
        webView.loadDataWithBaseURL("file:///android_asset/", htmlData, "text/html", "utf-8", null);
// Activate zooming
        webView.getSettings().setBuiltInZoomControls(true);
        //webView.setInitialScale(1);


    }
}

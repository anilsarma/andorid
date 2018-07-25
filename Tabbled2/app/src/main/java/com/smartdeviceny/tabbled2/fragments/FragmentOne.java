package com.smartdeviceny.tabbled2.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smartdeviceny.tabbled2.R;
import com.smartdeviceny.tabbled2.SystemService;
import com.smartdeviceny.tabbled2.adapters.ServiceConnected;

public class FragmentOne extends Fragment implements ServiceConnected{
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        inflater.inflate(R.layout.fragment_one, container, false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onTimerEvent(SystemService systemService) {

    }

    @Override
    public void onDepartureVisionUpdated(SystemService systemService) {

    }

    @Override
    public void onSystemServiceConnected(SystemService systemService) {

    }
}

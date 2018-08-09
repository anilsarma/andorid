package com.smartdeviceny.phonenumberblocker.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smartdeviceny.phonenumberblocker.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */
public  class BlockedNumberFragment extends Fragment {
    SharedPreferences config;
    BlockedNumberRecycleViewAdaptor adapter;
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    public BlockedNumberFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static BlockedNumberFragment newInstance(int sectionNumber) {
        BlockedNumberFragment fragment = new BlockedNumberFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //getArguments().get(ARG_SECTION_NUMBER)
        View view = inflater.inflate(R.layout.fragment_main, container, false);
       // RecyclerView recyclerView = view.findViewById(R.id.blockedNumberContainer);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = getActivity().findViewById(R.id.blockedNumberContainer);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));

        config = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        Set<String> entries = config.getStringSet("TerminatedNumbers", new HashSet<String>());
        ArrayList<String> numbers = new ArrayList<>(entries);

        adapter = new BlockedNumberRecycleViewAdaptor(getActivity(), numbers);
        recyclerView.setAdapter(adapter);

        SwipeRefreshLayout swipeRefreshLayout = getActivity().findViewById(R.id.fragment_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SwipeRefreshLayout swipeRefreshLayout = getActivity().findViewById(R.id.fragment_swipe_refresh);
                swipeRefreshLayout.setRefreshing(false);
                config = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                Set<String> entries = config.getStringSet("TerminatedNumbers", new HashSet<String>());
                adapter.updateData(new ArrayList<>(entries));
                adapter.notifyDataSetChanged();
            }
        });

        super.onViewCreated(view, savedInstanceState);
        //Toast.makeText(getActivity().getApplicationContext(), "OnViewCreated", Toast.LENGTH_LONG).show();
    }
}

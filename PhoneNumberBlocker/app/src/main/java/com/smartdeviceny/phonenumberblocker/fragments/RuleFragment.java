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
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.smartdeviceny.phonenumberblocker.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */
public  class RuleFragment extends Fragment {
    SharedPreferences config;
    BlockedNumberRecycleViewAdaptor adapter;
    ArrayAdapter<String> themeSpinnerAdapter;
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    public RuleFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static RuleFragment newInstance(int sectionNumber) {
        RuleFragment fragment = new RuleFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //getArguments().get(ARG_SECTION_NUMBER)
        View view = inflater.inflate(R.layout.rules, container, false);
        config = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = getActivity().findViewById(R.id.ruleEntries);
        final Spinner themeSpinner = getActivity().findViewById(R.id.themeSpinner);
        ArrayList<String> themes = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.Themes)));
        themeSpinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, themes);
        themeSpinner.setAdapter(themeSpinnerAdapter);

        int index = themes.indexOf(config.getString("Theme", themes.get(0)));
        themeSpinner.setSelection(index);
        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String value = (String)themeSpinner.getSelectedItem();
                String current = config.getString("Theme", "");
                Log.d("RULE", "Selected Theme:" + value);
                if ( !value.equals(current) ) {
                    config.edit().putString("Theme", value).commit();

//                    if( value.equals("Dark")) {
//                        getActivity().getApplication().setTheme(R.style.AppThemeDark);
//                        getActivity().recreate();
//                    } if( value.equals("Light")) {
//                        getActivity().getApplication().setTheme(R.style.AppTheme);
//                        getActivity().recreate();
//                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //recyclerView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));

        ItemTouchHelper handler = new ItemTouchHelper(new SwipeToDeleteCallback(getActivity()) {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                //adapter.removeAt(viewHolder.getAdapterPosition());
                adapter.notifyDataSetChanged();
            }
        });
        handler.attachToRecyclerView(recyclerView);

        // temp untill we have rules.
        config = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        Set<String> entries = config.getStringSet("TerminatedNumbers", new HashSet<String>());
        ArrayList<String> numbers = new ArrayList<>(entries);

        adapter = new BlockedNumberRecycleViewAdaptor(getActivity(), numbers);
        recyclerView.setAdapter(adapter);

        super.onViewCreated(view, savedInstanceState);
        //Toast.makeText(getActivity().getApplicationContext(), "OnViewCreated", Toast.LENGTH_LONG).show();
    }
}

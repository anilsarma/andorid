package com.smartdeviceny.tabbled2.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.smartdeviceny.tabbled2.MainActivity;
import com.smartdeviceny.tabbled2.R;
import com.smartdeviceny.tabbled2.SystemService;
import com.smartdeviceny.tabbled2.adapters.ServiceConnected;
import com.smartdeviceny.tabbled2.utils.Utils;

import java.util.ArrayList;

public class FragmentSettings extends Fragment implements ServiceConnected{

    ArrayAdapter<CharSequence> routes_adapter;
    ArrayAdapter<CharSequence> start_adapter;
    ArrayAdapter<CharSequence> stop_adapter;

    Spinner route_spinner;
    Spinner start_spinner;
    Spinner stop_spinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.settings, container, false);
        routes_adapter  = new ArrayAdapter<>(getActivity(), R.layout.spinner_item);
        start_adapter  = new ArrayAdapter<>(getActivity(), R.layout.spinner_item);
        stop_adapter  = new ArrayAdapter<>(getActivity(), R.layout.spinner_item);

        routes_adapter.setDropDownViewResource(R.layout.template_spinner_drop_down);
        start_adapter.setDropDownViewResource(R.layout.template_spinner_drop_down);
        stop_adapter.setDropDownViewResource(R.layout.template_spinner_drop_down);


        route_spinner = view.findViewById(R.id.routes_spinner);
        route_spinner.setAdapter(routes_adapter);

        start_spinner = view.findViewById(R.id.start_station_spinner);
        start_spinner .setAdapter(start_adapter);

        stop_spinner = view.findViewById(R.id.stop_station_spinner);
        stop_spinner.setAdapter(stop_adapter);

        route_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String route_name = adapterView.getSelectedItem().toString();
                if( ((MainActivity)getActivity()).systemService != null) {
                    updateStartStop(((MainActivity)getActivity()).systemService, route_name);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        Button sqlButton = (Button)view.findViewById(R.id.button_schedule_update);
        sqlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SystemService systemService=null;
                if( ((MainActivity)getActivity()).systemService != null ) {
                    systemService = ((MainActivity) getActivity()).systemService;
                }
                if (systemService!= null ) {
                    systemService.checkForUpdate();
                    if ( getMainActivity().progressDialog != null && getMainActivity().progressDialog.isShowing()) {
                        getMainActivity().progressDialog.dismiss();
                    }
                    if( systemService.isUpdateRunning()) {
                        getMainActivity().showUpdateProgressDialog(v.getContext());
                    }
                } else {
                    Log.d("BTNDNLD", "system service not init " + systemService );
                }
            }
        });


        return view;
        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity m = (MainActivity) getActivity();
        if (m.systemService != null ) {
            initData(m.systemService);
        }
    }


    public MainActivity getMainActivity() {
        return (MainActivity)getActivity();
    }
    public SystemService getSystemService() {
        if( ((MainActivity)getActivity()).systemService != null) {
            return ((MainActivity)getActivity()).systemService;
        }
        return null;
    }

    void initData(SystemService systemService ) {
        SharedPreferences config = getActivity().getPreferences(Context.MODE_PRIVATE);
        systemService.get_values("select * from routes", "route_long_name");
        String values[] = systemService.get_values( "select * from routes", "route_long_name");
        routes_adapter.clear();
        for(String rt: values ) {
            rt = Utils.capitalize(rt);
            Log.e("STG", " query retrieved " + rt);
            routes_adapter.insert(rt, routes_adapter.getCount());
        }
        routes_adapter.notifyDataSetChanged();

        values = systemService.getRouteStations(Utils.getConfig(config,
                getString(R.string.CONFIG_ROUTE), getString(R.string.CONFIG_DEFAULT_ROUTE)));
        start_adapter.clear();
        stop_adapter.clear();
        for(String value: values ) {
            Log.e("STG", " query start retrieved " + value);
            start_adapter.insert(Utils.capitalize(value), start_adapter.getCount());
            stop_adapter.insert(Utils.capitalize(value), stop_adapter.getCount());
        }
        start_adapter.notifyDataSetChanged();
        stop_adapter.notifyDataSetChanged();

        String rt = Utils.getConfig(config,
                getString(R.string.CONFIG_ROUTE), getString(R.string.CONFIG_DEFAULT_ROUTE));

        String start = Utils.getConfig(config,
                getString(R.string.CONFIG_START_STATION), getString(R.string.CONFIG_DEFAULT_START_STATION));
        String stop = Utils.getConfig(config,
                getString(R.string.CONFIG_STOP_STATION), getString(R.string.CONFIG_DEFAULT_STOP_STATION));

        route_spinner.setSelection(routes_adapter.getPosition(Utils.capitalize(rt)));
        start_spinner.setSelection(start_adapter.getPosition(Utils.capitalize(start)));
        stop_spinner.setSelection(stop_adapter.getPosition(Utils.capitalize(stop)));
        Button button = getView().findViewById(R.id.start_stop_submit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the selected values
                String route = route_spinner.getSelectedItem().toString();
                String start = start_spinner.getSelectedItem().toString();
                String stop = stop_spinner.getSelectedItem().toString();

                MainActivity m = (MainActivity)getActivity();
                if(m.systemService != null ){
                    ArrayList<SystemService.Route> rts = m.systemService.getRoutes(start, stop, null);
                    if(rts.isEmpty()) {
                        Toast.makeText(getActivity(),"No direct trains found for " + start + " to " + stop + " config not updated", Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getActivity(),"config updated", Toast.LENGTH_LONG).show();
                        Utils.setConfig(config, getString(R.string.CONFIG_ROUTE), route);
                        Utils.setConfig(config, getString(R.string.CONFIG_START_STATION), start);
                        Utils.setConfig(config, getString(R.string.CONFIG_STOP_STATION), stop);
                    }
                }
            }
        });
    }

    void updateStartStop(SystemService systemService, String route_name ) {
        SharedPreferences config = getActivity().getPreferences(Context.MODE_PRIVATE);
        String [] values = systemService.getRouteStations(route_name);

        start_adapter.clear();
        stop_adapter.clear();
        for(String value: values ) {
            Log.e("STG", " query start retrieved " + value);
            start_adapter.insert(Utils.capitalize(value), start_adapter.getCount());
            stop_adapter.insert(Utils.capitalize(value), stop_adapter.getCount());
        }
        start_adapter.notifyDataSetChanged();
        stop_adapter.notifyDataSetChanged();

        String start = Utils.getConfig(config,
                getString(R.string.CONFIG_START_STATION), getString(R.string.CONFIG_DEFAULT_START_STATION));
        String stop = Utils.getConfig(config,
                getString(R.string.CONFIG_STOP_STATION), getString(R.string.CONFIG_DEFAULT_STOP_STATION));

        route_spinner.setSelection(routes_adapter.getPosition(Utils.capitalize(route_name)));
        start_spinner.setSelection(start_adapter.getPosition(Utils.capitalize(start)));
        stop_spinner.setSelection(stop_adapter.getPosition(Utils.capitalize(stop)));

    }

    @Override
    public void onTimerEvent(SystemService systemService) {

    }

    @Override
    public void onDepartureVisionUpdated(SystemService systemService) {

    }

    @Override
    public void onSystemServiceConnected(SystemService systemService) {
        initData(systemService);
    }

    @Override
    public void configChanged(SystemService systemService) {
        if( route_spinner!=null) {
            if (route_spinner.getSelectedItem() != null) {
                updateStartStop(systemService, route_spinner.getSelectedItem().toString());
            }
        }
    }
}

package com.smartdeviceny.njts.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.smartdeviceny.njts.MainActivity;
import com.smartdeviceny.njts.R;
import com.smartdeviceny.njts.SystemService;
import com.smartdeviceny.njts.adapters.ServiceConnected;
import com.smartdeviceny.njts.utils.ConfigUtils;
import com.smartdeviceny.njts.utils.Utils;
import com.smartdeviceny.njts.values.Config;
import com.smartdeviceny.njts.values.ConfigDefault;

import java.util.ArrayList;

public class FragmentSettings extends Fragment implements ServiceConnected{

    ArrayAdapter<CharSequence> routes_adapter;
    ArrayAdapter<CharSequence> start_adapter;
    ArrayAdapter<CharSequence> stop_adapter;

    Spinner route_spinner;
    Spinner start_spinner;
    Spinner stop_spinner;
    TextView edittext_view_db_version;
    TextView edittext_app_version;
    EditText text_edit_delta_days;
    EditText edit_text_polling_frequency;
    Switch checkBox_debug;
    SharedPreferences config;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try { config = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());} catch (Exception e) { }
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
        Button sqlButton = view.findViewById(R.id.button_schedule_update);
        sqlButton.setOnClickListener(v -> ((MainActivity)getActivity()).doCheckForUpdate(getActivity()));

        edittext_app_version = view.findViewById(R.id.edittext_app_version);
        edittext_view_db_version = view.findViewById(R.id.edittext_db_version);
        edittext_view_db_version.setText("TBD");
        edittext_app_version.setText("TBD");
        text_edit_delta_days = view.findViewById(R.id.edit_text_delta_days);
        if ( config != null){
            text_edit_delta_days.setText(ConfigUtils.getConfig(config, Config.DELTA_DAYS, ConfigDefault.DELTA_DAYS));
        }
        text_edit_delta_days.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String value = charSequence.toString();
                if(!value.isEmpty()){
                    try {
                        //SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                        int v = Integer.parseInt(value);
                        if( v > 15 ) {
                            v = 1;
                        }
                        v = Math.min(5,v);
                        v = Math.max(0,v);
                        ConfigUtils.setConfig(config, Config.DELTA_DAYS, "" + v);
                        //Toast.makeText(getActivity(), "set delta days to value " + value, Toast.LENGTH_LONG).show();
                        ((MainActivity)getActivity()).doConfigChanged();
                    }catch(Exception e) {
                        Toast.makeText(getActivity(), "Failed to set value " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        edit_text_polling_frequency = view.findViewById(R.id.edit_text_polling_frequency);
        edit_text_polling_frequency.setText("10");
        try {
            String time = config.getString(Config.POLLING_TIME, ConfigDefault.POLLING_TIME);
            int int_time= Integer.parseInt(time)/1000;
            edit_text_polling_frequency.setText("" + int_time);
        } catch(Exception e) {}
        edit_text_polling_frequency.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String value = charSequence.toString();
                if(!value.isEmpty()){
                    try {
                        //SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                        int v = Integer.parseInt(value);
                        v = Math.abs(v) * 1000;
                        ConfigUtils.setConfig(config, Config.POLLING_TIME, "" + v);
                        ((MainActivity)getActivity()).doConfigChanged();
                    }catch(Exception e) {
                        Toast.makeText(getActivity(), "Failed to set value " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        checkBox_debug = view.findViewById(R.id.checkbox_debug);
        checkBox_debug.setChecked(config.getBoolean(Config.DEBUG, ConfigDefault.DEBUG));
        checkBox_debug.setOnCheckedChangeListener((compoundButton, b) -> {
            SharedPreferences.Editor edit = config.edit();
            edit.putBoolean(Config.DEBUG, b);
            edit.apply();

            Button debugUpgrade = view.findViewById(R.id.debugForceUpgrade);
            debugUpgrade.setVisibility(b?View.VISIBLE:View.INVISIBLE);
            view.invalidate();

        });

        Button debugUpgrade = view.findViewById(R.id.debugForceUpgrade);
        debugUpgrade.setVisibility(checkBox_debug.isChecked()?View.VISIBLE:View.INVISIBLE);
        debugUpgrade.setOnClickListener(v -> ((MainActivity)getActivity()).doForceCheckUpgrade(getActivity()));

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
        if(route_spinner ==null) {
            return; // system not initalized
        }
        config = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        systemService.get_values("select * from routes", "route_long_name");
        String values[] = systemService.get_values( "select * from routes", "route_long_name");

        edittext_view_db_version.setText(systemService.getDBVersion());
        edittext_app_version.setText(getActivity().getString(R.string.app_version));


        routes_adapter.clear();
        for(String rt: values ) {
            rt = Utils.capitalize(rt);
            //Log.e("STG", " query retrieved " + rt);
            routes_adapter.insert(rt, routes_adapter.getCount());
        }
        routes_adapter.notifyDataSetChanged();

        values = systemService.getRouteStations(Utils.getConfig(config, Config.ROUTE, ConfigDefault.ROUTE));
        start_adapter.clear();
        stop_adapter.clear();
        for(String value: values ) {
            //Log.e("STG", " query start retrieved " + value);
            start_adapter.insert(Utils.capitalize(value), start_adapter.getCount());
            stop_adapter.insert(Utils.capitalize(value), stop_adapter.getCount());
        }
        start_adapter.notifyDataSetChanged();
        stop_adapter.notifyDataSetChanged();

        String rt = Utils.getConfig(config,Config.ROUTE, ConfigDefault.ROUTE);

        String start = Utils.getConfig(config,Config.START_STATION, ConfigDefault.START_STATION);
        String stop = Utils.getConfig(config,Config.STOP_STATION, ConfigDefault.STOP_STATION);

        route_spinner.setSelection(routes_adapter.getPosition(Utils.capitalize(rt)));
        start_spinner.setSelection(start_adapter.getPosition(Utils.capitalize(start)));
        stop_spinner.setSelection(stop_adapter.getPosition(Utils.capitalize(stop)));
        Button button = getView().findViewById(R.id.start_stop_submit);
        button.setOnClickListener(view -> {
            // get the selected values
            String route = route_spinner.getSelectedItem().toString();
            String start1 = start_spinner.getSelectedItem().toString();
            String stop1 = stop_spinner.getSelectedItem().toString();

            MainActivity m = (MainActivity)getActivity();
            if(m.systemService != null ){
                int delta = -1;
                try {delta = Integer.parseInt(ConfigUtils.getConfig(config, Config.DELTA_DAYS, "" + delta)); } catch (Exception e){ }

                ArrayList<SystemService.Route> rts = m.systemService.getRoutes(start1, stop1, null, delta);
                if(rts.isEmpty()) {
                    Toast.makeText(getActivity(),"No direct trains found for " + start1 + " to " + stop1 + " config not updated", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(getActivity(),"config updated", Toast.LENGTH_LONG).show();
                    Utils.setConfig(config, Config.ROUTE, route);
                    Utils.setConfig(config, Config.START_STATION, start1);
                    Utils.setConfig(config, Config.STOP_STATION, stop1);
                }
            }
        });
    }

    void updateStartStop(SystemService systemService, String route_name ) {
        // SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String [] values = systemService.getRouteStations(route_name);

        start_adapter.clear();
        stop_adapter.clear();
        for(String value: values ) {
            //Log.e("STG", " query start retrieved " + value);
            start_adapter.insert(Utils.capitalize(value), start_adapter.getCount());
            stop_adapter.insert(Utils.capitalize(value), stop_adapter.getCount());
        }
        start_adapter.notifyDataSetChanged();
        stop_adapter.notifyDataSetChanged();

        String start = Utils.getConfig(config,Config.START_STATION, ConfigDefault.START_STATION);
        String stop = Utils.getConfig(config,Config.STOP_STATION, ConfigDefault.STOP_STATION);

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
        if(edittext_view_db_version != null ) {
            edittext_view_db_version.setText(systemService.getDBVersion());
        }

        if( route_spinner!=null) {
            if (route_spinner.getSelectedItem() != null) {
                updateStartStop(systemService, route_spinner.getSelectedItem().toString());
            }
        }
    }
}

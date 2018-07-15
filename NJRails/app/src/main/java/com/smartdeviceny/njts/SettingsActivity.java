package com.smartdeviceny.njts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.smartdeviceny.njts.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;


public class SettingsActivity extends Activity {
    SQLHelper dbHelper = null;
    HashMap<String, String> stationcodes = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        if (dbHelper == null) {
            dbHelper = new SQLHelper(getApplicationContext());
        }
        initStationCodes();
        final Spinner start_station_spinner = findViewById(R.id.start_station_spinner);
        final Spinner stop_station_spinner = findViewById(R.id.stop_station_spinner);

        String route_name = SQLHelper.get_user_pref_value(dbHelper.getReadableDatabase(), "route_name", "Northeast Corridor");
        String startStations[] = SQLHelper.getRouteStations(dbHelper.getReadableDatabase(), route_name);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this.getApplicationContext(), android.R.layout.simple_spinner_item, startStations);
        start_station_spinner.setAdapter(adapter);

        String start = SQLHelper.get_user_pref_value(dbHelper.getReadableDatabase(), "start_station", startStations[0]);
        start_station_spinner.setSelection(adapter.getPosition(Utils.capitalize(start)));

        stop_station_spinner.setAdapter(adapter);
        String stop = SQLHelper.get_user_pref_value(dbHelper.getReadableDatabase(), "stop_station", startStations[1]);

        stop_station_spinner.setSelection(adapter.getPosition(Utils.capitalize(stop)));


        String njt_routes[] = SQLHelper.get_values(dbHelper.getReadableDatabase(), "select * from routes", "route_long_name");
        for (int i = 0; i < njt_routes.length; i++) {
            njt_routes[i] = Utils.capitalize(njt_routes[i]);
        }
        ArrayAdapter<CharSequence> njt_adaptor = new ArrayAdapter<CharSequence>(getApplicationContext(), android.R.layout.simple_spinner_item, njt_routes);
        njt_adaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner routes_spinner = findViewById(R.id.routes_spinner);
        routes_spinner.setAdapter(njt_adaptor);
        routes_spinner.setSelection(njt_adaptor.getPosition(route_name));


        // setup the departure vision.
        Spinner departure_vision_station_spinner = (Spinner) findViewById(R.id.departure_vision_station_spinner);
        String codes[] = stationcodes.keySet().toArray(new String[]{});
        Arrays.sort(codes);
        ArrayAdapter<CharSequence> departure_vision_adaptor = new ArrayAdapter<CharSequence>(getApplicationContext(), android.R.layout.simple_spinner_item,  codes);
        departure_vision_adaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        departure_vision_station_spinner.setAdapter(departure_vision_adaptor);

        String departure_station = SQLHelper.get_user_pref_value(dbHelper.getReadableDatabase(), "departure_station", null);
        if ( departure_station != null ) {
            int idx = departure_vision_adaptor.getPosition(departure_station);
            if(idx >=0 ) {
                departure_vision_station_spinner.setSelection(idx);
            }
        }

        setupStartStopListeners();
        setupDepartureVisionListeners();

        routes_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String route_name = parent.getSelectedItem().toString();
                //Toast.makeText(rootView.getContext(), "Selected :" + route_name , Toast.LENGTH_LONG).show();
                // need to update adaptor value .. and refresh
                String startStations[] = SQLHelper.getRouteStations( dbHelper.getReadableDatabase(), route_name );
                ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getApplicationContext(), android.R.layout.simple_spinner_item,  startStations);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                start_station_spinner.setAdapter(adapter);
                stop_station_spinner.setAdapter(adapter);

                start_station_spinner.invalidate();
                stop_station_spinner.invalidate();
                adapter.notifyDataSetChanged();

                String start = SQLHelper.get_user_pref_value( dbHelper.getReadableDatabase(), "start_station", startStations[0] );
                String stop  = SQLHelper.get_user_pref_value( dbHelper.getReadableDatabase(), "stop_station", startStations[1] );

                int spinnerPosition = adapter.getPosition(Utils.capitalize(start));
                stop_station_spinner.setSelection(spinnerPosition);
                spinnerPosition = adapter.getPosition(Utils.capitalize(stop));
                start_station_spinner.setSelection(spinnerPosition);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    ArrayList<HashMap<String, Object>> routes = new ArrayList<>();

    void setupStartStopListeners() {
        Button button = (Button) findViewById(R.id.start_stop_submit);
        Button button_cancel = (Button) findViewById(R.id.start_stop_cancel);
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner st = (Spinner) findViewById(R.id.start_station_spinner);
                Spinner sp = (Spinner) findViewById(R.id.stop_station_spinner);
                Spinner routes_spinner = findViewById(R.id.routes_spinner);

                String start = st.getSelectedItem().toString();
                String stop = sp.getSelectedItem().toString();
                String route_name = routes_spinner.getSelectedItem().toString();

                if (start.isEmpty() || stop.isEmpty()) {
                    Toast.makeText(getApplicationContext(), (String) "Both start and stop stations must be selected",
                            Toast.LENGTH_LONG).show();
                    return;

                }

                SQLHelper.update_user_pref(dbHelper.getWritableDatabase(), "route_name", route_name, new Date());
                SQLHelper.update_user_pref(dbHelper.getWritableDatabase(), "start_station", start, new Date());
                SQLHelper.update_user_pref(dbHelper.getWritableDatabase(), "stop_station", stop, new Date());
                System.out.println("updated " +  route_name + " " + start + " "  + stop);

                Toast.makeText(getApplicationContext(), (String) "Start/Stop station updated",  Toast.LENGTH_LONG).show();
                Intent data = new Intent();

                data.putExtra("start_station", start);
                data.putExtra("stop_station", stop);
                setResult(RESULT_OK, data);

                finish();

            }
        });
    }
    void setupDepartureVisionListeners() {
        Button button = (Button) findViewById(R.id.departure_vision_submit);
        Button button_cancel = (Button) findViewById(R.id.departure_vision_cancel);
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner departure_vision_station_spinner = findViewById(R.id.departure_vision_station_spinner);
                String departure_station = departure_vision_station_spinner.getSelectedItem().toString();

                if( departure_station.isEmpty() ) {
                    Toast.makeText(getApplicationContext(),(String)"A station must be selected",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                String code = stationcodes.get(Utils.capitalize(departure_station));
                System.out.println("code" + code);
                if ( code == null ) {
                    Toast.makeText(getApplicationContext(),(String)"A departure vision code for the station " + departure_station + " Could not be found",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                SQLHelper.update_user_pref(dbHelper.getWritableDatabase(), "departure_code", code, new Date());
                SQLHelper.update_user_pref(dbHelper.getWritableDatabase(), "departure_station", departure_station, new Date());

                Intent data = new Intent();

                data.putExtra("departure_status", departure_station);
                data.putExtra("departure_code", code);
                setResult(RESULT_OK, data);
                SettingsActivity.this.finish();

            }
        });
    }

    private void initStationCodes()
    {
        if (stationcodes.size()==0) {
            ArrayList<HashMap<String, Object>> r = dbHelper.read_csv("station_codes.txt");
            //System.out.print(r);
            for (int i = 0; i < r.size(); i++) {
                try {
                    System.out.println("Station code " + Utils.capitalize(r.get(i).get("station_name").toString()) +"=" +  r.get(i).get("station_code").toString());
                    stationcodes.put(Utils.capitalize(r.get(i).get("station_name").toString()), r.get(i).get("station_code").toString());

                } catch (Exception e) {
                    // e.printStackTrace();
                }
            }
        }
    }
}

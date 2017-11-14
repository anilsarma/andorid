package com.example.asarma.njrails;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivityFragment extends Fragment {
    static int STATUS_PAGE=0;
    public static int FIRST_PAGE=1;

    public static final String ARG_OBJECT = "object";
    TrainStatusUpdate status = null;
    Date date = new Date(new Date().getTime()-6000000);
    ArrayList<HashMap<String, Object>> status_result = new ArrayList<HashMap<String, Object>>();
    HashMap<Integer, View> views = new HashMap<>();
    boolean sqlrequest = false;
    RouteActivity activity = new RouteActivity();
    SQLHelper dbHelper;
    HashMap<String, String> stationcodes = new HashMap<>();
    int multiplier=3;
    int myPage = 0;
    ArrayList<HashMap<String, Object>> routes = new ArrayList<>();

    View rootView=null;
    Timer timer = null;
    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        setupDB();

        // do this in the background

        if (stationcodes.size()==0) {
            ArrayList<HashMap<String, Object>> r = dbHelper.read_csv("station_codes.txt");
            //System.out.print(r);
            for (int i = 0; i < r.size(); i++) {
                try {
                    if (true) {
                        stationcodes.put(r.get(i).get("station_name").toString(), r.get(i).get("station_code").toString());
                    }
                } catch (Exception e) {
                    // e.printStackTrace();
                }
            }
        }
        stationcodes.put("New Brunswick", "NB");
        stationcodes.put("New York", "NY");
        System.out.println("Code Size" + stationcodes.size());
        System.out.println(stationcodes.keySet().toArray(new String[]{}));
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        Bundle args = getArguments();
        final int index = args.getInt(ARG_OBJECT);
        myPage = index;

        rootView = inflater.inflate(R.layout.route_layout, container, false);
        final SwipeRefreshLayout swipe = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);
        final MainActivityFragment frag = this;

        if(dbHelper==null ) {
            dbHelper = new SQLHelper(this.getContext());
        }
        if( !sqlrequest ) {
            sqlrequest = true;
            new UploadSqlDBTask(rootView, frag).execute();
        }

        View station_query = (View) rootView.findViewById(R.id.station_query);
        View status_query = (View) rootView.findViewById(R.id.status_query);
        Spinner start_station_spinner = (Spinner) rootView.findViewById(R.id.start_station_spinner);
        Spinner stop_station_spinner = (Spinner) rootView.findViewById(R.id.stop_station_spinner);
        Spinner routes_spinner = (Spinner) rootView.findViewById(R.id.routes_spinner);
        ViewGroup.LayoutParams params = station_query.getLayoutParams();
        params.height = 0;
        station_query.setLayoutParams(params);
        station_query.setVisibility(View.INVISIBLE);
        ViewGroup.LayoutParams params0 = status_query.getLayoutParams();
        params.height = 0;
        status_query.setLayoutParams(params);

        {
            ArrayList<HashMap<String, Object>> data = SQLHelper.query(dbHelper.getReadableDatabase(), "select * from routes");
            System.out.println(data);
        }
        String njt_routes[] = SQLHelper.get_values(dbHelper.getReadableDatabase(), "select * from routes", "route_long_name");
        for (int i = 0; i < njt_routes.length; i++) {
            njt_routes[i] = Utils.capitalize(njt_routes[i]);
        }
        ArrayAdapter<CharSequence> njt_adaptor = new ArrayAdapter<CharSequence>(rootView.getContext(), android.R.layout.simple_spinner_item,  njt_routes);
        String sql_stations = "select * from stops where stop_id in (select  distinct stop_id from stop_times where trip_id in ( select distinct trip_id from trips where route_id  = {route_id} ) );";

        String sql_route = "select * from routes where route_long_name like '%{route_name}%';".replace("{route_name}", "Northeast Corridor");
        System.out.println("SQL:" + sql_route);
        String route_id[]  = SQLHelper.get_values(dbHelper.getReadableDatabase(),sql_route, "route_id");
        sql_stations = sql_stations.replace("{route_id}", "" + route_id[0] );
        System.out.println("SQL:" + sql_stations );
        String startStations[] = SQLHelper.get_values( dbHelper.getReadableDatabase(), sql_stations, "stop_name");
        for (int i = 0; i < startStations.length; i++) {
            startStations[i] = Utils.capitalize(startStations[i]);
        }
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(rootView.getContext(), android.R.layout.simple_spinner_item,  startStations);

        njt_adaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        start_station_spinner.setAdapter(adapter);
        stop_station_spinner.setAdapter(adapter);
        routes_spinner.setAdapter(njt_adaptor);

        int spinnerPosition = njt_adaptor.getPosition("Northeast Corridor");
        routes_spinner.setSelection(spinnerPosition);
        System.out.println("Spinner count:"+spinnerPosition);

        String start = SQLHelper.get_user_pref_value( dbHelper.getReadableDatabase(), "start_station", "NEW BRUNSWICK");
        String stop  = SQLHelper.get_user_pref_value( dbHelper.getReadableDatabase(), "stop_station", "NEW YORK PENN STATION");

        SQLHelper.update_user_pref( dbHelper.getWritableDatabase(), "start_station", start, new Date());
        SQLHelper.update_user_pref( dbHelper.getWritableDatabase(), "stop_station", stop, new Date());


        spinnerPosition = adapter.getPosition(Utils.capitalize(start));
        stop_station_spinner.setSelection(spinnerPosition);
        spinnerPosition = adapter.getPosition(Utils.capitalize(stop));
        start_station_spinner.setSelection(spinnerPosition);

        params = stop_station_spinner.getLayoutParams();
        params.height= 100;
        stop_station_spinner.setLayoutParams(params);
        swipe.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        if (swipe.isRefreshing()) {
                            //return;
                        }
                        try {
                            ViewPager mViewPager = (ViewPager) frag.getActivity().findViewById(R.id.pager);

                            if (mViewPager != null ) {
                                Integer index = mViewPager.getCurrentItem();
                                View view = views.get(index);
                                Date now = new Date();
                                String status_code = SQLHelper.get_user_pref_value(dbHelper.getReadableDatabase(), "status_code", "NY");
                                if( status_result.isEmpty() || (( now.getTime() - date.getTime()) > 20000)) {
                                    new DownloadFilesTask(rootView, frag).execute("", status_code);
                                }
                                else {
                                    swipe.setRefreshing(false);
                                }
                            }
                            else {
                                Toast.makeText(getContext(),(String)"Pager not found", Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
        );

        views.put(new Integer(index), rootView);
        if( index >= FIRST_PAGE ) {
            Button button = (Button)rootView.findViewById(R.id.station_query_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Spinner st = (Spinner)view.getRootView().findViewById(R.id.start_station_spinner);
                    Spinner sp = (Spinner)view.getRootView().findViewById(R.id.stop_station_spinner);
                    String start = st.getSelectedItem().toString();
                    String stop = sp.getSelectedItem().toString();

                    if( start.isEmpty() || stop.isEmpty() ) {
                        Toast.makeText(getContext(),(String)"Both start and stop stations must be selected",
                                Toast.LENGTH_LONG).show();
                        return;

                    }
                    DateFormat dateFormat = new SimpleDateFormat("YYYYMMdd");
                    Calendar cal = Calendar.getInstance();
                    TimeZone tz = cal.getTimeZone();
                    dateFormat.setTimeZone(tz);
                    Date date = new Date();
                    int dt = Integer.parseInt(dateFormat.format(Utils.adddays(date, index-1)));
                    routes = Utils.parseCursor(SQLHelper.getRoutes(dbHelper.getReadableDatabase(), start, stop, dt ));
                    if( routes.isEmpty()) {
                        Toast.makeText(getContext(),(String)"Start " + start + " Stop:" + stop  + " are not valid destinations",
                                Toast.LENGTH_LONG).show();
                        return;

                    }

                    SQLHelper.update_user_pref(dbHelper.getWritableDatabase(), "start_station", start, new Date());
                    SQLHelper.update_user_pref(dbHelper.getWritableDatabase(), "stop_station", stop, new Date());
                    updateRoutes(rootView, start, stop, index-1, routes);
                    View station_query = (View) rootView.findViewById(R.id.station_query);
                    ViewGroup.LayoutParams params = station_query.getLayoutParams();
                    params.height = 0;
                    station_query.setLayoutParams(params);
                    station_query.setVisibility(View.INVISIBLE);
                }
            });
        }
        if ( index == STATUS_PAGE ) {
            if ( status == null) {
                status = new TrainStatusUpdate();
            }

            TextView th =(TextView) rootView.findViewById(R.id.route_header);
            // th.setText("Train Details + " + Integer.toString(args.getInt(ARG_OBJECT)));
            th.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View status_query = (View) rootView.findViewById(R.id.status_query);
                    ViewGroup.LayoutParams params = status_query.getLayoutParams();
                    if (params.height == 0) {
                        params.height = 800;
                        status_query.setVisibility(View.VISIBLE);
                    }
                    else {
                        params.height=0;
                        status_query.setVisibility(View.INVISIBLE);
                    }
                    status_query.setLayoutParams(params);
                }
            });

            /* move this to a section to a background thread */
            Spinner status_spinner = (Spinner) rootView.findViewById(R.id.station_status_spinner);
            String codes[] = stationcodes.keySet().toArray(new String[]{});
            ArrayAdapter<CharSequence> status_adaptor = new ArrayAdapter<CharSequence>(rootView.getContext(), android.R.layout.simple_spinner_item,  codes);
            status_adaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            status_spinner.setAdapter(status_adaptor);

            System.out.println("Status codes " + codes);
            String status_station = SQLHelper.get_user_pref_value(dbHelper.getReadableDatabase(), "status_code", null);
            if ( status_station != null ) {
                int idx = status_adaptor.getPosition(status_station);
                if(idx >=0 ) {
                    status_spinner.setSelection(idx);
                }
            }

            Button button = (Button)rootView.findViewById(R.id.status_query_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Spinner st = (Spinner)view.getRootView().findViewById(R.id.station_status_spinner);
                    String start = st.getSelectedItem().toString();

                    if( start.isEmpty() ) {
                        Toast.makeText(getContext(),(String)"A station must be selected",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    String code = stationcodes.get(start);
                    SQLHelper.update_user_pref(dbHelper.getWritableDatabase(), "status_code", code, new Date());
                    SQLHelper.update_user_pref(dbHelper.getWritableDatabase(), "status_station", start, new Date());
                    new DownloadFilesTask(rootView, frag).execute( start, code  );

                    View status_query = (View) rootView.findViewById(R.id.status_query);
                    ViewGroup.LayoutParams params = status_query.getLayoutParams();
                    params.height = 0;
                    status_query.setLayoutParams(params);
                    status_query.setVisibility(View.INVISIBLE);
                }
            });

            //((TextView) rootView.findViewById(R.id.route_header)).setText("Train Status + " + Integer.toString(args.getInt(ARG_OBJECT)));
            status.updateRoutes(rootView, status_result);
            try {
                String status_code = SQLHelper.get_user_pref_value(dbHelper.getReadableDatabase(), "status_code", "NY");
                new DownloadFilesTask( rootView, this).execute("", status_code);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else {
            TextView th =(TextView) rootView.findViewById(R.id.route_header);
            th.setText("Train Details + " + Integer.toString(args.getInt(ARG_OBJECT)));
            th.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View station_query = (View) rootView.findViewById(R.id.station_query);
                    ViewGroup.LayoutParams params = station_query.getLayoutParams();
                    if (params.height == 0) {
                        params.height = 800;
                        station_query.setVisibility(View.VISIBLE);
                    }
                    else {
                        params.height=0;
                        station_query.setVisibility(View.INVISIBLE);
                    }
                    station_query.setLayoutParams(params);
                }
            });
            TextView td =(TextView) rootView.findViewById(R.id.route_details);
            td.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String start = SQLHelper.get_user_pref_value( dbHelper.getReadableDatabase(), "start_station", "");
                    if (start == null ) {
                        return;
                    }
                    String stop = SQLHelper.get_user_pref_value( dbHelper.getReadableDatabase(), "stop_station", null);
                    if (stop == null ) {
                        return;
                    }
                    SQLHelper.update_user_pref(dbHelper.getWritableDatabase(), "start_station", stop, new Date());
                    SQLHelper.update_user_pref(dbHelper.getWritableDatabase(), "stop_station", start, new Date());

                    int days = index-1;
                    routes = Utils.parseCursor(SQLHelper.getRoutes(dbHelper.getWritableDatabase(), stop, start, Integer.parseInt(Utils.getLocaDate(days))));
                    updateRoutes(rootView, stop, start, days, routes);
                }
            });

            on_local_create(rootView, index, savedInstanceState);
        }

        return rootView;
    }
    public void updateAdapter(View view, Long s, ArrayList<HashMap<String, Object>> result) {
        // update thes

        if( s != 0 ) {
            if (result.isEmpty()) {
                return;
            }
        }
        status_result = result;
        final SwipeRefreshLayout swipe = (SwipeRefreshLayout)view.findViewById(R.id.swiperefresh);


        ViewPager mViewPager = (ViewPager) view.getRootView().findViewById(R.id.pager);
        view = null;
        if (mViewPager != null ) {
            Integer index = mViewPager.getCurrentItem();
            view = views.get(index);
            if ( status != null && index == STATUS_PAGE) {
                ((TextView) view.findViewById(R.id.route_header)).setText("Train Details " );
                status.updateRoutes(view, result);
            }
        }

        if(view != null ) {

            swipe.setRefreshing(false);
            System.out.println("set prefresing to false ins");
        }
        System.out.println("set prefresing to false out");
    }

    public void sqlUpdateComplete()
    {
        System.out.println("sql complete");
    }
    void setupDB()
    {
        if (dbHelper == null) {
            dbHelper = new SQLHelper(getContext());
        }
        //super.onCreate(savedInstanceState);
        SQLiteDatabase db= dbHelper.getWritableDatabase();

        try {

            if ( SQLHelper.check_table(db, "trips")== 0 ) {
                Toast.makeText(getContext(),(String)"updating Database Tables",
                        Toast.LENGTH_SHORT).show();
                RailHelper.create_tables(db);
                dbHelper.update_tables(db);
            }
        } catch (Exception e) {
            Toast.makeText(getContext(),(String)"Creating Database Tables",
                    Toast.LENGTH_SHORT).show();
            RailHelper.create_tables(db);
            dbHelper.update_tables(db);
        }
    }

    void on_local_create(View rootView, int index, Bundle savedInstanceState) {
        setupDB();
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();

        DateFormat dateFormat = new SimpleDateFormat("YYYYMMdd");
        dateFormat.setTimeZone(tz);
        Date date = new Date();
        int dt = Integer.parseInt(dateFormat.format(date));
        String start = "New York";
        String stop = "New Brunswick";
        {
            start = SQLHelper.get_user_pref_value( dbHelper.getReadableDatabase(), "start_station", start);
            stop = SQLHelper.get_user_pref_value( dbHelper.getReadableDatabase(), "stop_station", stop);
        }
        updateRoutes(rootView, start, stop, index-1, Utils.parseCursor(SQLHelper.getRoutes(dbHelper.getReadableDatabase(), start, stop, Integer.parseInt(Utils.getLocaDate(index-1)))));
    }

    TextView addTextView(Context context, ViewGroup parent, String text, int font_size, int padding)
    {
        TextView tv = new TextView(context);
        tv.setText( text);
        tv.setTextSize(Utils.pxFromDp(font_size, getContext()));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, font_size*multiplier);
        TableRow.LayoutParams params =  new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        //params.setMargins(0, 5, 0, 5);
        parent.addView(tv, params);
        tv.setPadding(Utils.pxFromSp(10, getContext()), Utils.pxFromSp(padding, getContext()) , 0, 0);
        return  tv;
    }
    void updateRoutes(View rootView, String start, String stop, int days, ArrayList<HashMap<String, Object>> routes)
    {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss EEEE");
        Date date = Utils.adddays(new Date(), days );

        dateFormat.setTimeZone(tz);
        SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm:ss");
        timeformat.setTimeZone(tz);

        TextView route_header = (TextView)rootView.findViewById(R.id.route_header);
        TextView route_details = (TextView)rootView.findViewById(R.id.route_details);
        //route_header.setBackgroundColor(Color.BLACK);
        route_header.setTextColor(Color.WHITE);

        route_header.setText( dateFormat.format(date));
        route_header.setTextSize(Utils.pxFromDp(5, getContext()));
        route_header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 5*multiplier);
        route_details.setTextSize(Utils.pxFromDp(5, getContext()));
        route_details.setTextSize(TypedValue.COMPLEX_UNIT_SP, 5*multiplier);

        route_details.setText(Utils.capitalize(start) + "-->" + Utils.capitalize(stop));
        route_details.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        // route_header.setTextColor(Color.BLUE);
        //setContentView(route_header);
        TableLayout tl = (TableLayout)rootView.findViewById(R.id.routes);
        tl.removeAllViews();
        int selected = 0;
        String favs = SQLHelper.get_user_pref_value(dbHelper.getReadableDatabase(), "favorites", "");
        ArrayList<String> fav = Utils.split(",", favs);
        int location = rootView.getScrollY();
        Date now = new Date();
        for(int i=0;i< routes.size();i ++) {
            HashMap<String, Object> data = routes.get(i);
            // departure_time, destnaton arrival time block_id, route_long namem duration
            final String departture_time = data.get("departure_time").toString();
            String destination_time = data.get("destination_time").toString();
            final String block_id = data.get("block_id").toString();
            final String route_name = data.get("route_long_name").toString();
            final String trip_id = data.get("trip_id").toString();
            if ( i == 0 ) {
                route_header.setText(route_name + " " + dateFormat.format(date) );
            }
            String time = "";
            try {
                Date st_time = timeformat.parse(departture_time);
                Date end_time = timeformat.parse(destination_time);
                long milli = end_time.getTime() - st_time.getTime();
                long minutes = milli/(60*1000);
                time = "" + new Long(minutes).toString() ;
            } catch (Exception e) {

            }

            TableLayout tl2 = new TableLayout(getContext());
            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 5, 0, 5);


            tl2.setLayoutParams(params);

            TableRow tr = new TableRow(getContext());
            tr.setBackgroundColor(Color.LTGRAY);
            TableRow.LayoutParams params0 = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            params0.setMargins(5, 5,5,5);

            tr.setLayoutParams(params0);
            TextView tv = new TextView(getContext());
            tv.setText( " " + Utils.formatToLocalTime(Utils.parseLocalTime(departture_time)) + " - " + Utils.formatToLocalTime(Utils.parseLocalTime(destination_time)));
            tv.setTextSize(Utils.pxFromDp(6, getContext()));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 6*multiplier);
            tr.addView(tv);

            TableRow tr2 = new TableRow(getContext());
            TableRow.LayoutParams params2 = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            params2.setMargins(0, 20, 0, 20);
            tr2.setLayoutParams(params2);

            // tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            tl2.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            tl2.addView(tr2, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            //tl2.addView(tr3, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            //addTextView(this, tl2, "" + block_id + " " + departture_time + " " + destination_time, 6, 10);
            addTextView(getContext(), tl2, "" + route_name + "#" + block_id, 5, 10);

            String msg = "" + time+ " minutes";
            String schedule = "";
            try {

                if ( days == 0 ) {
                    DateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    Date st_time = dateTimeFormat.parse(Utils.formatToLocalDate(now) + " "  + departture_time);
                    long diff = (st_time.getTime() - (now.getTime()))/(1000*60);

                    if ((diff >=-10) && (diff < 120)) {
                        if ( diff >=0 ) {
                            schedule= "    " + diff + " minutes";
                        }
                        if ( diff < 0 ) {
                            schedule= "    " + Math.abs(diff) + " minutes ago";
                        }
                    }


                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            TextView th0 = addTextView(getContext(), tl2, "", 5, 5); // just a blank line
            TextView th1 = addTextView(getContext(), tl2, schedule, 5, 0);

            if ( !schedule.isEmpty()) {
                th1.setBackgroundColor(Color.GREEN);
                if (schedule.contains("ago")) {
                    th1.setBackgroundColor(Color.RED);
                }
            }
            TextView th2  = addTextView(getContext(), tl2, msg, 5, 0);
            //addTextView(this, tl2, "" , 5, 5);
            //tl2.setOnClickListener( new TableRowListener(th, block_id + " "+ route_name + " " + departture_time));

            //new RouteTouchListener(tl2, block_id + " "+ route_name + " " + departture_time));
            boolean initstate = true;
            if ( fav.contains("" + block_id )) {
                initstate=false;
                tl2.setBackgroundColor(Color.parseColor("#18FFFF"));
            }
            tl2.setOnLongClickListener(new RouteLongClickListener(dbHelper, block_id, block_id + " "+ route_name + " " + departture_time, initstate));
            tl2.setOnTouchListener(new RouteTouchListener(dbHelper, this.getContext(), tl2, trip_id,  block_id, block_id + " "+ route_name + " " + departture_time, initstate));
            tl.addView(tl2, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            //selected += 1;

            Date ddtime = Utils.parseLocalTime(departture_time);
            if ( ddtime.getTime() < now.getTime()) {
                int sz = tl2.getBottom() - tl2.getTop();
                if ( sz <= 0 ) {
                    sz = 70;
                }
                selected = i * sz;
            }

            for ( String key:data.keySet()
                    ) {
                Object value = data.get(key);
                //System.out.println("key=" + key + "=" + value);
            }

        }



        final int pxdown = location ;//* 200; selected
        final NestedScrollView v =(NestedScrollView) rootView.getRootView().findViewById(R.id.route_scroll);
        ViewTreeObserver vto = v.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            boolean done = false;
            @Override
            public void onGlobalLayout() {
                if(!done)  {
                    v.scrollTo(0, pxdown);
                    Toast.makeText(getContext(),(String)"Pager scroll to " + pxdown, Toast.LENGTH_SHORT).show();
                }
                done= true;
            }
        });


    }

    void buildTable(View rootView)
    {
        TableLayout tl = (TableLayout)rootView.findViewById(R.id.menu);
/* Create a new row to be added. */
        TableRow tr = new TableRow(getContext());
        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
/* Create a Button to be the row-content. */

        TextView tv = new TextView(getContext());
        tv.setText("Some text");
        tr.addView(tv);
        tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        Button b = new Button(getContext());
        b.setText("Dynamic Button");
        b.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
/* Add Button to row. */
        tr.addView(b);
        tv.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TableLayout tl = (TableLayout)v.getRootView().findViewById(R.id.menu);
/* Create a new row to be added. */
                TableRow tr = new TableRow(v.getContext());
                tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
/* Create a Button to be the row-content. */

                TextView tv = new TextView(v.getContext());
                tv.setText("Some text");
                tr.addView(tv);
                tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                Button b = new Button(v.getContext());
                b.setText("Dynamic Button");
                b.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
/* Add Button to row. */
                tr.addView(b);
                b.setOnClickListener( new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                    }
                });
/* Add row to TableLayout. */
//tr.setBackgroundResource(R.drawable.sf_gradient_03);
                tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            }
        });
/* Add row to TableLayout. */
//tr.setBackgroundResource(R.drawable.sf_gradient_03);
        tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

    }

    public void showDialog(final String phone) throws Exception
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Ring: " + phone);

        builder.setPositiveButton("Ring", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);// (Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phone));
                //startActivity(callIntent);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Avbryt", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        builder.show();
    }
    class UpdateTimeTask extends TimerTask {
        View rootView;
        UpdateTimeTask(View rootView) {
            this.rootView = rootView;
        }
        public void run() {
            //Code for the viewPager to change view
            rootView.postInvalidate();
            System.out.println("timer expired.");
            rootView.post(new Runnable() {
                @Override
                public void run() {
                    refresh();
                }
            });
        }
    }
public void refresh() {
    String start = SQLHelper.get_user_pref_value(dbHelper.getReadableDatabase(), "start_station", "");
    if (start == null) {
        return;
    }
    String stop = SQLHelper.get_user_pref_value(dbHelper.getReadableDatabase(), "stop_station", null);
    if (stop == null) {
        return;
    }
    int days = myPage - 1;
    routes = Utils.parseCursor(SQLHelper.getRoutes(dbHelper.getWritableDatabase(), stop, start, Integer.parseInt(Utils.getLocaDate(days))));
    if (!routes.isEmpty()) {
        updateRoutes(rootView, stop, start, days, routes);
    }
}
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if ( myPage == FIRST_PAGE ) {
            if (visible && rootView != null) {
                refresh();
                timer = new Timer();
                timer.schedule(new UpdateTimeTask(rootView), 30000, 30000);
            }

            if ( !visible) {
                if ( timer != null ) {
                    timer.cancel();
                    timer = null;
                }
            }
        }
    }
}

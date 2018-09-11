package com.smartdeviceny.njts.fragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.smartdeviceny.njts.MainActivity;
import com.smartdeviceny.njts.R;
import com.smartdeviceny.njts.SystemService;
import com.smartdeviceny.njts.adapters.RecycleSheduleAdaptor;
import com.smartdeviceny.njts.adapters.ServiceConnected;
import com.smartdeviceny.njts.utils.ConfigUtils;
import com.smartdeviceny.njts.utils.Utils;
import com.smartdeviceny.njts.values.Config;
import com.smartdeviceny.njts.values.ConfigDefault;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class FragmentRouteSchedule extends Fragment implements ServiceConnected {
    RecycleSheduleAdaptor adapter;
    SharedPreferences config;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View  view =  inflater.inflate(R.layout.fragment_njt_schedule, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.schedule_vision_scroll_view);
        // get the departure vision data.

        int height  = (int)(getResources().getDimensionPixelSize(R.dimen.recycler_section_header_height )*2.5); // TODO:: move this to config or something.
        RecyclerRouteDecoration sectionItemDecoration =new RecyclerRouteDecoration(height,true, getSectionCallback());
        //getResources().getDimensionPixelSize(R.dimen.recycler_section_header_height),true, getSectionCallback());
        recyclerView.addItemDecoration(sectionItemDecoration);
        return view;
    }

    String getConfig(SharedPreferences config, String name, String defaultValue) {
        return config.getString(name, defaultValue);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = getActivity().findViewById(R.id.schedule_vision_scroll_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        config = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        ArrayList<SystemService.Route> routes = new ArrayList<>();

        adapter = new RecycleSheduleAdaptor(getActivity(), routes);
        SystemService systemService = ((MainActivity)getActivity()).systemService;
        if( systemService !=null ) {
            refreshRouteAdaptor(systemService); //
        }

        //adapter.setClickListener(getc);
        recyclerView.setAdapter(adapter);

        SwipeRefreshLayout swipeRefreshLayout = getActivity().findViewById(R.id.fragment_njt_schedule);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SwipeRefreshLayout swipeRefreshLayout = getActivity().findViewById(R.id.fragment_njt_schedule);
                swipeRefreshLayout.setRefreshing(false);
                if( ((MainActivity)getActivity()).systemService  != null ) {
                    ((MainActivity)getActivity()).systemService.schdeuleDepartureVision(((MainActivity)getActivity()).getStationCode(), 0);
                }
            }
        });
        super.onViewCreated(view, savedInstanceState);
        //Toast.makeText(getActivity().getApplicationContext(), "OnViewCreated", Toast.LENGTH_LONG).show();
    }



    @Override
    public void onTimerEvent(SystemService systemService) {
        // give it a little kick.
        RecyclerView recyclerView = getActivity().findViewById(R.id.schedule_vision_scroll_view);
        adapter.notifyDataSetChanged();
        recyclerView.invalidate();
    }

    @Override
    public void onDepartureVisionUpdated(SystemService systemService) {
        // get the departure vision data.
        HashMap<String, SystemService.DepartureVisionData> data =  systemService.getCachedDepartureVisionStatus_byTrip();
        data = (data==null)?new HashMap<>():data;
        if(data.isEmpty() ) {
            //nothing to do
            return;
        }
        adapter.updateDepartureVision(data);
        adapter.notifyDataSetChanged();
        RecyclerView recyclerView = getActivity().findViewById(R.id.schedule_vision_scroll_view);
        if(recyclerView !=null) {
            recyclerView.invalidate();
        }
        //TODO: this is expensive need to remove in the future.
        ((MainActivity)getActivity()).systemService.updateActiveDepartureVisionStation(((MainActivity)getActivity()).getStationCode());
        for(SystemService.DepartureVisionData dv:data.values()) {
           try {
               if(notifyUser(dv) ) {
                   break;
               }
           } catch(Exception e ) {

           }
        }
    }


    @Override
    public void onSystemServiceConnected(SystemService systemService) {
        refreshRouteAdaptor(systemService);
    }

    public void refreshRouteAdaptor(SystemService systemService) {

        RecyclerView recyclerView = getActivity().findViewById(R.id.schedule_vision_scroll_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        String startStation = getConfig(config, Config.START_STATION,  ConfigDefault.START_STATION);
        String stopStation = getConfig(config, Config.STOP_STATION, ConfigDefault.STOP_STATION);
        String departureVisionCode = ConfigUtils.getConfig(config, Config.DV_STATION, ConfigDefault.DV_STATION);

        int delta = -1;
        try {delta = Integer.parseInt(ConfigUtils.getConfig(config, Config.DELTA_DAYS, "" + delta)); } catch (Exception e){ }
        ArrayList<SystemService.Route> routes = systemService.getRoutes(startStation, stopStation, null, delta);

        systemService.schdeuleDepartureVision(departureVisionCode, 30000);
        //Log.d("FRAGRT", "updated routes start:" + startStation + " stop:"  + stopStation);

        adapter.updateRoutes(routes);
        adapter.notifyDataSetChanged();
        onDepartureVisionUpdated(systemService); // inital paint.
        // we do not want to do this all the time just when we reconnect.
        recyclerView.scrollToPosition(getPosition(routes));
        Log.d("FRAGRT", "updated routes ");
    }
    int getPosition(ArrayList<SystemService.Route> routes) {
        int index = -1;
        int i = 0;
        Date now = new Date();
        try {
            for (SystemService.Route rt : routes) {
                if (rt.departure_time_as_date.getTime() > now.getTime()) {
                    break; // we want this to be in the middle of the page some what.
                }
                index = i;
                i++;
            }
        } catch (Exception e) {
        }
        return index;
    }
    boolean notifyUser( SystemService.DepartureVisionData dv) throws ParseException
    {
        // do this for favs only.
        if (dv == null) {
            return false;
        }
        if(!dv.favorite || dv.stale) {
            return false;
        }
        if( dv.status.isEmpty() && dv.track.isEmpty()) {
            return false;
        }
        long diff = Utils.makeDate(Utils.getTodayYYYYMMDD(null),  dv.time, "yyyyMMdd HH:mm").getTime() - new Date().getTime();
        if (diff > 0 ) {  // other checks needed.
            String msg  = "Train " + dv.block_id + " departs " + dv.time + " from " + dv.station;
            if( !dv.track.isEmpty()) {
                msg += " Track " + dv.track;
            }
            if( !dv.status.isEmpty()) {
                msg += " " + dv.status;
            }
            Utils.notify_user(this.getActivity(), "NJTS", "NJTS", msg, 3);
            //Toast.makeText(getActivity().getApplicationContext(), (String) "sent notification ", Toast.LENGTH_SHORT).show();
            return true;
            //notification = true;
        }
        return  false;
    }


    final DateFormat printableDateFmt = new SimpleDateFormat("EEE, MMM d, yyyy");
    private RecyclerRouteDecoration.SectionCallback getSectionCallback() {
        return new RecyclerRouteDecoration.SectionCallback() {
            @Override
            public boolean isSection(int position) {
                adapter.mRoutes.size();
                // header from the last and current is different we we are a header.
                return position == 0  || !adapter.mRoutes.get(position).header.equals(adapter.mRoutes.get(position - 1).header);
            }

            @Override
            public CharSequence getSectionHeader(int position) {
                position = Math.max(position, 0);
                position = Math.min( position, adapter.mRoutes.size()-1);
                return adapter.mRoutes.get(position).from + " \u279F " + adapter.mRoutes.get(position).to;
            }

            @Override
            public CharSequence getSectionDate(int position) {
                position = Math.max(position, 0);
                position = Math.min(position, adapter.mRoutes.size()-1);
                return printableDateFmt.format(adapter.mRoutes.get(position).departure_time_as_date);
            }
        };
    }

    @Override
    public void configChanged(SystemService systemService) {
        Log.d("FRAGRT", "Route Schedule, onConfig Changed");
        if(adapter != null ) {
            adapter.clearData();
        }
        onSystemServiceConnected(systemService);

    }
}

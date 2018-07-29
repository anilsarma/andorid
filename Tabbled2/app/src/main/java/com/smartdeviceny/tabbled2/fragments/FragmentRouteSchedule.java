package com.smartdeviceny.tabbled2.fragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import com.smartdeviceny.tabbled2.MainActivity;
import com.smartdeviceny.tabbled2.R;
import com.smartdeviceny.tabbled2.SystemService;
import com.smartdeviceny.tabbled2.adapters.RecycleSheduleAdaptor;
import com.smartdeviceny.tabbled2.adapters.ServiceConnected;
import com.smartdeviceny.tabbled2.utils.Config;
import com.smartdeviceny.tabbled2.utils.Utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class FragmentRouteSchedule extends Fragment implements ServiceConnected {
    RecycleSheduleAdaptor adapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View  view =  inflater.inflate(R.layout.fragment_njt_schedule, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.schedule_vision_scroll_view);
       // View tmp = inflater.inflate(R.layout.route_section_header, null, null);

        int height = view.getMeasuredHeight();
        height = (int)(getResources().getDimensionPixelSize(R.dimen.recycler_section_header_height )*2.5);
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
        SharedPreferences config = getActivity().getPreferences(Context.MODE_PRIVATE);
        ArrayList<SystemService.Route> routes = new ArrayList<>();

        if( ((MainActivity)this.getActivity()).systemService  == null ) {
            Log.d("FRAG", "System Service i null ");
        }
        else {
            String startStation = getConfig(config, getString(R.string.CONFIG_START_STATION), getString(R.string.CONFIG_DEFAULT_START_STATION));
            String stopStation = getConfig(config, getString(R.string.CONFIG_STOP_STATION), getString(R.string.CONFIG_DEFAULT_STOP_STATION));
            int delta = -1;
            try {delta = Integer.parseInt(Config.getConfig(config, getString(R.string.CONFIG_DELTA_DAYS), "" + delta)); } catch (Exception e){ }
            routes = ((MainActivity)this.getActivity()).systemService.getRoutes(startStation, stopStation, null, delta);
        }
        Log.d("FRAG", "onViewCreated");
        adapter = new RecycleSheduleAdaptor(getActivity(), routes);
        //adapter.setClickListener(getc);
        recyclerView.setAdapter(adapter);

        SwipeRefreshLayout swipeRefreshLayout = getActivity().findViewById(R.id.fragment_njt_schedule);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SwipeRefreshLayout swipeRefreshLayout = getActivity().findViewById(R.id.fragment_njt_schedule);
                swipeRefreshLayout.setRefreshing(false);
                if( ((MainActivity)getActivity()).systemService  != null ) {
                    ((MainActivity)getActivity()).systemService.getDepartureVision("NY", 0);
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
        if(data.isEmpty() ) {
            //nothing to do
            return;
        }
        adapter.updateDepartureVision(data);
        adapter.notifyDataSetChanged();
        RecyclerView recyclerView = getActivity().findViewById(R.id.schedule_vision_scroll_view);
        if(recyclerView !=null) {
            //Toast.makeText(getActivity().getApplicationContext(), (String) "invalidate ", Toast.LENGTH_SHORT).show();
            adapter.notifyDataSetChanged();
            recyclerView.invalidate();
        }
        ((MainActivity)getActivity()).systemService.updateDapartureVisionCheck("NY");
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
        RecyclerView recyclerView = getActivity().findViewById(R.id.schedule_vision_scroll_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ArrayList<SystemService.Route> routes = new ArrayList<>();

        if( ((MainActivity)this.getActivity()).systemService  == null ) {
            Log.d("FRAG", "System Service i null ");
        }
        else {
            SharedPreferences config = getActivity().getPreferences(Context.MODE_PRIVATE);
            String startStation = getConfig(config, getString(R.string.CONFIG_START_STATION), getString(R.string.CONFIG_DEFAULT_START_STATION));
            String stopStation = getConfig(config, getString(R.string.CONFIG_STOP_STATION), getString(R.string.CONFIG_DEFAULT_STOP_STATION));

            String departureVisionCode = "NY"; // lookup value
            int delta = -1;
            try {delta = Integer.parseInt(Config.getConfig(config, getString(R.string.CONFIG_DELTA_DAYS), "" + delta)); } catch (Exception e){ }
            routes = ((MainActivity)this.getActivity()).systemService.getRoutes(startStation, stopStation, null, delta);

            ((MainActivity)getActivity()).systemService.getDepartureVision(departureVisionCode, 30000);
            Log.d("FRAG", "updated routes start:" + startStation + " stop:"  + stopStation);
        }
        adapter.updateRoutes(routes);
        adapter.notifyDataSetChanged();
        onDepartureVisionUpdated(systemService); // inital paint.
        // we do not want to do this all the time just when we reconnect.
        recyclerView.scrollToPosition(getPosition(routes));
        Log.d("FRAG", "updated routes ");
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
        if (dv == null) {
            return false;
        }
        if(true) {
            return false;
        }
        long diff = Utils.makeDate(Utils.getTodayYYYYMMDD(null),  dv.time, "yyyyMMdd HH:mm").getTime() - new Date().getTime();
        if (diff > 0 ) {  // other checks needed.
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getActivity(), "FRAG_SVC")
                           // .setSmallIcon(R.mipmap.ic_launcher)
                            .setTicker("NJS")
                            .setContentTitle("Train " + dv.block_id + " Track# " + dv.track  )
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setContentText( Utils.formatToLocalTime(Utils.parseLocalTime(dv.time)) + " departure in " +  Math.abs(diff) + "(mins)" );
            NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notify = mBuilder.build();
            notify.flags|= Notification.FLAG_AUTO_CANCEL;
            mNotificationManager.notify(1, notify);

            Toast.makeText(getActivity().getApplicationContext(), (String) "sent notification ", Toast.LENGTH_SHORT).show();
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
                adapter.mData.size();
                // header from the last and current is different we we are a header.
                return position == 0  || !adapter.mData.get(position).header.equals(adapter.mData.get(position - 1).header);
            }

            @Override
            public CharSequence getSectionHeader(int position) {
                return adapter.mData.get(position).from + " \u279F " + adapter.mData.get(position).to;
            }

            @Override
            public CharSequence getSectionDate(int position) {
                return printableDateFmt.format(adapter.mData.get(position).departure_time_as_date);
            }
        };
    }

    @Override
    public void configChanged(SystemService systemService) {
        onSystemServiceConnected(systemService);
    }
}

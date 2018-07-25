package com.smartdeviceny.tabbled2.fragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import com.smartdeviceny.tabbled2.MainActivity;
import com.smartdeviceny.tabbled2.R;
import com.smartdeviceny.tabbled2.SystemService;
import com.smartdeviceny.tabbled2.adapters.RecycleSheduleAdaptor;
import com.smartdeviceny.tabbled2.adapters.ServiceConnected;
import com.smartdeviceny.tabbled2.utils.Utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class FragmentSchedule extends Fragment implements ServiceConnected {
    RecycleSheduleAdaptor adapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_njt_schedule, container, false);
        return view;
        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = getActivity().findViewById(R.id.schedule_vision_scroll_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));

        ArrayList<SystemService.Route> routes = new ArrayList<>();

        if( ((MainActivity)this.getActivity()).systemService  == null ) {
            Log.d("FRAG", "System Service i null ");
        }
        else {
            routes = ((MainActivity)this.getActivity()).systemService.getRoutes("New York Penn Station", "New Brunswick", null);
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

//        WebView web = getActivity().findViewById(R.id.nj_map_view_layout);
//        web.loadUrl("http://dv.njtransit.com/mobile/tid-mobile.aspx?sid=NY");
//        web.getSettings().setBuiltInZoomControls(true);
//        web.getSettings().setLoadWithOverviewMode(true);
//        web.getSettings().setUseWideViewPort(true);
//
//        NestedScrollView scrollView = getActivity().findViewById(R.id.departure_vision_scroll_view);
//        //scrollView.getChildAt();
//        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
//            @Override
//            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//                SwipeRefreshLayout swipeRefreshLayout = getActivity().findViewById(R.id.departure_vision_layout);
//                //LinearLayout llParent = findViewById(R.id.departure_vision_scroll);
//                swipeRefreshLayout.setEnabled(scrollY ==0); // enable only if at the top.
//            }
//        });

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

        //Log.d("FRAGS", "got departure vision " + data.size());
//        for(String dv: data.keySet()) {
//            Log.d("FRAGS", "got departure vision " + dv + " " + data.get(dv).line);
//        }

        adapter.updateDepartureVision(data);
        adapter.notifyDataSetChanged();
        RecyclerView recyclerView = getActivity().findViewById(R.id.schedule_vision_scroll_view);
        if(recyclerView !=null) {
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
            routes = ((MainActivity)this.getActivity()).systemService.getRoutes("New York Penn Station", "New Brunswick", null);
            ((MainActivity)getActivity()).systemService.getDepartureVision("NY", 30000);
        }
        adapter.updateRoutes(routes);
        adapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(getPosition(routes));


        Log.d("FRAG", "updated routes");
    }
    int getPosition(ArrayList<SystemService.Route> routes) {
        int index = -1;
        int i = 0;
        Date now = new Date();
        try {
            for (SystemService.Route rt : routes) {
                if (rt.getDate(rt.arrival_time).getTime() > now.getTime()) {
                    return i;
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
        long diff = Utils.makeDate(Utils.getTodayYYYYMMDD(null),  dv.time).getTime() - new Date().getTime();
        if (diff > 0 ) {  // other checks needed.
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getActivity().getApplicationContext())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setTicker("NJS")
                            .setContentTitle("Train " + dv.block_id + " Track# " + dv.track  )
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setContentText( Utils.formatToLocalTime(Utils.parseLocalTime(dv.time)) + " departure in " +  Math.abs(diff) + "(mins)" );
            // NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            Notification notify = mBuilder.build();

            notify.flags|= Notification.FLAG_AUTO_CANCEL;
            final NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, notify);

            Toast.makeText(getActivity().getApplicationContext(), (String) "sent notification ", Toast.LENGTH_SHORT).show();
            return true;
            //notification = true;
        }
        return  false;

    }
}

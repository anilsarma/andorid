package com.example.asarma.helloworld.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.asarma.helloworld.R;
import com.example.asarma.helloworld.RemoteBinder;
import com.example.asarma.helloworld.SystemService;
import com.example.asarma.helloworld.utils.Utils;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

public class DepartureVisionActivity extends Activity {
    boolean mIsBound = false;
    SystemService systemService;


    @Override
    protected void onStart() {
        super.onStart();

        doBindService();

        setContentView(R.layout.departure_vision_layout);
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.departure_vision_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (systemService != null) {
                    systemService.getDepartureVision("NY");
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        TextView departure_vision_header = findViewById(R.id.departure_vision_header);
        departure_vision_header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DepartureVisionActivity.this.finish();
            }
        });
        //updateTables();
        swipeRefreshLayout.setEnabled(true);
        ScrollView scrollView = findViewById(R.id.departure_vision_scroll_view);
        //scrollView.getChildAt();
        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                //LinearLayout llParent = findViewById(R.id.departure_vision_scroll);
                swipeRefreshLayout.setEnabled(scrollY ==0); // enable only if at the top.
            }
        });

//        listView = (ListView) findViewById(R.id.listView);
//        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                if (listView.getChildAt(0) != null) {
//                    swipeRefreshLayout.setEnabled(listView.getFirstVisiblePosition() == 0 && listView.getChildAt(0).getTop() == 0);
//                }
//            }
//        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);

    }

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("departure-vision-updated");
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
        updateTables();
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        super.onPause();
    }

    void updateTables() {
        if( systemService!=null) {
            LinearLayout parent = findViewById(R.id.departure_vision_scroll);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            parent.removeAllViews();

            HashMap<String, ArrayList<HashMap<String, Object>>> dv_status = systemService.getCachedDepartureVisionStatus();
            for(String station_code:dv_status.keySet() ) {
                ArrayList<HashMap<String, Object>> entries = dv_status.get(station_code);
//                data.put("time", time);
//                data.put("to", to);
//                data.put("track", track);
//                data.put("line", line);
//                data.put("status", status);
//                data.put("train", train);
//                data.put("station", station);
                for(HashMap<String, Object> departure: entries ) {
                    String time = departure.get("time").toString();
                    String to = departure.get("to").toString();
                    String track = departure.get("track").toString();
                    String line = departure.get("line").toString();
                    String status = departure.get("status").toString();
                    String train = departure.get("train").toString();
                    String station = departure.get("station").toString();

                    View dv_item = inflater.inflate(R.layout.departure_vision_layout_template, null);
                    ((TextView)dv_item.findViewById(R.id.train_name)).setText(Utils.capitalize(line ) + " #" + train);
                    ((TextView)dv_item.findViewById(R.id.track_number)).setText(track);
                    ((TextView)dv_item.findViewById(R.id.track_number)).setVisibility(track.isEmpty()?View.INVISIBLE:View.VISIBLE);

                    dv_item.findViewById(R.id.train_status_header).setVisibility(status.isEmpty()?View.INVISIBLE:View.VISIBLE);
                    ((TextView)dv_item.findViewById(R.id.track_status_details)).setText(status);
                    dv_item.findViewById(R.id.train_status_line).setVisibility(status.isEmpty()?View.GONE:View.VISIBLE);

                    //((TextView)dv_item.findViewById(R.id.train_live_details)).setText(station);
                    //((TextView)dv_item.findViewById(R.id.train_live_details)).setVisibility(station.isEmpty()?View.INVISIBLE:View.VISIBLE);
                    ((TextView)dv_item.findViewById(R.id.departure_time)).setText(time);
                    to = to.replace("&nbsp;", " ");
                    to = to.replace("<i>", "");
                    to = to.replace("</i>", "");
                    ((TextView)dv_item.findViewById(R.id.destination)).setText(to);


                    parent.addView(dv_item);

                }

            }
        }
    }

    @Override
    protected void onDestroy() {
        doUnbindService();
        super.onDestroy();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            systemService = ((RemoteBinder)service).getService();
            Log.d("DVA", "SystemService connected, called method on remote binder "  + ((RemoteBinder)service).getService());

            updateTables();

        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            systemService = null;
            Log.d("DVA", "SystemService disconnected");
            //textStatus.setText("Disconnected.");
        }
    };

    void doBindService() {
        if (!mIsBound) {
            Log.d("SVCON", "SystemService binding.");
            bindService(new Intent(this, SystemService.class), mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }
    }

    void doUnbindService() {
        if (mIsBound) {
            Log.d("SVCON", "SystemService doUnbindService.");
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            unbindService(mConnection);
            mIsBound = false;
            //textStatus.setText("Unbinding.");
        }
    }
    public class LocalBcstReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.d("MAIN", "onReceive " + intent.getAction());
            if (intent.getAction().equals("departure-vision-updated" )) {
                Log.d("DVA", "departure-vision-updated");
                updateTables();
            }
        }
    }
    private BroadcastReceiver mMessageReceiver = new LocalBcstReceiver();


}

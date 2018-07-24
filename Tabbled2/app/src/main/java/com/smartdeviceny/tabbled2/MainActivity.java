package com.smartdeviceny.tabbled2;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.smartdeviceny.tabbled2.adapters.FragmentPagerMainPageAdaptor;
import com.smartdeviceny.tabbled2.adapters.ServiceConnected;


import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    boolean mIsBound = false;
    public SystemService systemService;
    ProgressDialog progressDialog =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(MainActivity.this, SystemService.class));
        doBindService();
        IntentFilter filter = new IntentFilter();
        filter.addAction("custom-event-name");
        filter.addAction("database-ready");
        filter.addAction("database-check-complete");
        filter.addAction("departure-vision-updated");
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);


        setContentView(R.layout.activity_main);
        initToolbar();

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager viewPager = findViewById(R.id.view_pager);
        FragmentPagerMainPageAdaptor adapter = new FragmentPagerMainPageAdaptor(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.addOnTabSelectedListener(new  TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }

    @Override
    protected void onDestroy() {
        doUnbindService();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        //stopService(startService(new Intent(MainActivity.this, SystemService.class)));
        super.onDestroy();
    }

    @Override
    protected void onPostResume() {
        if(progressDialog !=null && progressDialog.isShowing()) {
            if(systemService != null && !systemService.isUpdateRunning()) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
        super.onPostResume();
    }

    private Toolbar initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("NJ Transit Schedule");

        return toolbar;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        Log.d("MA", "menu created");
       // RecyclerView rv;
        //rv.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(systemService!=null) {
            // TODO:;
            systemService.getDepartureVision("NY");
        }
        return super.onOptionsItemSelected(item);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            systemService = ((RemoteBinder)service).getService();
            Log.d("MAIN", " Fragment ID:"
                    + getSupportFragmentManager().getFragments().get(0).getId() + ", "
                    + getSupportFragmentManager().getFragments().get(1).getId() + ", "
                    //+ getSupportFragmentManager().getFragments().get(2).getId() + " "
                    + " Other:" + R.id.fragment_njt_schedule );

            for(Fragment f:getSupportFragmentManager().getFragments()) {
                ServiceConnected frag = (ServiceConnected) f;
                if (frag != null) {
                    Log.d("MAIN", "got frag");
                    frag.onSystemServiceConnected(systemService);
                } else {
                    Log.d("MAIN", "no frag.");
                }
            }
            Log.d("MAIN", "SystemService connected, called method on remote binder "  + ((RemoteBinder)service).getService());
            // we just reconnected  check the progressdialog
            updateSubscriptions();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            systemService = null;
            Log.d("MAIN", "SystemService disconnected");
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

    boolean departureVisionSubscription = false;
    ArrayList<String> departureVisionSubscriptionStations = new ArrayList<>();
    void subscribeDepartureVision(String station)
    {
        departureVisionSubscription = true;
        if( !departureVisionSubscriptionStations.contains(station)) {
            departureVisionSubscriptionStations.add(station);
        }
        updateSubscriptions();
    }
    void updateSubscriptions() {
        if( departureVisionSubscription) {
            if(systemService!=null ) {
                systemService.subscribeDepartureVision(1, departureVisionSubscriptionStations);
            }
        }
    }
    void showUpdateProgressDialog(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Checking for NJ Transit schedule updates");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }
    public class LocalBcstReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.d("MAIN", "onReceive " + intent.getAction());
            if (intent.getAction().equals("database-ready" )) {
                Log.d("receiver", "Database is ready we can do all the good stuff");
                if(progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } else if (intent.getAction().equals("database-check-complete" )) {
                Log.d("receiver", "database-check-complete");
                if(progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } else if (intent.getAction().equals("departure-vision-updated" )) {
                Log.d("DVA", "departure-vision-updated");
                for(Fragment f:getSupportFragmentManager().getFragments()) {
                    ServiceConnected frag = (ServiceConnected) f;
                    if (frag != null) {
                        frag.onDepartureVisionUpdated(systemService);
                    }
                }
            }
            else {
                Log.d("receiver", "got omething not sure what " + intent.getAction());
            }
        }
    }
    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "custom-event-name" is broadcasted.
    private BroadcastReceiver mMessageReceiver = new LocalBcstReceiver();
}

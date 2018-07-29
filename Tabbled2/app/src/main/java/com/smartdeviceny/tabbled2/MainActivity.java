package com.smartdeviceny.tabbled2;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
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
import android.view.View;

import com.smartdeviceny.tabbled2.adapters.FragmentPagerMainPageAdaptor;
import com.smartdeviceny.tabbled2.adapters.ServiceConnected;
import com.smartdeviceny.tabbled2.utils.Config;
import com.smartdeviceny.tabbled2.utils.Utils;

public class MainActivity extends AppCompatActivity {
    boolean mIsBound = false;
    public SystemService systemService;
    public ProgressDialog progressDialog =null;
    int tabSelected = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences config = getPreferences(Context.MODE_PRIVATE);
        //setupConfigDefaults(config, getString(R.string.CONFIG_START_STATION), getString(R.string.CONFIG_DEFAULT_START_STATION));
        //setupConfigDefaults(config, getString(R.string.CONFIG_STOP_STATION), getString(R.string.CONFIG_DEFAULT_STOP_STATION));
        //setupConfigDefaults(config, getString(R.string.CONFIG_DEFAULT_ROUTE), getString(R.string.CONFIG_DEFAULT_ROUTE));

        startService(new Intent(this, PowerStartService.class));
        startService(new Intent(this, SystemService.class));
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
                tabSelected = tab.getPosition();
                Log.d("MAIN", "Tab selected" + tabSelected);
                // need a model based design.
                if ( tabSelected == 1 ) {
//                    MenuItem item = findViewById(R.id.menu_reverse);
//                    if(item != null ) {
//                        item.setVisible(true);
//                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if ( tabSelected == 1 ) {
//                    MenuItem item = findViewById(R.id.menu_reverse);
//                    if(item != null ) {
//                        item.setVisible(false);
//                    }
                }
                tabSelected = -1;
                Log.d("MAIN", "Tab un-selected" + tabSelected);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.d("MAIN", "Tab selected" + tabSelected);
                onTabSelected(tab);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        doBindService();
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(NotificationValues.BROADCAT_DATABASE_READY);
            filter.addAction(NotificationValues.BROADCAT_DATABASE_CHECK_COMPLETE);
            filter.addAction(NotificationValues.BROADCAT_DEPARTURE_VISION_UPDATED);
            filter.addAction(NotificationValues.BROADCAT_PERIODIC_TIMER);
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // bug in noughat... crap
        Utils.scheduleJob(this.getApplicationContext(), DepartureVisionJobService.class, 15*1000, false);
    }
    public void doCheckIsDatabaseReady(Context context) {
        if (systemService!= null ) {
            systemService.checkForUpdate();
            if ( progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if( !systemService.isDatabaseReady()) {
                showUpdateProgressDialog(context, "System getting ready.");
            }

        } else {
            Log.d("MAIN", "system service not init " + systemService );
        }
    }

    public void doCheckForUpdate(Context context) {
        if (systemService!= null ) {
            systemService.checkForUpdate();
            if ( progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if( systemService.isUpdateRunning()) {
                showUpdateProgressDialog(context, "Checking for Latest NJ Transit Train Schedules");
            }
        } else {
            Log.d("MAIN", "system service not init " + systemService );
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        doUnbindService();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onDestroy() {
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
        doCheckIsDatabaseReady(this);
        super.onPostResume();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private Toolbar initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle("NJ Transit Schedule");
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
        switch (item.getItemId()) {
            case R.id.menu_Refresh:
                if(systemService!=null) {
                    SharedPreferences config = getPreferences(MODE_PRIVATE);
                    String departureVisionCode = Config.getConfig(config, getString(R.string.CONFIG_DV_STATION), getString(R.string.CONFIG_DEFAULT_DV_STATION));

                    systemService.getDepartureVision(departureVisionCode, 30000);
                }
                return true;
            case R.id.menu_reverse: {
                // swap the routes
                if(tabSelected == 1|| tabSelected == 3 ) {
                    SharedPreferences config = getPreferences(Context.MODE_PRIVATE);
                    String start = Config.getConfig(config, getString(R.string.CONFIG_START_STATION), getString(R.string.CONFIG_DEFAULT_START_STATION));
                    String stop = Config.getConfig(config, getString(R.string.CONFIG_STOP_STATION), getString(R.string.CONFIG_DEFAULT_STOP_STATION));
                    Config.setConfig(config, getString(R.string.CONFIG_START_STATION), stop);
                    Config.setConfig(config, getString(R.string.CONFIG_STOP_STATION), start);

                    for(Fragment f:getSupportFragmentManager().getFragments()) {
                        ServiceConnected frag = (ServiceConnected) f;
                        if (frag != null) {
                            frag.configChanged(systemService); // TODO: implement a reoute refresh here.
                        }
                    }
                }
            }
            break;
            case R.id.menu_Settings: {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
            break;

        }


        return super.onOptionsItemSelected(item);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            systemService = ((RemoteBinder)service).getService();
            doCheckIsDatabaseReady(MainActivity.this);

            Log.d("MAIN", "SystemService connected, calling onSystemServiceConnected on fragments");
            for(Fragment f:getSupportFragmentManager().getFragments()) {
                ServiceConnected frag = (ServiceConnected) f;
                if (frag != null) {
                    frag.onSystemServiceConnected(systemService);
                }
            }

        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            systemService = null;
            Log.d("MAIN", "SystemService disconnected");
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

    public void showUpdateProgressDialog(Context context, @Nullable  String msg) {
        progressDialog = new ProgressDialog(context);
        if(msg == null) {
            msg = "Checking for NJ Transit schedule updates";
        }
        progressDialog.setMessage(msg);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public void doConfigChanged() {
        for(Fragment f:getSupportFragmentManager().getFragments()) {
            ServiceConnected frag = (ServiceConnected) f;
            if (frag != null) {
                if(systemService != null ) {
                    frag.configChanged(systemService);
                }
            }
        }
    }
    public String getStationCode() {
        SharedPreferences config = getPreferences(Context.MODE_PRIVATE);
        return Config.getConfig(config, getString(R.string.CONFIG_DV_STATION), getString(R.string.CONFIG_DEFAULT_DV_STATION));
    }
    public class LocalBcstReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            //Log.d("MAIN", "onReceive " + intent.getAction());
            if (intent.getAction().equals(NotificationValues.BROADCAT_DATABASE_READY )) {
                Log.d("MAIN", "Database is ready we can do all the good stuff");
                if(progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                doConfigChanged();
            } else if (intent.getAction().equals(NotificationValues.BROADCAT_DATABASE_CHECK_COMPLETE)) {
                Log.d("MAIN", NotificationValues.BROADCAT_DATABASE_CHECK_COMPLETE);
                if(progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            } else if (intent.getAction().equals(NotificationValues.BROADCAT_DEPARTURE_VISION_UPDATED )) {
                Log.d("MAIN", NotificationValues.BROADCAT_DEPARTURE_VISION_UPDATED);
                boolean hasfrag = false;
                for(Fragment f:getSupportFragmentManager().getFragments()) {
                    ServiceConnected frag = (ServiceConnected) f;
                    if (frag != null) {
                        hasfrag = true;
                        if(systemService != null ) {
                            frag.onDepartureVisionUpdated(systemService);
                        }
                    }
                }
            } else if (intent.getAction().equals(NotificationValues.BROADCAT_PERIODIC_TIMER )) {
                Log.d("MAIN", NotificationValues.BROADCAT_PERIODIC_TIMER);
                boolean hasfrag = false;
                for(Fragment f:getSupportFragmentManager().getFragments()) {
                    ServiceConnected frag = (ServiceConnected) f;
                    if (frag != null) {
                        hasfrag = true;
                        if(systemService != null ) {
                            frag.onTimerEvent(systemService);
                        }
                    }
                }
            }  else {
                Log.d("MAIN", "got omething not sure what " + intent.getAction());
            }
        }
    }
    // Our handler for received Intents. This will be called whenever an Intent
    private BroadcastReceiver mMessageReceiver = new LocalBcstReceiver();
}

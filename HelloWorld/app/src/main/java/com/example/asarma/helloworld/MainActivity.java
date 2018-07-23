package com.example.asarma.helloworld;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asarma.helloworld.activities.DepartureVisionActivity;
import com.example.asarma.helloworld.utils.SQLiteLocalDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    //DownloadManager manager;
    private long enqueue;
    public static final String PREF_FILE_NAME = "test.pref";
    DownloadFile download = null;
    SQLiteLocalDatabase sql;
    SystemService systemService=null;
    ProgressDialog progressDialog =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Config config = new Config(getApplicationContext());
        super.onCreate(savedInstanceState);
        doBindService();

        IntentFilter filter = new IntentFilter();
        filter.addAction("custom-event-name");
        filter.addAction("database-ready");
        filter.addAction("database-check-complete");
        filter.addAction("departure-vision-update");
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);


        config.set("SOME_FLAG", "Heloo There");
        Set<String> values = new ArraySet<>();
        values.add("ITEM1");
        values.add("ITEM2");
        config.set("SET",values);
        setContentView(R.layout.activity_main);

        LinearLayout parent = (LinearLayout)this.findViewById(R.id.activity_main);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View route = inflater.inflate(R.layout.route_layout_template, null);
        parent.addView(route);

        View route2 = inflater.inflate(R.layout.route_layout_template, null);
        TextView track_number = route2.findViewById(R.id.track_number);
        TextView train_live_header = route2.findViewById(R.id.train_live_header);
        TextView train_live_details = route2.findViewById(R.id.train_live_details);
        Button detail_button = route2.findViewById(R.id.detail_button);

        detail_button.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View v) {
                                                 Toast.makeText(getApplicationContext(), "A new window will show with details", Toast.LENGTH_LONG).show();

                                                 Intent i = new Intent(getApplicationContext(), NJMapActivity.class);
                                                 startActivity(i);

                                             }
                                         });
        route2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LinearLayout ll = (LinearLayout) v;
                Resources resources = getApplicationContext().getResources();
                if ( ll.isSelected()) {
                    int resid = resources.getIdentifier("route_background", "drawable", getApplicationContext().getPackageName());
                    ll.setBackgroundResource(resid);
                    ll.setSelected(false);
                } else {
                    int resid = resources.getIdentifier("route_background_selected", "drawable", getApplicationContext().getPackageName());
                    ll.setBackgroundResource(resid);
                    ll.setSelected(true);
                }
                return false;
            }
        });

        track_number.setText("12");
        train_live_header.setVisibility(View.VISIBLE);
        train_live_details.setText("Track 12, status good");
        Resources resources = getApplicationContext().getResources();
        int resourceId = resources.getIdentifier("card_text_border_red", "drawable",getApplicationContext().getPackageName());
        track_number.setBackground(resources.getDrawable(resourceId));

        parent.addView(route2);

        //registerReceiver(mMessageReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        File f = new File(getApplicationContext().getApplicationInfo().dataDir + File.separator + "rails_db.sql");
        //f.delete();


        System.out.println("started");
        startService(new Intent(MainActivity.this, MessageService.class));
        startService(new Intent(MainActivity.this, SystemService.class));

        //startService(new Intent(MainActivity.this, MainActivity.MessageService.class));
        Button buttonStartService = (Button) findViewById(R.id.button_ok);
        buttonStartService.setOnClickListener( p -> new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                System.out.println("on click");
                //Register MessageService in Manifest to work
                startService(new Intent(MainActivity.this, MessageService.class));
                sendMessageToService(12);
            }
        });

        Button sqlButton = (Button)findViewById(R.id.check_for_updates);
        sqlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (systemService!= null ) {
                    systemService.checkForUpdate();
                    if ( progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    if( systemService.isUpdateRunning()) {
                        showUpdateProgressDialog(v.getContext());
                    }
                } else {
                    Log.d("BTNDNLD", "system service not init " + systemService );
                }
            }
        });

        Button button_departurevision = (Button)findViewById(R.id.button_departurevision);
        button_departurevision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(systemService!=null) {
                    systemService.getDepartureVision("NY");
                }
                Intent i = new Intent(getApplicationContext(), DepartureVisionActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(download != null ) {
            Log.d("main", "cleaning up download .... ");
            download.cleanup();
            download = null;
        }
        doUnbindService();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        System.out.print("Local::onPause");
        // Unregister since the activity is paused.
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
//        if(progressDialog !=null) {
//            progressDialog.
//        }
        super.onPause();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        System.out.print("Local::onResume");

        if(progressDialog !=null && progressDialog.isShowing()) {
            if(systemService != null && !systemService.isUpdateRunning()) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
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
            } else {
                Log.d("receiver", "got omething not sure what " + intent.getAction());
            }
        }
    }
    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "custom-event-name" is broadcasted.
    private BroadcastReceiver mMessageReceiver = new LocalBcstReceiver();

    boolean mIsBound = false;


    final Messenger mMessenger = new Messenger(new IncomingHandler());
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d("main, receiver", "Got reply" + msg);
            switch (msg.what) {
                case Config.MSG_SET_INT_VALUE:
                    //textIntValue.setText("Int Message: " + msg.arg1);
                    break;
                case Config.MSG_SET_STRING_VALUE:
                    String str1 = msg.getData().getString("str1");
                    //textStrValue.setText("Str Message: " + str1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
    void showUpdateProgressDialog(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Checking for NJ Transit schedule updates");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
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
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            systemService = ((RemoteBinder)service).getService();
            Log.d("SVCON", "SystemService connected, called method on remote binder "  + ((RemoteBinder)service).getService());
            // we just reconnected  check the progressdialog
            updateSubscriptions();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            systemService = null;
            Log.d("SVCON", "SystemService disconnected");
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

    private void sendMessageToService(int intvaluetosend) {
//        if (mIsBound) {
//            if (systemService != null) {
//                try {
//                    Message msg = Message.obtain(null, Config.MSG_SET_INT_VALUE, intvaluetosend, 0);
//                    msg.replyTo = mMessenger;
//                    Log.d("send", "message sent");
//                    //systemService mService.send(msg);
//                }
//                catch (RemoteException e) {
//                }
//               // mService.getBinder()
//            }
//        }
//        else {
//            System.out.println("Service not bound ... ");
//        }
    }

}

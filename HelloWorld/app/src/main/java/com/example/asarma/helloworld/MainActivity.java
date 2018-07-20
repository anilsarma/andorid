package com.example.asarma.helloworld;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.asarma.helloworld.utils.SQLiteLocalDatabase;
import com.example.asarma.helloworld.utils.SqlUtils;
import com.example.asarma.helloworld.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Set;
import java.util.zip.ZipInputStream;

public class MainActivity extends AppCompatActivity {
    DownloadManager manager;
    private long enqueue;
    public static final String PREF_FILE_NAME = "test.pref";
    DownloadFile download = null;
    SQLiteLocalDatabase sql;
    SystemService systemService=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Config config = new Config(getApplicationContext());
        super.onCreate(savedInstanceState);
        doBindService();

        config.set("SOME_FLAG", "Heloo There");
        Set<String> values = new ArraySet<>();
        values.add("ITEM1");
        values.add("ITEM2");
        config.set("SET",values);
        setContentView(R.layout.activity_main);

        registerReceiver(mMessageReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        File f = new File(getApplicationContext().getApplicationInfo().dataDir + File.separator + "rails_db.sql");
        //f.delete();

         manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if(manager !=null) {
            String url = "https://raw.githubusercontent.com/anilsarma/misc/master/njt/version.txt";
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDescription("Some descrition");
            request.setTitle("Some title");
            //enqueue = manager.enqueue(request);
            //System.out.println("downloading service .... ");
        }
        else {
           // System.out.println("does not have download serecicxe");
        }
        download = new DownloadFile(getApplicationContext(), new DownloadFile.Callback() {
            @Override
            public boolean downloadComplete(DownloadFile d, long id, String url, File file) {
                Log.d("download", "downloading complete " + file.getAbsolutePath() + " " + url +  " " + id );
                return true;
            }

            @Override
            public void downloadFailed(DownloadFile d, long id, String url) {
                Log.d("download", "downloading failed " + " " + url +  " " + id );
            }
        });
        download.downloadFile("https://raw.githubusercontent.com/anilsarma/misc/master/njt/version.txt", "version.txt", "NJT Schedule version",
                DownloadManager.Request.NETWORK_WIFI| DownloadManager.Request.NETWORK_MOBILE, null);

                //startActivity(i);
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

        Button sqlButton = (Button)findViewById(R.id.sql);
        sqlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (systemService!= null ) {
                    systemService.checkForUpdate();
                } else {
                    Log.d("BTNDNLD", "system service not init " + systemService );
                }
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
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        System.out.print("Local::onPause");
        // Unregister since the activity is paused.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        doUnbindService();
        super.onPause();
    }

    @Override
    protected void onResume() {
        System.out.print("Local::onResume");
        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "custom-event-name".
        IntentFilter filter = new IntentFilter();
        filter.addAction("custom-event-name");
        filter.addAction("database-ready");
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);


        doBindService();
        super.onResume();
    }

    public class LocalBcstReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals("custom-event-name")) {
                System.out.print("BroadcastReceiver::onReceive");
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    Log.d("download complete", "compleete ... ");
                    handle_download_complete(context, intent);
                }
                // Get extra data included in the Intent
                String message = intent.getStringExtra("message");
                Log.d("receiver", "~~~~~~~~~~~~ Got message: " + message);
            } else if (intent.getAction().equals("database-ready" )) {
                Log.d("receiver", "Database is ready we can do all the good stuff");
            }
        }

        public void handle_download_complete(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(enqueue);
                Cursor c = manager.query(query);
                if (c.moveToFirst()) {
                    int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                        String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI ))
                                + " " + c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));


                        String downloadFileLocalUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        File mFile = new File(Uri.parse(downloadFileLocalUri).getPath());

                        System.out.println("~~~~~~~~~~~ COmplete" + uriString + " File " + mFile.getAbsolutePath());
                        mFile.delete();
                    }
                }
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

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            systemService = ((RemoteBinder)service).getService();
            Log.d("SVCON", "SystemService connected, called method on remote binder "  + ((RemoteBinder)service).getService());

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

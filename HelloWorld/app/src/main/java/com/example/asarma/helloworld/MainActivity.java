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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Config config = new Config(getApplicationContext());
        super.onCreate(savedInstanceState);
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
        download.downloadFile("https://raw.githubusercontent.com/anilsarma/misc/master/njt/version.txt", "NJS", "",
                DownloadManager.Request.NETWORK_WIFI| DownloadManager.Request.NETWORK_MOBILE, null);

                //startActivity(i);
        System.out.println("started");
        startService(new Intent(MainActivity.this, MessageService.class));
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
                File f = new File(getApplicationContext().getApplicationInfo().dataDir + File.separator + "rails_db.sql");
                if (f.exists()) {
                    sql = new SQLiteLocalDatabase(getApplicationContext(), "rails_db.sql", null);
                    sql.getWritableDatabase();
                    Toast.makeText(getApplicationContext(), "Got SQL", Toast.LENGTH_LONG).show();
                    Log.d("SQL", "found database file and opened." + sql);
                    try {
                        String njt_routes[] = SqlUtils.get_values(sql.getReadableDatabase(), "select * from routes", "route_long_name");
                        for (int i = 0; i < njt_routes.length; i++) {
                            njt_routes[i] = Utils.capitalize(njt_routes[i]);
                            Log.d("SQL", "route " + njt_routes[i]);
                        }
                    } catch(Exception e) {
                        Log.d("SQL", "get routes failed need to download");
                        sql.close();
                        sql = null;
                    }
                }
                // download it any way.
                 {
                    final DownloadFile d = new DownloadFile(getApplicationContext(), new DownloadFile.Callback() {
                        @Override
                        public boolean downloadComplete(DownloadFile d, long id, String url, File file) {
                            File tmpFilename=null;
                            File tmpVersionFilename=null;
                            try {
                                tmpFilename = File.createTempFile(f.getName(), ".sql.tmp", f.getParentFile());
                                tmpVersionFilename = File.createTempFile("version", ".txt.tmp", f.getParentFile());
                                InputStream is = new FileInputStream(file);
                                OutputStream os = new FileOutputStream(f);
                                Log.d("SQL", "getting Inputstream");
                                ZipInputStream zis = Utils.getFileFromZip(is, "rail_data.db");
                                OutputStream zos = new FileOutputStream(tmpFilename);
                                Log.d("SQL", "writing stream to disk"+ f.getAbsolutePath());
                                Utils.writeExtractedFileToDisk(zis, zos);

                                ZipInputStream zis_version = Utils.getFileFromZip(is, "rail_data.db");
                                OutputStream zos_version = new FileOutputStream(tmpVersionFilename);
                                Log.d("SQL", "writing stream to disk"+ tmpVersionFilename.getAbsolutePath());
                                Utils.writeExtractedFileToDisk(zis_version, zos_version);
                                String version_str = Utils.getFileContent(tmpVersionFilename);

                                if(f.exists()) {
                                    if(sql != null) {
                                        boolean closeDB = true;
                                        if ( SqlUtils.check_if_user_pref_exists(sql.getWritableDatabase())) {
                                            String db_ver = SqlUtils.get_user_pref_value( sql.getWritableDatabase(),"version", "");
                                            if (db_ver.equals(version_str)) {
                                                Log.d("SQL", "no upgrade required, version  matches " + version_str);
                                                closeDB = false;
                                            }
                                        }
                                        if(closeDB) {
                                            sql.close();
                                            sql = null;
                                        }
                                    }
                                    if(sql == null ) {
                                        tmpFilename.renameTo(f);
                                        sql = new SQLiteLocalDatabase(getApplicationContext(), f.getName(), null);
                                        SqlUtils.create_user_pref_table(sql.getWritableDatabase());
                                        SqlUtils.update_user_pref( sql.getWritableDatabase(),"version", version_str, new Date());
                                    }
                                }

                                Log.d("SQL", "extracted zip file " + f.getAbsolutePath() );
                            } catch(IOException e) {
                                Log.d("SQL", "failed reading zip file " + e);
                                e.printStackTrace();
                            }
                            finally {
                                if(tmpFilename != null ) {
                                    try {tmpFilename.delete();} catch (Exception e){}
                                }
                            }
                            return false;
                        }

                        @Override
                        public void downloadFailed(DownloadFile d,long id, String url) {
                            Log.d("SQL", "download of SQL file failed " + url);
                        }
                    });
                    d.downloadFile("https://github.com/anilsarma/misc/raw/master/njt/rail_data_db.zip", "", "",
                            DownloadManager.Request.NETWORK_MOBILE| DownloadManager.Request.NETWORK_WIFI, "application/zip");
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
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("custom-event-name"));
        doBindService();
        super.onResume();
    }

    public class LocalBcstReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            System.out.print("BroadcastReceiver::onReceive");
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                Log.d("download complete", "compleete ... ");
                handle_download_complete(context, intent);
            }
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("receiver", "~~~~~~~~~~~~ Got message: " + message);
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
    Messenger mService;

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
            Log.d("main activity", "called method on remote binder "  + ((RemoteBinder)service).getService().getValue());
            boolean usemessanger = false;
            if ( usemessanger ) {
                mService = new Messenger(service);
                //textStatus.setText("Attached.");
                try {
                    Message msg = Message.obtain(null, Config.MSG_REGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);

                    //RemoteBinder remote = (RemoteBinder)service;

                    Log.d("main activity", "called method on remote binder " + service);
                } catch (RemoteException e) {
                    // In this case the service has crashed before we could even do anything with it
                }
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
            //textStatus.setText("Disconnected.");
        }
    };

    void doBindService() {
        bindService(new Intent(this, MessageService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, Config.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }
                catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            //textStatus.setText("Unbinding.");
        }
    }

    private void sendMessageToService(int intvaluetosend) {
        if (mIsBound) {
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, Config.MSG_SET_INT_VALUE, intvaluetosend, 0);
                    msg.replyTo = mMessenger;
                    Log.d("send", "message sent");
                    mService.send(msg);
                }
                catch (RemoteException e) {
                }
               // mService.getBinder()
            }
        }
        else {
            System.out.println("Service not bound ... ");
        }
    }

}

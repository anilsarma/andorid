package com.example.asarma.helloworld;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.example.asarma.helloworld.utils.SQLiteLocalDatabase;
import com.example.asarma.helloworld.utils.SqlUtils;
import com.example.asarma.helloworld.utils.Utils;

import java.io.File;
import java.util.Date;

public class SystemService extends Service {

    boolean           checkingVersion;
    DownloadFile      downloader;
    SQLiteLocalDatabase sql;
    BroadcastReceiver receiver = new LocalbroadcastReceiver(this);


    DownloadFile.Callback callback = new DownloadFile.Callback() {
        @Override
        public void downloadFailed(DownloadFile d, long id, String url) {
            checkingVersion = false;
            // we need to redo again.
        }

        @Override
        public boolean downloadComplete(DownloadFile d, long id, String url, File file) {
            checkingVersion = false;
            return true; // remove the file.
        }
    };

    public SystemService()
    {}

    @Override
    public void onCreate() {
        downloader = new DownloadFile(this.getApplicationContext(),  callback);
        super.onCreate();
        setupDb();
        sendDatabaseReady();
        checkForUpdate();

    }

    @Override
    public void onDestroy() {
        if(downloader !=null) {
            downloader.cleanup();
        }
        super.onDestroy();
    }

    boolean checkForUpdate() {
        if (checkingVersion) {
            return false; // already running.
        }
        _checkRemoteDBUpdate();
        return true;
    }

    void _checkRemoteDBUpdate() {
        Log.d("SVC", "checking Schedule status");
        File f = new File(getApplicationContext().getApplicationInfo().dataDir + File.separator + "rails_db.sql");
        sql = UtilsDBVerCheck.getSQLDatabase(getApplicationContext(), f);
        checkingVersion=true;
        if( sql == null) {
            _checkRemoteDBZipUpdate(""); // download it anyway we dont have a valid database.
            return;
        }
        final DownloadFile d = new DownloadFile(getApplicationContext(), new DownloadFile.Callback() {
            @Override
            public boolean downloadComplete(DownloadFile d, long id, String url, File file) {
                String version_str = Utils.getFileContent(file);
                _checkRemoteDBZipUpdate(version_str);
                return false;
            }

            @Override
            public void downloadFailed(DownloadFile d,long id, String url) {
                Log.d("SQL", "download of SQL file failed " + url);
                checkingVersion=false;  // could not get a version string, we will do it later.
            }
        });
        d.downloadFile("https://github.com/anilsarma/misc/raw/master/njt/version.txt", "version.txt", "NJ Transit Schedules Version",
                DownloadManager.Request.NETWORK_MOBILE| DownloadManager.Request.NETWORK_WIFI, null);
    }

    // never call this directly shold be called via _checkRemoteDBUpdate
    private void _checkRemoteDBZipUpdate(String version_str) {
            File f = new File(getApplicationContext().getApplicationInfo().dataDir + File.separator + "rails_db.sql");
            sql = UtilsDBVerCheck.getSQLDatabase(getApplicationContext(), f);
            if (UtilsDBVerCheck.matchDBVersion( sql, version_str) ) {
                checkingVersion = false;
                Log.d("DBSVC", "system schedule db is upto date " + version_str);
                return;
            }
            final DownloadFile d = new DownloadFile(getApplicationContext(), new DownloadFile.Callback() {
                @Override
                public boolean downloadComplete(DownloadFile d, long id, String url, File file) {
                    checkingVersion=false;
                    File dbFilePath = new File(getApplicationContext().getApplicationInfo().dataDir + File.separator + "rails_db.sql");
                    File tmpFilename=null;
                    File tmpVersionFilename=null;
                    try {
                        tmpFilename = UtilsDBVerCheck.createTempFile(file, dbFilePath.getParentFile(), "rail_data.db");
                        tmpVersionFilename = UtilsDBVerCheck.createTempFile(file, dbFilePath.getParentFile(), "version.txt");
                        String version_str = Utils.getFileContent(tmpVersionFilename);

                        if(!tmpFilename.exists()) {// extracted file does not exit some thing is wong
                            return true; // remove the downloaded files.
                        }
                        if (UtilsDBVerCheck.matchDBVersion( sql, version_str) ) {
                            return true;
                        }
                        if(sql != null ) {
                            sql.close();
                            sql = null;
                            dbFilePath.delete();
                            Log.d("SQL", "renamed filed " + tmpFilename.getAbsolutePath() + " to " + dbFilePath.getAbsolutePath());
                        }

                        Log.d("SQL", "renamed filed " + tmpFilename.getAbsolutePath() + " to " + dbFilePath.getAbsolutePath());
                        tmpFilename.renameTo(dbFilePath); tmpFilename = null;
                        //dbFilePath = new File(dbFilePath.getAbsolutePath());

                        sql = new SQLiteLocalDatabase(getApplicationContext(), dbFilePath.getName(), null);
                        SqlUtils.create_user_pref_table(sql.getWritableDatabase());
                        SqlUtils.update_user_pref( sql.getWritableDatabase(),"version", version_str, new Date());

                        // let the user know we have upgraded.
                        notify_user_of_upgrade(version_str);
                    }
                    finally {
                        Utils.delete(tmpFilename);
                        Utils.delete(tmpVersionFilename);
                        sendDatabaseReady();
                    }
                    return false;
                }

                @Override
                public void downloadFailed(DownloadFile d,long id, String url) {
                    Log.d("SQL", "download of SQL file failed " + url);
                    checkingVersion=false;
                }
            });

            d.downloadFile("https://github.com/anilsarma/misc/raw/master/njt/rail_data_db.zip", "rail_data_db.zip", "NJ Transit Schedules",
                    DownloadManager.Request.NETWORK_MOBILE| DownloadManager.Request.NETWORK_WIFI, "application/zip");
            checkingVersion=true;
        }

    @Override
    public IBinder onBind(Intent intent) {
       return new RemoteBinder(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendDatabaseReady() {
        if( isDatabaseReady() ) {
            Intent intent = new Intent("database-ready");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            Log.d("SVC", "sending database ready");
        }
    }

    // SQL related messages similar to the uil
    public boolean isDatabaseReady() {
        if(sql == null ) {
            return false;
        }
        SQLiteDatabase db = sql.getWritableDatabase();
        if( SqlUtils.check_if_user_pref_exists(db)) {
            if(SqlUtils.check_if_table_exists(db, "routes") && SqlUtils.check_if_table_exists(db, "trips") ) {
                return true;
            }
        }
        return  false;
    }

    private void setupDb() {
        if(sql == null ) {
            File f = new File(getApplicationContext().getApplicationInfo().dataDir + File.separator + "rails_db.sql");
            if(f.exists()) {
                sql = new SQLiteLocalDatabase(getApplicationContext(), f.getName(), null);
            }
        }
    }
    private void notify_user_of_upgrade(String new_version) {
        final NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //int icon = R.mipmap.ic_launcher;
        //long when = System.currentTimeMillis();
//        Notification notification = new Notification(icon, getString(R.string.app_name), when);
//        notification.flags |= Notification.FLAG_NO_CLEAR; //Do not clear the notification
//        notification.defaults |= Notification.DEFAULT_LIGHTS; // LED
//        notification.defaults |= Notification.DEFAULT_VIBRATE; //Vibration
//        notification.defaults |= Notification.DEFAULT_SOUND; // Sound
        String msg = "NJT Schedule upgraded to version " + new_version;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("NJTS Schedule Upgraded")
                        .setTicker("NT Transit Schedule.")
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setContentText(msg);

        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        //notification.defaults |= Notification.DEFAULT_SOUND;

        Log.d("SVC", "database schedule upgraded " + msg );
        mNotificationManager.notify(1, notification);
    }

    public class LocalbroadcastReceiver extends BroadcastReceiver {
        SystemService service;
        public LocalbroadcastReceiver(){}

        public LocalbroadcastReceiver(SystemService service) {
            this.service = service;
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            if(service !=null ) {
                service.checkForUpdate();
            }
        }
    }
}
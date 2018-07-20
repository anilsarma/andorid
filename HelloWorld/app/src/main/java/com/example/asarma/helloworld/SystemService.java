package com.example.asarma.helloworld;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.asarma.helloworld.utils.SQLiteLocalDatabase;
import com.example.asarma.helloworld.utils.SqlUtils;
import com.example.asarma.helloworld.utils.Utils;
import com.google.gson.Gson;

import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.zip.ZipInputStream;

public class SystemService extends Service {

    boolean           checkingVersion;
    DownloadFile      downloader;
    SQLiteLocalDatabase sql;


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
        if(isDatabaseReady()) {
            sendDatabaseReady();
        }
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

        // check if we have a valid db file, if we don't get it directly not using the download manager.
        // non blocking run a thread in the back.
        // check the version file check if it matches.

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
                    f.delete();
                    sql = null;
                }
            }
            // download it any way.
            {
                final DownloadFile d = new DownloadFile(getApplicationContext(), new DownloadFile.Callback() {
                    @Override
                    public boolean downloadComplete(DownloadFile d, long id, String url, File file) {
                        checkingVersion=false;
                        File f = new File(getApplicationContext().getApplicationInfo().dataDir + File.separator + "rails_db.sql");
                        File tmpFilename=null;
                        File tmpVersionFilename=null;
                        try {
                            tmpFilename = File.createTempFile(f.getName(), ".sql.tmp", f.getParentFile());
                            tmpVersionFilename = File.createTempFile("version", ".txt.tmp", f.getParentFile());
                            Log.d("SQL", "getting Inputstream");
                            ZipInputStream zis = Utils.getFileFromZip(new FileInputStream(file), "rail_data.db");
                            Log.d("SQL", "writing stream to disk "+ tmpFilename.getAbsolutePath());
                            Utils.writeExtractedFileToDisk(zis, new FileOutputStream(tmpFilename));

                            ZipInputStream zis_version = Utils.getFileFromZip(new FileInputStream(file), "version.txt");
                            Log.d("SQL", "writing stream to disk"+ tmpVersionFilename.getAbsolutePath());
                            Utils.writeExtractedFileToDisk(zis_version, new FileOutputStream(tmpVersionFilename));
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
                                    f.delete();
                                    Log.d("SQL", "renamed filed " + tmpFilename.getAbsolutePath() + " to " + f.getAbsolutePath());
                                    tmpFilename.renameTo(f);
                                    f = new File(f.getAbsolutePath());
                                    tmpFilename = null;
                                    sql = new SQLiteLocalDatabase(getApplicationContext(), f.getName(), null);
                                    SqlUtils.create_user_pref_table(sql.getWritableDatabase());
                                    SqlUtils.update_user_pref( sql.getWritableDatabase(),"version", version_str, new Date());

                                }
                            } else {
                                Log.d("SQL", "renamed filed " + tmpFilename.getAbsolutePath() + " to " + f.getAbsolutePath());
                                tmpFilename.renameTo(f);
                                f = new File(f.getAbsolutePath());
                                tmpFilename = null;
                                sql = new SQLiteLocalDatabase(getApplicationContext(), f.getName(), null);
                                SqlUtils.create_user_pref_table(sql.getWritableDatabase());
                                SqlUtils.update_user_pref( sql.getWritableDatabase(),"version", version_str, new Date());

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
                            if(tmpVersionFilename != null ) {
                                try {tmpVersionFilename.delete();} catch (Exception e){}
                            }
                            if(sql!=null) {
                                sendDatabaseReady();
                            }
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
        }
    @Override
    public IBinder onBind(Intent intent) {
       return new RemoteBinder(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendMessage() {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("custom-event-name");
        // You can also include some extra data.
        intent.putExtra("message", "This is my message!");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    private void sendDatabaseReady() {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("database-ready");
        // You can also include some extra data.
        //intent.putExtra("message", "This is my message!");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d("SVC", "sending database ready");
        Toast.makeText(this.getApplicationContext(),"System Database ready sending", Toast.LENGTH_LONG).show();
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
            sql = new SQLiteLocalDatabase(getApplicationContext(), f.getName(), null);
        }
    }

}
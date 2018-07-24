package com.smartdeviceny.tabbled2;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.smartdeviceny.tabbled2.utils.DownloadFile;
import com.smartdeviceny.tabbled2.utils.SQLHelper;
import com.smartdeviceny.tabbled2.utils.SQLiteLocalDatabase;
import com.smartdeviceny.tabbled2.utils.SqlUtils;
import com.smartdeviceny.tabbled2.utils.Utils;
import com.smartdeviceny.tabbled2.utils.UtilsDBVerCheck;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class SystemService extends Service {

    boolean           checkingVersion;
    DownloadFile downloader;
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
        return checkingVersion;
    }
    boolean isUpdateRunning() {
        return checkingVersion;
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
                file.delete();
                return true;
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
            Log.d("DBSVC", "system schedule db is uptodate " + version_str);
            sendCheckcomplete();
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
                    sendCheckcomplete();
                    sendDatabaseReady();

                }
                return false;
            }

            @Override
            public void downloadFailed(DownloadFile d,long id, String url) {
                Log.d("SQL", "download of SQL file failed " + url);
                checkingVersion=false;
                sendCheckcomplete();
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
        Log.d("SVC", "SystemService is now running");
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendDatabaseReady() {
        if( isDatabaseReady() ) {
            Intent intent = new Intent("database-ready");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            Log.d("SVC", "sending database ready");
        }
    }

    private void sendCheckcomplete() {
        Intent intent = new Intent("database-check-complete");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d("SVC", "sending database-check-complete");

    }
    private void sendDepartVisionUpdated() {
        Intent intent = new Intent("departure-vision-updated");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d("SVC", "sending departure-vision-updated");

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
    HashMap<Integer, ArrayList<String>> departureVisionSubscriptions = new HashMap<Integer, ArrayList<String>>();
    public void subscribeDepartureVision(int uniqueid, ArrayList<String> departureVisionSubscriptionStations) {
        // update the subscriptions
        departureVisionSubscriptions.put(uniqueid, departureVisionSubscriptionStations);
    }

    ArrayList<HashMap<String, Object>> parseDepartureVision(String station, Document doc) {
        Log.d("DV", "parsing Departure vision Doc");
        ArrayList<HashMap<String, Object>> result =  new ArrayList<HashMap<String, Object>>();
        try {
            Element table = doc.getElementById("GridView1");
            Node node = table;
            List<Node> child = node.childNodes().get(1).childNodes();
            // discard the frist 3
            //Log.d("DV", "child ===================== Size:" + child.size());
            for (int i = 3; i < child.size(); i++) {
                Node tr = child.get(i);
                List<Node> td = tr.childNodes();
                //Log.d("DV", "childNodes(td) ===================== Size:" + td.size());

                if (td.size()< 4 ) {
                    continue;
                }
                HashMap<String, Object> data = new HashMap<>();
                String time = ((Element)td.get(1)).html().toString();
                String to =  ((Element)td.get(3)).html().toString();
                String track = ((Element)td.get(5)).html().toString();
                String line = ((Element)td.get(7)).html().toString();
                String train = ((Element)td.get(9)).html().toString();
                String status =  ((Element)td.get(11)).html().toString();;
                data.put("time", time);
                data.put("to", to);
                data.put("track", track);
                data.put("line", line);
                data.put("status", status);
                data.put("train", train);
                data.put("station", station);
                Log.d("DV", "details time:" + time +  " to:" + to + " track:" + track + " line:" + line + " status:" + status + " train:" + train + " station:" + station );
                result.add(data);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //Log.d("DV", "result=" + result.size());
        return result;
    }
    HashMap<String, ArrayList<HashMap<String, Object>>> status = new HashMap<>();
    HashMap<String, DepartureVisionData> status_by_trip = new HashMap<>();
    public void getDepartureVision(String station) {
        // check if we have a recent download, less than 1 minute old
        final DownloadFile d = new DownloadFile(getApplicationContext(), new DownloadFile.Callback() {
            @Override
            public boolean downloadComplete(DownloadFile d, long id, String url, File file) {
                try {
                    //Log.d("DV", "File Content\n" + Utils.getEntireFileContent(file));
                    Document doc = Jsoup.parse(file, null, "http://dv.njtransit.com");
                    ArrayList<HashMap<String, Object>> result = parseDepartureVision(station, doc);
                    status.put("NY", result);
                    for(HashMap<String, Object> dv:result) {
                        DepartureVisionData dd = new DepartureVisionData(dv);
                        status_by_trip.put(dd.trip, dd);

                    }
                    sendDepartVisionUpdated();
                    // send this off on an intent.
                } catch(Exception e) {
                    Log.d("SOUP", "Failed to parse soup " + e.getMessage());
                }
                file.delete();
                return true;
            }

            @Override
            public void downloadFailed(DownloadFile d,long id, String url) {
                Log.d("SQL", "download of SQL file failed " + url);
                checkingVersion=false;  // could not get a version string, we will do it later.
            }
        });

        d.downloadFile("http://dv.njtransit.com/mobile/tid-mobile.aspx?sid="+ station, "njts_departure_vision_" + station.toLowerCase() + ".html",
                "NJ Transit DepartureVision",
                DownloadManager.Request.NETWORK_MOBILE| DownloadManager.Request.NETWORK_WIFI, "text/html");

    }
    public class Route {
        public String departture_time;
        public String destination_time;
        public String block_id;
        public String route_name;
        public String trip_id;

        public Route(HashMap<String, Object> data) {
            departture_time = data.get("departure_time").toString();
            destination_time = data.get("destination_time").toString();
            block_id = data.get("block_id").toString();
            route_name = data.get("route_long_name").toString();
            trip_id = data.get("trip_id").toString();
        }
    }
    // this is a syncronous call
    public ArrayList<Route>  getRoutes(String from, String to, @Nullable Integer date ) {
        ArrayList<Route> r = new ArrayList<>();
        SQLiteDatabase db = null;
        try {
            if (date == null ) {
                date = Integer.parseInt(Utils.getLocaDate(0));
            }
            db = sql.getReadableDatabase();
            ArrayList<HashMap<String, Object>> rotues = Utils.parseCursor(SQLHelper.getRoutes(db, from, to, date));
            for (HashMap<String, Object> rt : rotues) {
                r.add(new Route(rt));
            }
        } catch(Exception e ) {
            Log.d("SVC", "error during getRoutes " + e.getMessage());
        }
        finally {
            if (db != null) {
                try {db.close(); } catch(Exception e) {}
            }
        }
        return r;
    }
    public class DepartureVisionData {
        public String time;
        public String to;
        public String track;
        public String line;
        public String status;
        public String trip;
        public String station;

        public DepartureVisionData(HashMap<String, Object> data) {
            time = data.get("time").toString();
            to = data.get("to").toString();
            track = data.get("track").toString();
            line = data.get("line").toString();
            status = data.get("status").toString();
            trip = data.get("train").toString();
            station = data.get("station").toString();
        }
    }
    public HashMap<String, ArrayList<HashMap<String, Object>>> getCachedDepartureVisionStatus() {
        return  status;
    }
    public HashMap<String, DepartureVisionData>getCachedDepartureVisionStatus_byTrip() {
        return  status_by_trip;
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
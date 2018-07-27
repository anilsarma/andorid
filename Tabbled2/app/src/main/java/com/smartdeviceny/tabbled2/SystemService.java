package com.smartdeviceny.tabbled2;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.smartdeviceny.tabbled2.adapters.ServiceConnected;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class SystemService extends Service {

    boolean           checkingVersion;
    DownloadFile downloader;
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
        IntentFilter filter = new IntentFilter();
        filter.addAction(NotificationValues.BROADCAT_SEND_DEPARTURE_VISION_PING);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);

        setupDb();
        sendDatabaseReady();
        checkForUpdate();
        //Nyquist criteria
    }

    @Override
    public void onDestroy() {
        if(downloader !=null) {
            downloader.cleanup();
        }
        if( sql != null ) {
            sql.close();
            sql = null;
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
                Utils.delete(file);
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
                        Utils.delete(dbFilePath);
                        Log.d("SQL", "renamed file " + tmpFilename.getAbsolutePath() + " to " + dbFilePath.getAbsolutePath());
                    }

                    Log.d("SQL", "renamed file " + tmpFilename.getAbsolutePath() + " to " + dbFilePath.getAbsolutePath());
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
                    Utils.delete(file);
                    sendCheckcomplete();
                    sendDatabaseReady();

                }
                return true;
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
        setupDb();
        return new RemoteBinder(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("SVC", "SystemService is now running");
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendDatabaseReady() {
        if( isDatabaseReady() ) {
            Intent intent = new Intent(NotificationValues.BROADCAT_DATABASE_READY);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            Log.d("SVC", "sending database ready");
        }
    }

    private void sendCheckcomplete() {
        Intent intent = new Intent(NotificationValues.BROADCAT_DATABASE_CHECK_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d("SVC", "sending " + NotificationValues.BROADCAT_DATABASE_CHECK_COMPLETE);

    }
    private void sendDepartVisionUpdated() {
        Intent intent = new Intent(NotificationValues.BROADCAT_DEPARTURE_VISION_UPDATED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d("SVC", "sending " + NotificationValues.BROADCAT_DEPARTURE_VISION_UPDATED);
        //Toast.makeText(getApplicationContext(),"sending " + NotificationValues.BROADCAT_DEPARTURE_VISION_UPDATED, Toast.LENGTH_SHORT).show();
    }

    public void sendTimerEvent() {
        Intent intent = new Intent(NotificationValues.BROADCAT_PERIODIC_TIMER);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d("SVC", "sending " + NotificationValues.BROADCAT_PERIODIC_TIMER);
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

    ArrayList<HashMap<String, Object>> parseDepartureVision(String station, Document doc) {
        //Log.d("DV", "parsing Departure vision Doc");
        ArrayList<HashMap<String, Object>> result =  new ArrayList<HashMap<String, Object>>();
        try {
            Element table = doc.getElementById("GridView1");
            Node node = table;
            List<Node> child = node.childNodes().get(1).childNodes();
            String header_string = "";
            if (child.size()>0) {
                // discard the frist 3
                //Log.d("DV", "child ===================== Size:" + child.size());
                Node header = child.get(1);
                List<Node> header_elements = header.childNodes();
                Element h = (Element) header_elements.get(0);
                header_string = h.child(0).html().toString() + " " + h.child(1).html().toString().replace("&nbsp; &nbsp; Select a train to view station stops", "");
                System.out.println(header_string);
            }


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
                HashMap<String, String> stylemap = new HashMap<>();

                try {
                    String style[] = ((Element)tr).attr("style").split(";");
                    for(String s:style) {
                        String nvp[] = s.split(":");
                        stylemap.put(nvp[0], nvp[1]);
                    }
                } catch (Exception e) {

                }

                String time = ((Element)td.get(1)).html().toString();
                String to =  ((Element)td.get(3)).html().toString();
                String track = ((Element)td.get(5)).html().toString();
                String line = ((Element)td.get(7)).html().toString();
                String train = ((Element)td.get(9)).html().toString();
                String status =  ((Element)td.get(11)).html().toString();
                String background =  stylemap.get("background-color");
                String foreground =  stylemap.get("color");

                data.put("time", time);
                data.put("to", to);
                data.put("track", track);
                data.put("line", line);
                data.put("status", status);
                data.put("train", train);
                data.put("station", station);
                data.put("background", background);
                data.put("foreground", foreground);
                //Log.d("DV", "details time:" + time +  " to:" + to + " track:" + track + " line:" + line + " status:" + status + " train:" + train + " station:" + station );
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
    ArrayList<String> dvPendingRequests = new ArrayList<>();
    HashMap<String, Date> lastRequestTime = new HashMap<>();
    HashMap<String, Date> lastApiCallTime = new HashMap<>();
    public Date updateDapartureVisionCheck(String station)
    {
        Log.d("SVC", "updateDapartureVisionCheck for DV:" + station );
        synchronized (lastRequestTime) {
            Date last = lastApiCallTime.get(station);
            lastRequestTime.put(station, new Date());
            return last;
        }
    }
    // the idea here is that this will be periodically triggered
    // when ever we have data.
    public boolean sendDepartureVisionPings()
    {
        Date now = new Date();
        boolean scheduled = false;
        synchronized (lastRequestTime) {
            for(String station:lastRequestTime.keySet()) {
                Date dt = lastRequestTime.get(station);
                if( (now.getTime() - dt.getTime())< (10 * 1000 * 60 ) ) { // in minutes TODO:: make this configurable by user.
                    Log.d("SVC", "scheduling request for DV:" + station );
                    try {
                        _getDepartureVision(station, 30000);
                    } catch(Exception e) {
                        // don't really care.
                    }
                    scheduled = true;
                } else {
                    Log.d("SVC", "Request expired DV:" + station + " Time:" + dt + " now:" + now + " diff:" + (now.getTime() - dt.getTime())/1000  + " secs");
                }
            }
        }
        return scheduled;
    }
    public void getDepartureVision(String station, @Nullable Integer check_lastime) {
        String url = "http://dv.njtransit.com/mobile/tid-mobile.aspx?sid="+ station;
        Date last = updateDapartureVisionCheck(station);
        synchronized (dvPendingRequests) {
            if (dvPendingRequests.contains(url)) {
                Log.d("SVC", "pending request found for  DV:" + station );
                return;
            }
            dvPendingRequests.add(url);
        }


        _getDepartureVision(station, check_lastime);
    }

    public void _getDepartureVision(String station, @Nullable  Integer check_lastime) {
        String url = "http://dv.njtransit.com/mobile/tid-mobile.aspx?sid="+ station;

        if ( check_lastime == null ) {
            check_lastime = new Integer(10000);
        }
        if ( check_lastime == null ) {
            check_lastime = new Integer(10000);
        }
        Date last = null;
        synchronized (lastRequestTime) {
            last = lastApiCallTime.get(station);
        }
        if (check_lastime > 0 && last!=null) {
            Date now = new Date();
            if(check_lastime > 0) {
                if ((now.getTime()-last.getTime())<check_lastime) {
                    return; // too early
                }
            }
        }

        synchronized (lastRequestTime) {
            lastApiCallTime.put(station, new Date());
        }
        // check if we have a recent download, less than 1 minute old
        final DownloadFile d = new DownloadFile(getApplicationContext(), new DownloadFile.Callback() {
            @Override
            public boolean downloadComplete(DownloadFile d, long id, String url, File file) {
                try {

                    //Log.d("DV", "File Content\n" + Utils.getEntireFileContent(file));
                    Document doc = Jsoup.parse(file, null, "http://dv.njtransit.com");
                    ArrayList<HashMap<String, Object>> result = parseDepartureVision(station, doc);
                    status.put(station, result);
                    for(HashMap<String, Object> dv:result) {
                        DepartureVisionData dd = new DepartureVisionData(dv);
                        status_by_trip.put(dd.block_id, dd);
                    }
                    sendDepartVisionUpdated();
                    // send this off on an intent.
                } catch(Exception e) {
                    Log.d("SVC", "Failed to parse soup " + e.getMessage());
                } finally {
                    synchronized (dvPendingRequests) {
                        dvPendingRequests.remove(url);
                    }
                }
                Utils.delete(file);
                return true;
            }

            @Override
            public void downloadFailed(DownloadFile d,long id, String url) {
                try {
                    Log.d("SVC", "download of SQL file failed " + url);
                } finally {
                    synchronized (dvPendingRequests) {
                        dvPendingRequests.remove(url);
                    }
                }
            }
        });

        d.downloadFile(url, "njts_departure_vision_" + station.toLowerCase() + ".html",
                "NJ Transit DepartureVision",
                DownloadManager.Request.NETWORK_MOBILE| DownloadManager.Request.NETWORK_WIFI, "text/html");

    }
    public class Route {
        public String departture_time;
        public String arrival_time;
        public String block_id;
        public String route_name;
        public String trip_id;

        public String date;
        public String header;
        public String from;
        public String to;

        public Date date_as_date;
        public Date departure_time_as_date;
        public Date arrival_time_as_date;

        public Route(String date, String from, String to, HashMap<String, Object> data) {
            departture_time = data.get("departure_time").toString();
            arrival_time = data.get("destination_time").toString();
            block_id = data.get("block_id").toString();
            route_name = data.get("route_long_name").toString();
            trip_id = data.get("trip_id").toString();
            this.from = from;
            this.to = to;

            try {
                // remember hours are more than 24 hrs here to represent the next day.
                this.departure_time_as_date = dateTim24HrFmt.parse(date + " " + departture_time);
                this.arrival_time_as_date = dateTim24HrFmt.parse(date + " " + arrival_time);

                this.date = dateFmt.format(departure_time_as_date);
                this.date_as_date = dateFmt.parse(this.date);

            } catch (ParseException e) {
                e.printStackTrace();
            }
            this.date  = "" + this.date;
            this.header = this.date + " " + from + " => " + to;

        }



        public Date getDate(String time) throws ParseException {
            DateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
            Date tm = dateTimeFormat.parse(date + " "  + time);
            return tm;
        }

        public String getPrintableTime(String time) throws ParseException {
            SimpleDateFormat printFormat = new SimpleDateFormat("hh:mm a");
            return printFormat.format(getDate(time));
        }
    }
    final DateFormat dateTim24HrFmt = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
    final DateFormat time24HFmt = new SimpleDateFormat("HH:mm:ss");
    final DateFormat dateFmt = new SimpleDateFormat("yyyyMMdd");
    // this is a syncronous call
    public ArrayList<Route>  getRoutes(String from, String to, @Nullable Integer date ) {
        ArrayList<Route> r = new ArrayList<>();
        SQLiteDatabase db = null;
        try {
            if (date == null ) {
                date = Integer.parseInt(Utils.getLocaDate(0));
            }
            try {
                db = sql.getReadableDatabase();
            } catch (Exception e) {
                sql.opendatabase();
                db = sql.getReadableDatabase();
            }

            for(int i=-1; i < 2; i ++ ) {
                Date stDate = dateFmt.parse("" + date);
                stDate = Utils.adddays(stDate, i);
                ArrayList<HashMap<String, Object>> routes = Utils.parseCursor(SQLHelper.getRoutes(db, from, to, Integer.parseInt(dateFmt.format(stDate))));
                Log.d("SVC", "route " + stDate + " " + from + " to " +to );
                for (HashMap<String, Object> rt : routes) {
                    r.add(new Route(dateFmt.format(stDate), from, to, rt));
                }
            }
        } catch(Exception e ) {
            Log.d("SVC", "error during getRoutes " + e.getMessage());
        }

        return r;
    }
    public class DepartureVisionData {
        public String header="";
        public String time="";
        public String to="";
        public String track="";
        public String line="";
        public String status="";
        public String block_id="";
        public String station="";
        public Date   createTime=new Date(); // time this object was created
        public boolean stale = false;
        public DepartureVisionData() {
        }

        public DepartureVisionData(HashMap<String, Object> data) {
            time = data.get("time").toString();
            to = data.get("to").toString();
            track = data.get("track").toString();
            line = data.get("line").toString();
            status = data.get("status").toString();
            block_id = data.get("train").toString();
            station = data.get("station").toString();

            header  = " " + createTime + " "  + to;
            createTime = new Date();
        }


        public DepartureVisionData clone()  {
            DepartureVisionData obj = new DepartureVisionData();
            // TODO not sure how to clone strings ..
            obj.time = "" + this.time;
            obj.to = "" + this.to;
            obj.track = "" + this.track;
            obj.line = "" + this.line;
            obj.status = "" + this.status;
            obj.block_id = "" + this.block_id;
            obj.station = "" + this.station;
            obj.createTime = this.createTime;

            return obj;
        }
    }
    public HashMap<String, ArrayList<HashMap<String, Object>>> getCachedDepartureVisionStatus() {
        return  status;
    }
    public HashMap<String, DepartureVisionData>getCachedDepartureVisionStatus_byTrip() {
        return  status_by_trip;
    }



    public class LocalBcstReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.d("MAIN", "onReceive " + intent.getAction());
            if (intent.getAction().equals(NotificationValues.BROADCAT_SEND_DEPARTURE_VISION_PING )) {
                SystemService.this.sendDepartureVisionPings();
            }
            else {
                Log.d("receiver", "got omething not sure what " + intent.getAction());
            }
        }
    }
    // Our handler for received Intents. This will be called whenever an Intent
    private BroadcastReceiver mMessageReceiver = new SystemService.LocalBcstReceiver();
}
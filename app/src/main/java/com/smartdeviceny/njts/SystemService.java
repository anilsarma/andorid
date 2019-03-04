package com.smartdeviceny.njts;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.smartdeviceny.njts.utils.DownloadFile;
import com.smartdeviceny.njts.utils.SQLHelper;
import com.smartdeviceny.njts.utils.SQLiteLocalDatabase;
import com.smartdeviceny.njts.utils.SqlUtils;
import com.smartdeviceny.njts.utils.TimerKeeper;
import com.smartdeviceny.njts.utils.Utils;
import com.smartdeviceny.njts.utils.UtilsDBVerCheck;
import com.smartdeviceny.njts.values.Config;
import com.smartdeviceny.njts.values.ConfigDefault;
import com.smartdeviceny.njts.values.NotificationValues;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.io.File;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SystemService extends Service {

    boolean checkingVersion;
    DownloadFile downloader;
    SQLiteLocalDatabase sql;
    SharedPreferences config;
    HashSet<String> favorites = new HashSet<>();

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

    public SystemService() {
    }

    @Override
    public void onCreate() {
        config = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Set<String> tmp = config.getStringSet(Config.FAVORITES, favorites);
        favorites = new HashSet<>(tmp);

        for (String f : favorites) {
            Log.d("SVC", "read current fav " + f);
        }
        downloader = new DownloadFile(this.getApplicationContext(), callback);
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(NotificationValues.BROADCAT_SEND_DEPARTURE_VISION_PING);
        filter.addAction(NotificationValues.BROADCAT_CHECK_FOR_UPDATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);

        setupDb();
        sendDatabaseReady();
    }

    @Override
    public void onDestroy() {
        if (downloader != null) {
            downloader.cleanup();
        }
        if (sql != null) {
            sql.close();
            sql = null;
        }
        super.onDestroy();
    }

    public boolean checkForUpdate(boolean silent) {
        if (checkingVersion) {
            Log.d("SVC", "update already running");
            return false; // already running.
        }
        _checkRemoteDBUpdate(silent);
        return checkingVersion;
    }

    public boolean isUpdateRunning() {
        return checkingVersion;
    }


    void _checkRemoteDBUpdate(boolean silent) {
        Log.d("SVC", "checking for updated schedule db");
        File f = new File(getApplicationContext().getApplicationInfo().dataDir + File.separator + "rails_db.sql");
        sql = UtilsDBVerCheck.getSQLDatabase(getApplicationContext(), f);
        checkingVersion = true;
        if (sql == null) {
            _checkRemoteDBZipUpdate(silent, ""); // download it anyway we dont have a valid database.
            return;
        }
        if (versionPendingRequests.getPending("version.txt") > 0) {
            Log.d("SVC", "pending request for version.txt found");
        }
        config.edit().putLong(Config.UPDATE_LAST_CHECK_TIME, new Date().getTime()).commit();
        final DownloadFile d = new DownloadFile(getApplicationContext(), new DownloadFile.Callback() {
            @Override
            public boolean downloadComplete(DownloadFile d, long id, String url, File file) {
                try {
                    String version_str = Utils.getFileContent(file);
                    _checkRemoteDBZipUpdate(silent, version_str);
                    Utils.delete(file);
                    Utils.cleanFiles(file.getParentFile(), "version");
                } finally {
                    versionPendingRequests.updatePending("version.txt", -1, null);
                }
                return true;
            }

            @Override
            public void downloadFailed(DownloadFile d, long id, String url) {
                try {
                    Log.d("SQL", "download of SQL file failed " + url);
                    checkingVersion = false;  // could not get a version string, we will do it later.
                } finally {
                    versionPendingRequests.updatePending("version.txt", -1, null);
                }
            }
        });
        versionPendingRequests.updatePending("version.txt", 1, null);
        d.downloadFile("https://github.com/anilsarma/misc/raw/master/njt/version.txt", "version.txt", "NJ Transit Schedules Version",
                DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI, null);
    }

    // never call this directly shold be called via _checkRemoteDBUpdate
    private void _checkRemoteDBZipUpdate(boolean silent, String version_str) {
        File f = new File(getApplicationContext().getApplicationInfo().dataDir + File.separator + "rails_db.sql");
        sql = UtilsDBVerCheck.getSQLDatabase(getApplicationContext(), f);
        if (UtilsDBVerCheck.matchDBVersion(sql, version_str)) {
            checkingVersion = false;
            Log.d("DBSVC", "system schedule db is up-todate " + version_str);
            sendCheckcomplete();
            if (!silent) {
                notify_user_of_upgrade("Schedule Database", "No Update Required, version " + getDBVersion());
            }
            return;
        }
        final DownloadFile d = new DownloadFile(getApplicationContext(), new DownloadFile.Callback() {
            @Override
            public boolean downloadComplete(DownloadFile d, long id, String url, File file) {
                notify_user_of_upgrade("Schedule Database", "Download Complete");
                checkingVersion = false;
                File dbFilePath = new File(getApplicationContext().getApplicationInfo().dataDir + File.separator + "rails_db.sql");
                File tmpFilename = null;
                File tmpVersionFilename = null;
                try {
                    tmpFilename = UtilsDBVerCheck.createTempFile(file, dbFilePath.getParentFile(), "rail_data.db");
                    tmpVersionFilename = UtilsDBVerCheck.createTempFile(file, dbFilePath.getParentFile(), "version.txt");
                    String version_str = Utils.getFileContent(tmpVersionFilename);

                    if (!tmpFilename.exists()) {// extracted file does not exit some thing is wong
                        return true; // remove the downloaded files.
                    }
                    if (UtilsDBVerCheck.matchDBVersion(sql, version_str)) {
                        return true;
                    }
                    if (sql != null) {
                        sql.close();
                        sql = null;
                        Utils.delete(dbFilePath);
                        //Utils.cleanFiles(file.getParentFile(), "rail_data_db");
                        Log.d("SQL", "renamed file " + tmpFilename.getAbsolutePath() + " to " + dbFilePath.getAbsolutePath());
                    }

                    Log.d("SQL", "renamed file " + tmpFilename.getAbsolutePath() + " to " + dbFilePath.getAbsolutePath());
                    tmpFilename.renameTo(dbFilePath);
                    tmpFilename = null;
                    //dbFilePath = new File(dbFilePath.getAbsolutePath());
                    int retries = 0;
                    while (retries < 5) {
                        retries++;
                        try {
                            sql = new SQLiteLocalDatabase(getApplicationContext(), dbFilePath.getName(), null);
                            SqlUtils.create_user_pref_table(sql.getWritableDatabase());
                            SqlUtils.update_user_pref(sql.getWritableDatabase(), "version", version_str, new Date());
                            // let the user know we have upgraded.
                            notify_user_of_upgrade("Schedule Database", "upgraded to " + version_str);
                            break;
                        } catch (Exception e) {
                            try {
                                Thread.sleep(100);
                            } catch (Exception e1) {
                            }
                            Log.d("SVC", "failed to get sql retries:" + retries);
                            // check sql
                            if (sql != null) {
                                // this is definately a sync problem, need to fix this. TODO::
                                // or the notify failed.
                                Log.d("SVC", "failed to get sql but sql object already set.");
                                break;
                            }
                        }
                    }
                } finally {
                    Utils.delete(tmpFilename);
                    Utils.delete(tmpVersionFilename);
                    Utils.delete(file);
                    sendCheckcomplete();
                    sendDatabaseReady();

                }
                return true;
            }

            @Override
            public void downloadFailed(DownloadFile d, long id, String url) {
                Log.d("SQL", "download of SQL file failed " + url);
                checkingVersion = false;
                sendCheckcomplete();
            }
        });

        d.downloadFile("https://github.com/anilsarma/misc/raw/master/njt/rail_data_db.zip", "rail_data_db.zip", "NJ Transit Schedules",
                DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI, "application/zip");
        checkingVersion = true;
    }

    public void doForceCheckUpgrade() {

        try {
            DateFormat dateTimeFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss - 0.00");
            if (sql != null) {
                SqlUtils.create_user_pref_table(sql.getWritableDatabase());
                Date now = new Date();
                SqlUtils.update_user_pref(sql.getWritableDatabase(), "version", dateTimeFormat.format(now), now);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        if (isDatabaseReady()) {
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
        if (sql == null) {
            return false;
        }
        SQLiteDatabase db = sql.getWritableDatabase();
        if (SqlUtils.check_if_user_pref_exists(db)) {
            Log.d("SVC", "checking if database is ready.");
            if (SqlUtils.check_if_table_exists(db, "routes") && SqlUtils.check_if_table_exists(db, "trips")) {
                Log.d("SVC", "database is ready.");
                return true;
            }
        }
        Log.d("SVC", "database is not ready.");
        return false;
    }

    private void setupDb() {
        if (sql == null) {
            File f = new File(getApplicationContext().getApplicationInfo().dataDir + File.separator + "rails_db.sql");
            if (f.exists()) {
                sql = new SQLiteLocalDatabase(getApplicationContext(), f.getName(), null);
            }
        }
        if (sql != null) {
            // check for a valid database.
            String db = UtilsDBVerCheck.getDBVersion(sql);
            if (db.isEmpty()) {
                checkForUpdate(false);
            }
        } else {
            // we don't have a  db get it.
            checkForUpdate(false);
        }
    }

    private void notify_user_of_upgrade(@NonNull String title, @NonNull String msg) {
        Utils.notify_user(this.getApplicationContext(), "NJTS", title, msg, 2);
    }

    HashMap<String, DepartureVisionData> parseDepartureVision(String station, Document doc) {
        //Log.d("DV", "parsing Departure vision Doc");
        HashMap<String, DepartureVisionData> result = new HashMap<>();
        try {
            Element table = doc.getElementById("GridView1");
            Node node = table;
            List<Node> child = node.childNodes().get(1).childNodes();
            String header_string = "";
            if (child.size() > 0) {
                // discard the frist 3
                //Log.d("DV", "child ===================== Size:" + child.size());
                Node header = child.get(1);
                List<Node> header_elements = header.childNodes();
                Element h = (Element) header_elements.get(0);
                header_string = h.child(0).html().toString() + " " + h.child(1).html().toString().replace("&nbsp; &nbsp; Select a train to view station stops", "");
                //System.out.println(header_string);
            }


            // discard the frist 3
            //Log.d("DV", "child ===================== Size:" + child.size());
            for (int i = 3; i < child.size(); i++) {
                Node tr = child.get(i);
                List<Node> td = tr.childNodes();
                //Log.d("DV", "childNodes(td) ===================== Size:" + td.size());

                if (td.size() < 4) {
                    continue;
                }
                HashMap<String, Object> data = new HashMap<>();
                HashMap<String, String> stylemap = new HashMap<>();

                try {
                    String style[] = ((Element) tr).attr("style").split(";");
                    for (String s : style) {
                        String nvp[] = s.split(":");
                        stylemap.put(nvp[0], nvp[1]);
                    }
                } catch (Exception e) {

                }

                String time = ((Element) td.get(1)).html().toString();
                String to = ((Element) td.get(3)).html().toString();
                String track = ((Element) td.get(5)).html().toString();
                String line = ((Element) td.get(7)).html().toString();
                String train = ((Element) td.get(9)).html().toString();
                String status = ((Element) td.get(11)).html().toString();
                String background = stylemap.get("background-color");
                String foreground = stylemap.get("color");

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
                DepartureVisionData dv = new DepartureVisionData(data);
                result.put(dv.block_id, dv);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //Log.d("DV", "result=" + result.size());
        return result;
    }

    HashMap<String, HashMap<String, DepartureVisionData>> status = new HashMap<>();
    Integer lock_status_by_trip = new Integer(0);
    HashMap<String, DepartureVisionData> status_by_trip = new HashMap<>();

    TimerKeeper versionPendingRequests = new TimerKeeper();
    TimerKeeper dvPendingRequests = new TimerKeeper();

    public void updateActiveDepartureVisionStation(String station) {
        Log.d("SVC", "updateActiveDepartureVisionStation(async) for DV:" + station);
        new AsyncTask<String, String, Date>() {
            @Override
            protected Date doInBackground(String... station) {
                ArrayList<String> stations = dvPendingRequests.getActiveStations();
                if (!stations.contains(station[0])) {
                    dvPendingRequests.active(station[0]);
                    synchronized (lock_status_by_trip) {
                        status_by_trip = status.get(station[0]);
                        if (status_by_trip == null) {
                            status_by_trip = new HashMap<>();
                        }
                        for (DepartureVisionData dd : status_by_trip.values()) {
                            dd.favorite = false;
                            if (favorites.contains(dd.block_id)) {
                                dd.favorite = true;
                            }
                        }

                    }
                    sendDepartVisionUpdated();
                }
                return new Date();

            }
        }.execute(station);
    }


    // the idea here is that this will be periodically triggered
    // when ever we have data.
    public boolean sendDepartureVisionPings() {
        Date now = new Date();
        boolean scheduled = false;
        ArrayList<String> reqs = dvPendingRequests.getActiveStations();

        for (String station : reqs) {
            try {
                scheduled = true;
                Log.d("SVC", "sending ping request for DV:" + station);
                _getDepartureVision(station, 30000);
            } catch (Exception e) {
                // don't really care.
            }
        }
        return scheduled;
    }

    public void schdeuleDepartureVision(String station, @Nullable Integer check_lastime) {
        // make this call async
        class Param {
            public String station;
            public Integer check_lastime;

            Param(String station, @Nullable Integer check_lastime) {
                this.station = station;
                this.check_lastime = check_lastime;
            }
        }
        new AsyncTask<Param, Integer, String>() {

            @Override
            protected String doInBackground(Param... param) {
                _getDepartureVision(param[0].station, param[0].check_lastime);
                return "";
            }
        }.execute(new Param(station, check_lastime));
    }

    public void updateDepartureVision(String station, HashMap<String, DepartureVisionData> byBlockNew) {
        HashMap<String, DepartureVisionData> byBlock = status.get(station);
        byBlock = (byBlock == null) ? new HashMap<>() : byBlock;
        HashMap<String, DepartureVisionData> byTrack = new HashMap<>();
        for (DepartureVisionData dv : byBlock.values()) {
            if (dv.track.isEmpty()) {
                continue; // remove empty tracks from the old entries.
            }
            DepartureVisionData hasData = byBlockNew.get(dv.block_id);
            if (hasData == null) {
                dv.stale = true;
                dv.status = "";
            }
            byBlock.put(dv.block_id, dv);
            byTrack.put(dv.track, dv);
        }

        for (DepartureVisionData dv : byBlockNew.values()) {
            DepartureVisionData old = byTrack.get(dv.track);
            if (old != null && !dv.track.isEmpty()) {
                old.stale = true;
                old.status = ""; // clear the status but keep the track.
            }
            byBlock.put(dv.block_id, dv);
        }
        HashMap<String, DepartureVisionData> cleanEntries = new HashMap<>();
        Date now = new Date();
        for (DepartureVisionData dv : byBlock.values()) {
            // now trim any old entries if we the size is too big.
            if ((now.getTime() - dv.createTime.getTime()) < (8 * 60 * 60 * 1000)) {
                cleanEntries.put(dv.block_id, dv);
            }
        }
        status.put(station, cleanEntries);
    }

    public void _getDepartureVision(String station, @Nullable Integer check_lastime) {
        String url = "http://dv.njtransit.com/mobile/tid-mobile.aspx?sid=" + station;

        if (dvPendingRequests.getPending(station) > 0) {
            Log.d("SVC", "we already have a pending request for this station dv " + station);
            return;
        }
        Date lastRequesTime = dvPendingRequests.getLastRequestTime(station);
        Date now = new Date();

        if (check_lastime != null) {
            long diff = now.getTime() - lastRequesTime.getTime();
            if (diff < check_lastime) {
                Log.d("SVC", "need to wait for:" + station + " current:" + diff + " need:" + check_lastime + " diff:" + (check_lastime - diff));
                return;
            }
        }

        // check if we have a recent download, less than 1 minute old
        final DownloadFile d = new DownloadFile(getApplicationContext(), new DownloadFile.Callback() {
            String code = station;

            @Override
            public boolean downloadComplete(DownloadFile d, long id, String url, File file) {
                try {
                    //Log.d("DV", "File Content\n" + Utils.getEntireFileContent(file));
                    Log.d("SVC", "DV download complete for " + url);
                    Document doc = Jsoup.parse(file, null, "http://dv.njtransit.com");
                    HashMap<String, DepartureVisionData> result = parseDepartureVision(code, doc);
                    updateDepartureVision(code, result);
                    // we should use only the active stations
                    HashMap<String, DepartureVisionData> tmp_trip = new HashMap<>();
                    ArrayList<String> activeList = dvPendingRequests.getActiveStations();
                    if (activeList.isEmpty()) {
                        activeList.add(code);// just so that things are not empty.
                    }


                    for (String key : activeList) {
                        for (DepartureVisionData dd : status.get(key).values()) {
                            dd.favorite = false;
                            if (favorites.contains(dd.block_id)) {
                                dd.favorite = true;
                            }
                            tmp_trip.put(dd.block_id, dd);
                            //Log.d("SVC", "entry code=" + code + " key=" + key + " " + dd.createTime + " " + dd.time + " train:" + dd.block_id + " " + dd.station  + " track#" + dd.track + " stale:" + dd.stale);
                        }
                    }
                    synchronized (lock_status_by_trip) {
                        status_by_trip = tmp_trip;
                    }
                    sendDepartVisionUpdated();
                    // send this off on an intent.
                } catch (Exception e) {
                    Log.d("SVC", "Failed to parse soup " + e.getMessage());
                } finally {
                    dvPendingRequests.updatePending(station, -1, null);
                }
                Utils.delete(file);
                Utils.cleanFiles(file.getParentFile(), "njts_departure_vision_" + station.toLowerCase());
                return true;
            }

            @Override
            public void downloadFailed(DownloadFile d, long id, String url) {
                try {
                    Log.d("SVC", "download of SQL file failed " + url);
                } finally {
                    dvPendingRequests.updatePending(station, -1, null);
                }
            }
        });

        DownloadManager.Request request = d.buildRequest(url, "njts_departure_vision_" + station.toLowerCase() + ".html", "NJ Transit DepartureVision",
                DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI, "text/html");

        request.setNotificationVisibility(
                config.getBoolean(Config.DEBUG, ConfigDefault.DEBUG) ? DownloadManager.Request.VISIBILITY_VISIBLE : DownloadManager.Request.VISIBILITY_HIDDEN);
        long id = d.enqueue(request);
        Log.d("SVC", "downloadFile enqueued " + url + " " + id);
        synchronized (dvPendingRequests) {
            dvPendingRequests.updatePending(station, 1, null);
        }

    }

    public class Route {
        public String station_code;
        public String departture_time;
        public String arrival_time;
        public String block_id;
        public String route_name;
        public String trip_id;

        public String date;
        public String header;
        public String from;
        public String to;
        public boolean favorite = false;

        public Date date_as_date;
        public Date departure_time_as_date;
        public Date arrival_time_as_date;

        public Route(String station_code, String date, String from, String to, HashMap<String, Object> data) {
            this.station_code = station_code;
            departture_time = data.get("departure_time").toString();
            arrival_time = data.get("destination_time").toString();
            block_id = data.get("block_id").toString();
            route_name = data.get("route_long_name").toString();
            trip_id = data.get("trip_id").toString();
            this.from = from;
            this.to = to;
            if (favorites != null) {
                this.favorite = favorites.contains(this.block_id);
            }
            try {
                // remember hours are more than 24 hrs here to represent the next day.
                this.departure_time_as_date = dateTim24HrFmt.parse(date + " " + departture_time);
                this.arrival_time_as_date = dateTim24HrFmt.parse(date + " " + arrival_time);

                this.date = dateFmt.format(departure_time_as_date);
                this.date_as_date = dateFmt.parse(this.date);

            } catch (ParseException e) {
                e.printStackTrace();
            }
            this.date = "" + this.date;
            this.header = this.date + " " + from + " => " + to;

        }


        public Date getDate(String time) throws ParseException {
            DateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
            Date tm = dateTimeFormat.parse(date + " " + time);
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

    // this is a syncronous call TODO: make async implementation.
    public ArrayList<Route> getRoutes(String from, String to, @Nullable Integer date, @Nullable Integer delta) {
        ArrayList<Route> r = new ArrayList<>();
        SQLiteDatabase db = null;
        if (delta == null) {
            delta = new Integer(2);
        }
        try {
            if (date == null) {
                date = Integer.parseInt(Utils.getLocaDate(0));
            }
            try {
                db = sql.getReadableDatabase();
            } catch (Exception e) {
                sql.opendatabase();
                db = sql.getReadableDatabase();
            }

            String station_code = getStationCode(from);
            delta = Math.max(1, delta);
            for (int i = -delta; i < delta; i++) {
                Date stDate = dateFmt.parse("" + date);
                stDate = Utils.adddays(stDate, i);
                ArrayList<HashMap<String, Object>> routes = Utils.parseCursor(SQLHelper.getRoutes(db, from, to, Integer.parseInt(dateFmt.format(stDate))));
                Log.d("SVC", "route " + stDate + " " + from + " to " + to);
                for (HashMap<String, Object> rt : routes) {
                    r.add(new Route(station_code, dateFmt.format(stDate), from, to, rt));
                }
            }
        } catch (Exception e) {
            Log.w("SVC", "warn during getRoutes " + e.getMessage());
        }
        return r;
    }

    public class DepartureVisionData {
        public String header = "";
        public String time = "";
        public String to = "";
        public String track = "";
        public String line = "";
        public String status = "";
        public String block_id = "";
        public String station = "";
        public Date createTime = new Date(); // time this object was created
        public boolean stale = false;
        public boolean favorite = false;

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
            favorite = false;
            header = " " + createTime + " " + to;
            createTime = new Date();


        }


        public DepartureVisionData clone() {
            DepartureVisionData obj = new DepartureVisionData();
            // TODO not sure how to clone strings ..
            obj.time = "" + this.time;
            obj.to = "" + this.to;
            obj.track = "" + this.track;
            obj.line = "" + this.line;
            obj.status = "" + this.status;
            obj.block_id = "" + this.block_id;
            obj.station = "" + this.station;
            obj.favorite = this.favorite;
            obj.createTime = this.createTime;
            obj.header = this.header;

            return obj;
        }

        public String toString() {
            StringBuffer str = new StringBuffer();
            str.append("time=" + time);
            str.append(" to=" + to);
            str.append(" track=" + track);
            str.append(" line=" + line);
            str.append(" status=" + status);
            str.append(" block_id=" + block_id);
            str.append(" station=" + station);
            str.append(" favorite=" + favorite);
            str.append(" createTime=" + createTime);
            str.append(" header=" + header);
            return str.toString();

        }
    }

    public HashMap<String, DepartureVisionData> getCachedDepartureVisionStatus_byTrip() {
        synchronized (lock_status_by_trip) {
            return status_by_trip;
        }
    }

    public String getDBVersion() {
        return UtilsDBVerCheck.getDBVersion(sql);
    }

    public String[] get_values(String sqls, String key) {
        if (sql != null) {
            return SQLHelper.get_values(sql.getReadableDatabase(), sqls, key);
        }
        ArrayList<String> njtr = new ArrayList<>();
        return njtr.toArray(new String[]{});
    }

    public String[] getRouteStations(String route_name) {
        Log.d("SVC", "getRouteStations " + route_name + " SQL:" + sql);
        if (sql != null) {
            return SQLHelper.getRouteStations(sql.getReadableDatabase(), route_name);
        }
        ArrayList<String> njtr = new ArrayList<>();
        return njtr.toArray(new String[]{});
    }

    public String getStationCode(String station) {
        if (sql == null) {
            return "NY";
        }
        String value = SqlUtils.getStationCode(sql.getReadableDatabase(), station);
        Log.d("SVC", "looking up station code " + station + "=" + value);
        if (value == null || value.isEmpty()) {
            return "NY";
        }
        return value;
    }

    public ArrayList<HashMap<String, Object>> getTripStops(String trip_id) {
        SQLiteDatabase db = null;
        ArrayList<HashMap<String, Object>> tripStops = new ArrayList<>();
        try {
            db = sql.getReadableDatabase();
        } catch (Exception e) {
            sql.opendatabase();
            db = sql.getReadableDatabase();
        }
        try {
            tripStops = Utils.parseCursor(SQLHelper.getTripStops(db, trip_id));
        } catch (Exception e) {

        }
        return tripStops;

    }

    public void addFavorite(String block_id) {
        if (config != null) {
            //favorites = config.getStringSet(ConfigUtils.FAVORITES, favorites);
            favorites.add(block_id);
            SharedPreferences.Editor editor = config.edit();
            editor.putStringSet(Config.FAVORITES, favorites);
            editor.apply();

            for (String f : favorites) {
                Log.d("SVC", "current fav " + f);
            }
        }
    }

    public void removeFavorite(String block_id) {
        if (config != null) {
            //favorites = config.getStringSet(ConfigUtils.FAVORITES, favorites);
            favorites.remove(block_id);
            SharedPreferences.Editor editor = config.edit();
            editor.putStringSet(Config.FAVORITES, favorites);
            editor.apply();

            for (String f : favorites) {
                Log.d("SVC", "rm current fav " + f);
            }
        }
    }

    public class LocalBcstReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            //Log.d("MAIN", "onReceive " + intent.getAction());
            if (intent.getAction().equals(NotificationValues.BROADCAT_SEND_DEPARTURE_VISION_PING)) {
                SystemService.this.sendDepartureVisionPings();
            } else if (intent.getAction().equals(NotificationValues.BROADCAT_CHECK_FOR_UPDATE)) {
                Date now = new Date();
                Date d = new Date(config.getLong(Config.UPDATE_LAST_CHECK_TIME, Utils.adddays(new Date(), -1).getTime()));
                // every hour.
                if ((now.getTime() - d.getTime()) > 1 * 60 * 1000) {
                    SystemService.this.checkForUpdate(true);
                }
            } else {
                Log.d("receiver", "got something not sure what " + intent.getAction());
            }
        }
    }

    // Our handler for received Intents. This will be called whenever an Intent
    private BroadcastReceiver mMessageReceiver = new SystemService.LocalBcstReceiver();
}
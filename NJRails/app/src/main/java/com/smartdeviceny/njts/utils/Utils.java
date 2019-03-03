package com.smartdeviceny.njts.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.smartdeviceny.njts.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by asarma on 11/2/2017.
 */

public class Utils {

    static public String getTodayYYYYMMDD(@Nullable Date dt) {
        if (dt == null) {
            dt = new Date();
        }
        DateFormat dateFormat = new SimpleDateFormat("YYYYMMdd");
        return dateFormat.format(dt);
    }

    static public String formatToLocalDate(Date dt) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();

        DateFormat dateFormat = new SimpleDateFormat("YYYY/MM/dd");
        dateFormat.setTimeZone(tz);

        return dateFormat.format(dt);
    }

    static public String formatToLocalTime(Date dt) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();

        DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss a");
        dateFormat.setTimeZone(tz);

        return dateFormat.format(dt);
    }

    static Calendar make_cal(Date date, int field, int ammount) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        ca.add(field, ammount);

        return ca;
    }

    static public String formatToLocalDateTime(Date dt) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();

        DateFormat dateFormat = new SimpleDateFormat("YYYYMMdd HH:mm:ss");
        dateFormat.setTimeZone(tz);

        return dateFormat.format(dt);
    }

    static public String getLocaDateTime() {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();

        DateFormat dateFormat = new SimpleDateFormat("YYYYMMdd HH:mm:ss.SSS z");
        dateFormat.setTimeZone(tz);
        Date date = new Date();
        return dateFormat.format(date);
    }

    static public Date adddays(Date date, int days) {
        //if (days > 0 )
        {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(Calendar.DATE, days);
            date = c.getTime();
        }
        return date;
    }

    static public String getLocaDate(int days) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();

        DateFormat dateFormat = new SimpleDateFormat("YYYYMMdd");
        dateFormat.setTimeZone(tz);
        Date date = new Date();
        if (days > 0) {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(Calendar.DATE, days);
            date = c.getTime();
        }
        return dateFormat.format(date);
    }

    static public Date parseLocalTime(String time) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        // Date date = new Date();
        dateFormat.setTimeZone(tz);
        SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm:ss");
        timeformat.setTimeZone(tz);

        try {
            Date st_time = timeformat.parse(time);
            return st_time;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static public ArrayList<HashMap<String, Object>> parseCursor(Cursor cursor) {
        ArrayList<HashMap<String, Object>> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            HashMap<String, Object> data = new HashMap<>();
            for (int i = 0; i < cursor.getColumnCount(); i++) {
                if (cursor.getType(i) == Cursor.FIELD_TYPE_INTEGER) {
                    data.put(cursor.getColumnName(i), cursor.getInt(i));
                } else if (cursor.getType(i) == Cursor.FIELD_TYPE_FLOAT) {
                    data.put(cursor.getColumnName(i), cursor.getDouble(i));
                } else if (cursor.getType(i) == Cursor.FIELD_TYPE_STRING) {
                    data.put(cursor.getColumnName(i), cursor.getString(i).replace("\"", ""));
                }
            }
            result.add(data);
        }
        cursor.close();
        return result;
    }

    public static float pxFromDp(float dp, Context mContext) {
        return dp * mContext.getResources().getDisplayMetrics().density;
    }

    public static int pxFromSp(int sp, Context mContext) {
        return (int) ((double) sp * mContext.getResources().getDisplayMetrics().scaledDensity);
    }


    public static TextView addTextView(Context context, ViewGroup parent, String text, int font_size, int padding) {
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(Utils.pxFromDp(font_size, context));
        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        //params.setMargins(0, 5, 0, 5);
        parent.addView(tv, params);
        tv.setPadding(10, padding, 0, 0);
        return tv;
    }

    public static String capitalize(String str) {
        String strs[] = str.split("\\s");
        StringBuffer b = new StringBuffer();

        for (int i = 0; i < strs.length; i++) {
            if (strs[i].length() > 1 && strs[i].substring(1, 1) != ".") {
                strs[i] = strs[i].substring(0, 1).toUpperCase() + strs[i].substring(1).toLowerCase();
            }
            b.append(strs[i] + " ");
        }
        return b.toString().trim();
    }

    public static String join(String delimit, String data[]) {
        String str = "";
        for (int i = 0; i < data.length; i++) {
            if (!str.isEmpty()) {
                str += delimit;
            }
            str += data[i];
        }
        return str;
    }

    public static ArrayList<String> split(String delimit, String data) {
        ArrayList<String> favA = new ArrayList<String>();
        for (String x : data.split(delimit)) {
            favA.add(x);
        }
        return favA;
    }

    public static ArrayList<HashMap<String, String>> coerce(ArrayList<HashMap<String, Object>> data) {
        ArrayList<HashMap<String, String>> result = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            HashMap<String, String> r = new HashMap<>();
            for (String key : data.get(i).keySet()) {
                r.put(key, data.get(i).get(key).toString());
            }
            result.add(r);
        }
        return result;
    }

    // SQL
    private static final String TAG = Utils.class.getSimpleName();

    public static List<String> splitSqlScript(String script, char delim) {
        List<String> statements = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        boolean inLiteral = false;
        char[] content = script.toCharArray();
        for (int i = 0; i < script.length(); i++) {
            if (content[i] == '"') {
                inLiteral = !inLiteral;
            }
            if (content[i] == delim && !inLiteral) {
                if (sb.length() > 0) {
                    statements.add(sb.toString().trim());
                    sb = new StringBuilder();
                }
            } else {
                sb.append(content[i]);
            }
        }
        if (sb.length() > 0) {
            statements.add(sb.toString().trim());
        }
        return statements;
    }

    public static void writeExtractedFileToDisk(InputStream in, OutputStream outs) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) > 0) {
            outs.write(buffer, 0, length);
        }
        outs.flush();
        outs.close();
        in.close();
    }

    // assumes that there is only one entry in the file.
    public static ZipInputStream getFileFromZip(InputStream zipFileStream) throws IOException {
        ZipInputStream zis = new ZipInputStream(zipFileStream);
        ZipEntry ze;
        while ((ze = zis.getNextEntry()) != null) {
            Log.w(TAG, "extracting file: '" + ze.getName() + "'...");
            return zis;
        }
        return null;
    }

    public static ZipInputStream getFileFromZip(InputStream zipFileStream, String name) throws IOException {
        ZipInputStream zis = new ZipInputStream(zipFileStream);
        ZipEntry ze;
        while ((ze = zis.getNextEntry()) != null) {
            Log.w(TAG, "Zip entry  '" + ze.getName() + "'...");
            if (ze.getName().toUpperCase().equals(name.toUpperCase())) {
                Log.w(TAG, "Found matching file: '" + name + "'...");
                return zis;
            }
        }
        return null;
    }

    public static String convertStreamToString(InputStream is) {
        return new Scanner(is).useDelimiter("\\A").next();
    }

    static public String getFileContent(File file) {
        if (!file.exists()) {
            return "";
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String data = br.readLine();
            br.close();
            return data;
        } catch (IOException e) {
            return "";
        }
    }

    static public String getEntireFileContent(File file) {
        if (!file.exists()) {
            return "";
        }
        StringBuffer str = new StringBuffer();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "";
            while ((line = br.readLine()) != null) {
                str.append(line + "\n");
            }

        } catch (IOException e) {

        }
        return str.toString();
    }

    public static void scheduleJob(Context context, @NonNull Class<?> cls, @Nullable Integer ms_frequency, boolean periodic) {
        ComponentName serviceComponent = new ComponentName(context, cls);
        JobInfo.Builder builder = new JobInfo.Builder(1, serviceComponent);
        if (ms_frequency == null) {
            ms_frequency = new Integer(60 * 1000); // every minute.
        }
        if (periodic) {
            builder.setPeriodic(ms_frequency);
        } else {
            builder.setMinimumLatency(ms_frequency); // wait at least
            builder.setOverrideDeadline((int) (ms_frequency)); // maximum delay
        }
        //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(true); // we don't care if the device is charging or not
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        if (jobScheduler.schedule(builder.build()) <= 0) {
            Log.e("JOB", "error: Some error while scheduling the job");
        } else {
            // Log.d("JOB", "job scheduled " + ms_frequency);
        }
    }

    static public String getExtension(File name) {
        String extension = "";
        String fileName = name.getName();
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }

    static public String getBasename(File name) {
        String extension = "";
        String fileName = name.getName();
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(0, i - 1);
        }
        return extension;
    }

    static public String getExtension(String fileName) {
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i + 1);
        }
        return extension;
    }

    static public String getBasename(String fileName) {
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(0, i - 1);
        }
        return extension;
    }

    static public void delete(File file) {
        if (file != null) {
            try {
                if (file.getAbsoluteFile().delete()) {
                    Log.d("DEL", "deleted " + file.getAbsolutePath());
                } else {
                    Log.e("DEL", "delete failed " + file.getAbsolutePath());
                }
            } catch (Exception e) {
            }
        }
    }

    static public Date makeDate(String yyyymmdd, String time, @Nullable String format) throws ParseException {
        if (format == null) {
            format = "yyyyMMdd HH:mm:ss";
        }
        DateFormat dateTimeFormat = new SimpleDateFormat(format);
        Date tm = dateTimeFormat.parse("" + yyyymmdd + " " + time);
        return tm;
    }

    public static String getConfig(SharedPreferences config, String name, String defaultValue) {
        return config.getString(name, defaultValue);
    }

    public static void setConfig(SharedPreferences config, String name, String value) {
        SharedPreferences.Editor editor = config.edit();
        editor.putString(name, value);
        editor.commit();
    }

    public static void cleanFiles(File dir, String prefix) {
        if( dir ==null ) {
            return;
        }
        for (File f : dir.listFiles()) {
            try {
                if (f.getName().startsWith(prefix)) {
                    //Log.d("UTIL", "will remove " + f.getAbsolutePath());
                    try {
                        f.getAbsoluteFile().delete();
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {

            }
        }
    }

    private static void createNotificationChannel(Context context, String CHANNEL_ID) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static NotificationCompat.Builder makeNotificationBuilder(Context context, String channelID, String title, String msg) {
        createNotificationChannel(context, channelID);
        //int icon = R.mipmap.ic_launcher;
        //long when = System.currentTimeMillis();
//        Notification notification = new Notification(icon, getString(R.string.app_name), when);
//        notification.flags |= Notification.FLAG_NO_CLEAR; //Do not clear the notification
//        notification.defaults |= Notification.DEFAULT_LIGHTS; // LED
//        notification.defaults |= Notification.DEFAULT_VIBRATE; //Vibration
//        notification.defaults |= Notification.DEFAULT_SOUND; // Sound

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelID).setSmallIcon(R.mipmap.app_njs_icon).setContentTitle(title).setVisibility(
                NotificationCompat.VISIBILITY_PUBLIC).setContentText(msg);
        return mBuilder;
    }

    public static void notify(Context context, NotificationCompat.Builder builder, @Nullable Integer id) {
        final NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = builder.build();
        //notification.flags |= Notification.FLAG_AUTO_CANCEL;
        if (id == null) {
            id = new Integer(1);
        }
        mNotificationManager.notify(id, notification);
    }

    public static void notify_user(Context context, String channelID, String title, String msg, @Nullable Integer id) {
        final NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = makeNotificationBuilder(context, channelID, title, msg);
        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        //notification.defaults |= Notification.DEFAULT_SOUND;

        Log.d("SVC", "notification sent " + msg);
        if (id == null) {
            id = new Integer(1);
        }
        mNotificationManager.notify(id, notification);
    }


}

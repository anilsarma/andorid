package com.smartdeviceny.njts;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;

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
    public static String formatPrintableTime(Date time, @Nullable  String format)  {
        SimpleDateFormat printFormat = new SimpleDateFormat(format ==null?"hh:mm a":format);
        return printFormat.format(time);
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
    public static boolean copyFileIfNewer(File src, File dest) {
        if( dest ==null) {
            return src!=null;
        }
        if( src.getAbsolutePath().equals(dest.getAbsolutePath())) {
            return true;
        }

        if( !src.exists()) {
            return false;
        }
        if( src.exists()) {
            if( src.lastModified() < dest.lastModified()) {
                if( src.length()  == dest.length()) {
                    return true;
                }
            }
        }
        // need to copy src to file.
        try {
            FileUtils.copyFile(src, dest);
            return true;
        } catch(Exception e) {
            try {
                dest.delete();
            } catch(Exception ee) {

            }
            e.printStackTrace();
            return false;
        }

    }
    public static boolean copyFileIfNewer(String src, String dest) {
        if( dest ==null) {
            return src!=null;
        }
        if( src.equals(dest)) {
            return true;
        }

        File srcFile = new File(src);
        File destFile = new File(dest);
        return copyFileIfNewer(srcFile, destFile);

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

//    public static boolean scheduleJob(Context context, JobID id,  @NonNull Class<?> cls, @Nullable Integer ms_frequency, boolean periodic, @Nullable PersistableBundle bundle) {
//        ComponentName serviceComponent = new ComponentName(context, cls);
//        JobInfo.Builder builder = new JobInfo.Builder(id.getID(), serviceComponent);
//        if (ms_frequency == null) {
//            ms_frequency = new Integer(60 * 1000); // every minute.
//        }
//        if (periodic) {
//            builder.setPeriodic(ms_frequency);
//        } else {
//            builder.setMinimumLatency(ms_frequency); // wait at least
//            builder.setOverrideDeadline((int) (ms_frequency)); // maximum delay
//        }
//       if(bundle !=null ) {
//           builder.setExtras(bundle);
//       }
//
//        //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
//        //builder.setRequiresDeviceIdle(true); // device should be idle
//        //builder.setRequiresCharging(true); // we don't care if the device is charging or not
//        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
//        if (jobScheduler.schedule(builder.build()) <= 0) {
//            Log.e("JOB", "error: Some error while scheduling the job");
//            return false;
//        } else {
//            // Log.d("JOB", "job scheduled " + ms_frequency);
//        }
//        return true;
//    }

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
        if (file != null && file.exists()) {
            try {
                if (file.getAbsoluteFile().delete()) {
                    Log.d("DEL", "deleted " + file.getAbsolutePath());
                } else {
                    Log.e("DEL", "delete failed " + file.getAbsolutePath());
                }
            } catch (Exception e) {
                Log.e("DEL", "delete failed " + file.getAbsolutePath() + " " + e);
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
        if (dir == null) {
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



//    private static void createNotificationChannel(Context context, String CHANNEL_ID) {
//        // Create the NotificationChannels, but only on API 26+ because
//        // the NotificationChannels class is new and not in the support library
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = context.getString(R.string.channel_name);
//            String description = context.getString(R.string.channel_description);
//            int importance = NotificationManager.IMPORTANCE_LOW; // control sound.
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
//            channel.setDescription(description);
//            // Register the channel with the system; you can't change the importance
//            // or other notification behaviors after this
//            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//            //Log.i("NOTIFY", "Channel created");
//        }
//    }
//    private static void createNotificationChannel(Context context, NotificationChannels channelID) {
//        // Create the NotificationChannels, but only on API 26+ because
//        // the NotificationChannels class is new and not in the support library
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(channelID.getUniqueID(), channelID.getName(), channelID.getImportance());
//            channel.setDescription(channelID.getDescription());
//            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }
//    }

//    private static NotificationCompat.Builder createNotificationGroup(Context context, String channelID, String group, String title, String msg) {
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelID);
//        mBuilder.setSmallIcon(R.mipmap.app_njs_icon);
//        mBuilder.setGroup(group);
//        mBuilder.setContentTitle(group).setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
//        mBuilder.setContentText(msg);
//        mBuilder.setGroupSummary(true);
//        return mBuilder;
//    }
//    private static NotificationCompat.Builder createNotificationGroup(Context context, NotificationGroup group, @Nullable String group_title) {
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, group.getChannel().getUniqueID());
//        mBuilder.setSmallIcon(R.mipmap.app_njs_icon);
//        mBuilder.setGroup(group.getUniqueID());
//
//        group_title=(group_title==null|| group_title.isEmpty())?group.getDescription():group_title;
//        mBuilder.setContentTitle(group_title);
//        mBuilder.setContentText(group_title);// API < 24
//
//        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
//
//        mBuilder.setGroupSummary(true);
//        return mBuilder;
//    }

//    public static NotificationCompat.Builder makeNotificationBuilder(Context context, String channelID, String group, String title, String msg) {
//        createNotificationChannel(context, channelID);
//        //int icon = R.mipmap.ic_launcher;
//        //long when = System.currentTimeMillis();
////        Notification notification = new Notification(icon, getString(R.string.app_name), when);
////        notification.flags |= Notification.FLAG_NO_CLEAR; //Do not clear the notification
////        notification.defaults |= Notification.DEFAULT_LIGHTS; // LED
////        notification.defaults |= Notification.DEFAULT_VIBRATE; //Vibration
////        notification.defaults |= Notification.DEFAULT_SOUND; // Sound
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelID);
//        mBuilder.setSmallIcon(R.mipmap.app_njs_icon);
//        mBuilder.setGroup(group);
//        mBuilder.setContentTitle(title).setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
//        mBuilder.setContentText(msg);
//        return mBuilder;
//    }

//    public static NotificationCompat.Builder makeNotificationBuilder(Context context, NotificationGroup group, String title, String msg) {
//
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, group.getChannel().getUniqueID());
//        mBuilder.setSmallIcon(R.mipmap.app_njs_icon);
//        mBuilder.setGroup(group.getUniqueID());
//        mBuilder.setContentTitle(title).setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
//        mBuilder.setContentText(msg);
//        return mBuilder;
//    }


//    public static void notify_user(Context context, String channelID, String group, String group_title, String title, String msg, @Nullable Integer id) {
//        final NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        NotificationCompat.Builder mBuilder = makeNotificationBuilder(context, channelID, group,  title, msg);
//        Notification notification = mBuilder.build();
//
//        notification.flags |= Notification.FLAG_AUTO_CANCEL;
//        notification.defaults |=Notification.DEFAULT_SOUND;
//
//        Log.d("SVC", "notification sent " + msg);
//        if (id == null) {
//            id = new Integer(1);
//        }
//        //mNotificationManager.notify(id, notification);
//        mNotificationManager.notify(1, createNotificationGroup(context, channelID, group, group_title, group_title).build());
//        mNotificationManager.notify(id, notification);
//    }
//    public static void notify_user_big_text(Context context, String channelID, String group, String group_title, String title, String msg, @Nullable Integer id) {
//        final NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        NotificationCompat.Builder mBuilder = makeNotificationBuilder(context, channelID, group,  title, msg);
//        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg));
//        Notification notification = mBuilder.build();
//
//
//        notification.flags |= Notification.FLAG_AUTO_CANCEL;
//        notification.defaults |=Notification.DEFAULT_SOUND;
//
//        Log.d("SVC", "notification sent " + msg);
//        if (id == null) {
//            id = new Integer(1);
//        }
//        //mNotificationManager.notify(id, notification);
//        mNotificationManager.notify(1, createNotificationGroup(context, channelID, group, group_title, group_title).build());
//        mNotificationManager.notify(id, notification);
//    }


//    public static void notify_user(Context context, NotificationGroup group, @Nullable String group_title,  String msg, @Nullable Integer id) {
//        createNotificationChannel(context, group.getChannel());
//
//        final NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        NotificationCompat.Builder mBuilder = makeNotificationBuilder(context, group,  group.getDescription(), msg);
//        mBuilder.setAutoCancel(true);
//        Notification notification = mBuilder.build();
//
//        notification.flags |= Notification.FLAG_AUTO_CANCEL;
//        notification.defaults |=Notification.DEFAULT_SOUND;
//
//        Log.d("SVC", "notification sent " + msg);
//        if (id == null) {
//            id = group.getID() + 1;
//        }
//        mNotificationManager.notify(group.getID(), createNotificationGroup(context, group, group_title).build());
//        mNotificationManager.notify(id, notification);
//    }
//
//    public static void notify_user_big_text(Context context, NotificationGroup group,@Nullable String group_title, String msg, @Nullable Integer id) {
//        createNotificationChannel(context, group.getChannel());
//
//        final NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        group_title = (group_title ==null || group_title.isEmpty())?group.getDescription():group_title;
//        NotificationCompat.Builder mBuilder = makeNotificationBuilder(context, group,  group_title, msg);
//        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(msg));
//        mBuilder.setAutoCancel(true);
//        Notification notification = mBuilder.build();
//
//
//        notification.flags |= Notification.FLAG_AUTO_CANCEL;
//        notification.defaults |=Notification.DEFAULT_SOUND;
//
//        Log.d("SVC", "notification sent " + msg);
//        if (id == null) {
//            id = group.getID() + 1;
//        }
//        //mNotificationManager.notify(id, notification);
//        mNotificationManager.notify(group.getID(), createNotificationGroup(context, group, group_title).build());
//        mNotificationManager.notify(id, notification);
//    }

    public static long alignTime(long polling_time) {
        Date now = new Date();
        long epoch_time = now.getTime();
        epoch_time += polling_time; // next polling time
        epoch_time = (epoch_time / polling_time) * polling_time; // to the next clock time.
        long diff = epoch_time - now.getTime();
        if(diff > polling_time) {
            diff = diff%polling_time;
//            if(diff < 1000) {
//                diff = polling_time;
//            }
        }
        return diff;
    }
    public static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {

        }
    }

    public static String encodeToString(File inputFile) throws  IOException {
        byte[] fileContent = FileUtils.readFileToByteArray(inputFile);
        return encodeToString(new String(fileContent));
    }

    public static String encodeToString(String inputFile) throws  IOException {
        byte[] fileContent = inputFile.getBytes();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            byte[] encodedString = java.util.Base64.getEncoder().encode(fileContent);
            return new String(encodedString);
        } else {
            String encodedString = Base64.encodeToString(fileContent, fileContent.length);
            return new String(encodedString);
        }


    }

    public static String decodeToString(File inputFile) throws  IOException {
        byte[] fileContent = FileUtils.readFileToByteArray(inputFile);
        return decodeToString(new String(fileContent));

    }
    public static String decodeToString(String inputFile) throws  IOException {
        byte[] fileContent = inputFile.getBytes();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            byte[] encodedString = java.util.Base64.getDecoder().decode(fileContent);
            return new String(encodedString);
        } else {
            byte[] encodedString = Base64.decode(fileContent, 0);
            return new String(encodedString);
        }
    }
}

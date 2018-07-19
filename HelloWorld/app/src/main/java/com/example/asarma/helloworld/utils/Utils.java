package com.example.asarma.helloworld.utils;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
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

    static public String formatToLocalDate(Date dt)
    {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();

        DateFormat dateFormat = new SimpleDateFormat("YYYY/MM/dd");
        dateFormat.setTimeZone(tz);

        return dateFormat.format(dt);
    }
    static public String formatToLocalTime(Date dt)
    {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();

        DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss a");
        dateFormat.setTimeZone(tz);

        return dateFormat.format(dt);
    }
    static Calendar make_cal(Date date, int field, int ammount)
    {
        Calendar ca= Calendar.getInstance();
        ca.setTime(date);
        ca.add(field, ammount);

        return ca;
    }
    static public String formatToLocalDateTime(Date dt)
    {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();

        DateFormat dateFormat = new SimpleDateFormat("YYYYMMdd HH:mm:ss");
        dateFormat.setTimeZone(tz);

        return dateFormat.format(dt);
    }

    static public String getLocaDateTime()
    {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();

        DateFormat dateFormat = new SimpleDateFormat("YYYYMMdd HH:mm:ss.SSS z");
        dateFormat.setTimeZone(tz);
        Date date = new Date();
        return dateFormat.format(date);
    }
    static public Date adddays(Date date, int days) {
        if (days > 0 ) {
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.add(Calendar.DATE, days);
            date = c.getTime();
        }
        return date;
    }
    static public String getLocaDate(int days)
    {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();

        DateFormat dateFormat = new SimpleDateFormat("YYYYMMdd");
        dateFormat.setTimeZone(tz);
        Date date = new Date();
        if (days > 0 ) {
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
        }
        catch (Exception e) {
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
        return result;
    }
    public static float pxFromDp(float dp, Context mContext) {
        return dp * mContext.getResources().getDisplayMetrics().density;
    }
    public static int pxFromSp(int sp, Context mContext) {
        return (int)((double)sp * mContext.getResources().getDisplayMetrics().scaledDensity);
    }


    public static TextView addTextView(Context context, ViewGroup parent, String text, int font_size, int padding)
    {
        TextView tv = new TextView(context);
        tv.setText( text);
        tv.setTextSize(Utils.pxFromDp(font_size, context));
        TableRow.LayoutParams params =  new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        //params.setMargins(0, 5, 0, 5);
        parent.addView(tv, params);
        tv.setPadding(10, padding, 0, 0);
        return  tv;
    }

    public static String capitalize(String str)
    {
        String strs[] = str.split("\\s");
        StringBuffer b = new StringBuffer();

        for (int i = 0; i < strs.length; i++) {
            if ( strs[i].length() > 1 && strs[i].substring(1,1)!= "." )
            {
                strs[i] = strs[i].substring(0,1).toUpperCase() + strs[i].substring(1).toLowerCase();
            }
            b.append(strs[i] + " ");
        }
        return b.toString().trim();
    }
    public static String join(String delimit, String data[] )
    {
        String str = "";
        for (int i = 0; i < data.length; i++) {
            if (!str.isEmpty()) {
                str += delimit;
            }
            str += data[i];
        }
        return str;
    }
    public static ArrayList<String> split(String delimit, String data )
    {
        ArrayList<String> favA = new ArrayList<String>();
        for (String x: data.split(delimit)) {
            favA.add(x);
        }
        return favA;
    }

    public static ArrayList<HashMap<String, String>> coerce(ArrayList<HashMap<String, Object>> data )
    {
        ArrayList<HashMap<String, String>> result  = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            HashMap<String, String> r = new HashMap<>();
            for ( String key:data.get(i).keySet()) {
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
        while ((length = in.read(buffer))>0){
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

    public static String convertStreamToString(InputStream is) {
        return new Scanner(is).useDelimiter("\\A").next();
    }
}

package com.smartdeviceny.njts.utils;


import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by asarma on 11/2/2017.
 */

public class SqlUtils {

    public static void update_user_pref(SQLiteDatabase db, String name, String value, Date dt) {
        ArrayList<HashMap<String, Object>> data = new ArrayList<>();
        HashMap<String, Object> rec = new HashMap<>();
        rec.put("name", name);
        rec.put("value", value);
        rec.put("date", Utils.formatToLocalDateTime(dt));
        data.add(rec);
        query(db, "delete from user_pref where name like '%" + name + "%'");
        insert(db, "user_pref", data);
    }

    public static String get_user_pref_value(SQLiteDatabase db, String name, String default_value) {
        try {
            ArrayList<HashMap<String, Object>> data = query(db, "select * from user_pref where name like '%" + name + "%'");
            if (data.isEmpty()) {
                return default_value;
                //return null;
            }
            return (String) data.get(0).get("value");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return default_value;
    }

    public static void insert(SQLiteDatabase db, String table_name, ArrayList<HashMap<String, Object>> df) {

        if (df.isEmpty()) {
            return;
        }
        String cols = "";
        ArrayList<String> columns = new ArrayList<String>();
        String values = "";
        for (Iterator<String> iterator = df.get(0).keySet().iterator(); iterator.hasNext(); ) {
            String key = iterator.next();
            if (!cols.isEmpty()) {
                cols += ",";
                values += ",";
            }
            cols += key;
            values += "?";
            columns.add(key);
        }
        String sql = "INSERT INTO " + table_name + "(" + cols + ") VALUES(" + values + ")";
        //String sql_truncate = "db.execSQL("delete from "+ TABLE_NAME);
        // System.out.println("insert begin " + table_name);
        try {

            SQLiteStatement pstmt = db.compileStatement(sql);
            //conn.setAutoCommit(false);
            int count = 0;
            db.beginTransaction();
            for (Iterator<HashMap<String, Object>> iterator = df.iterator(); iterator.hasNext(); ) {
                HashMap<String, Object> data = iterator.next();
                for (int i = 0; i < columns.size(); i++) {
                    String key = columns.get(i);
                    Object v = data.get(key);
                    //System.err.println("Data -" +  key + " " + v + " type=" + v.getClass());
                    if (v instanceof Integer) {
                        pstmt.bindLong(i + 1, (Integer) v);
                    } else if (v instanceof Double) {
                        pstmt.bindDouble(i + 1, (Double) v);
                    } else {
                        pstmt.bindString(i + 1, (String) v);
                    }
                }
                pstmt.executeInsert();
                pstmt.clearBindings();
            }
            db.setTransactionSuccessful();
            //System.out.println("insert end " + table_name);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    static public int check_table(SQLiteDatabase db, String table) {
        String sql = "select count(*) from " + table;
        Cursor c = db.rawQuery(sql, null);
        return Utils.parseCursor(c).size();
    }

    static public String getStationCode(SQLiteDatabase db, String name) {
        try {
            String sql = "select station_code from station_codes where station_name like '%" + name + "%'";
            Cursor c = db.rawQuery(sql, null);
            ArrayList<HashMap<String, Object>> result = Utils.parseCursor(c);
            if (result.isEmpty()) {
                return "";
            }
            return result.get(0).get("station_code").toString();
        } catch (Exception e) {
            e.printStackTrace();
            ;
        }
        return "";
    }


    static public ArrayList<HashMap<String, Object>> query(SQLiteDatabase db, String sql) {
        //String sql = "select * from routes";
        ArrayList<HashMap<String, Object>> curser = Utils.parseCursor(db.rawQuery(sql, null));
        return curser;
    }


    static public void create_user_pref_table(SQLiteDatabase db) {
        String sql_string = "create table IF NOT EXISTS user_pref( name text primary key, value text, date real )";
        db.execSQL(sql_string);
    }

    static public boolean check_if_table_exists(SQLiteDatabase db, String name) {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + name + "';";
        ArrayList<HashMap<String, Object>> data = query(db, sql);
        if (data.isEmpty()) {
            return false;
        }
        return true;
    }

    static public boolean check_if_user_pref_exists(SQLiteDatabase db) {
        return check_if_table_exists(db, "user_pref");
    }


}
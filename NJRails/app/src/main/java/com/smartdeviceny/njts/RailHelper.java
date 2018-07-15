package com.smartdeviceny.njts;


import android.database.sqlite.SQLiteDatabase;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by asarma on 11/2/2017.
 */


public class RailHelper {

    public static void create_tables(SQLiteDatabase db) {
        String sql[] = {
                "create table IF NOT EXISTS stops( stop_id integer  primary key, stop_code integer, stop_name text, stop_desc text, stop_lat REAL, stop_lon REAL, zone_id integer)",
                "create table IF NOT EXISTS stop_times( trip_id integer, arrival_time NUMERIC, departure_time NUMERIC, stop_id integer, stop_sequence integer, pickup_type integer, drop_off_type integer,shape_dist_traveled real, primary key(trip_id, stop_id) )",
                "create table IF NOT EXISTS trips( route_id integer, service_id integer, trip_id integer primary key, trip_headsign text, direction_id integer, block_id integer, shape_id integer )",
                "create table IF NOT EXISTS shapes( shape_id integer, shape_pt_lat real, shape_pt_lon real, shape_pt_sequence integer, shape_dist_traveled real, PRIMARY key (shape_id, shape_pt_sequence) )",
                "create table IF NOT EXISTS routes( route_id integer primary key, agency_id text, route_short_name text, route_long_name text, route_type integer, route_url text, route_color text )",
                "create table IF NOT EXISTS calendar_dates( service_id integer, date integer, exception_type integer, PRIMARY KEY (date, service_id) )",

                "create table IF NOT EXISTS user_pref( name text primary key, value text, date real )",
                "create table IF NOT EXISTS station_codes( station_name text primary key, station_code text )",
                "create table IF NOT EXISTS checksum( filename text primary key, checksum text )",
        };
        String tables[] = {"trips", "stops", "routes", "calendar_dates", "stop_times" /*, "shapes" */};
        for (int i = 0; i < tables.length; i++) {
            try {
                if (false) db.execSQL("DROP TABLE  " + tables[i]);
            } catch (Exception e) {
                e.printStackTrace();
                ;
            }
        }

        for (int i = 0; i < sql.length; i++) {
            db.execSQL(sql[i]);
        }
    }


}

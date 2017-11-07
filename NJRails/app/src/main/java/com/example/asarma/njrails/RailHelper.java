package com.example.asarma.njrails;


import android.database.sqlite.SQLiteDatabase;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
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


    public static Connection connect() {
        Connection conn = null;

        return conn;
    }

    public static String make_sql_arg(Object v)
    {
        if ( v instanceof  String) {
            return "'" + v + "'";
        }
        return v.toString();
    }
    public static ArrayList<HashMap<String, Object>> make_result(ResultSet rs)
    {
        try {
            ResultSetMetaData metadata = rs.getMetaData();
            ArrayList<String> columns = new ArrayList<String>();
            int columnCount = metadata.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                //System.out.println(metadata.getColumnName(i) + ", ");
                columns.add(metadata.getColumnName(i));
            }
            ArrayList<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
            while (rs.next()) {
                //System.out.println(rs);
                HashMap<String, Object> row = new HashMap<String, Object>();
                for (int i = 0; i <  columns.size(); i++) {
                    String fld = columns.get(i);
                    Object v = rs.getObject(fld);
                    //System.out.println(fld +"=" + v);
                    row.put(fld, rs.getObject(fld));
                }
                result.add(row);
            }
            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static ResultSet _query_sql(Connection conn, String table_name, HashMap<String, Object> cond, String orderby)
    {
        String condition = "";
        for(Iterator<String> iter = cond.keySet().iterator(); iter.hasNext();  ) {
            String key = iter.next();
            Object value = cond.get(key);
            if (condition.length() == 0 ) {
                condition = " where ";
            }
            else {
                condition += " and ";
            }

            if ( value instanceof List<?>) {
                String vals = "";
                //List<String>  vals = new ArrayList<String>();
                for( Iterator<?> iter2 = ((List) value).iterator(); iter2.hasNext(); ) {
                    Object v = iter2.next();
                    if ( vals.length() == 0 ) {
                        vals = make_sql_arg(v);
                    }
                    else {
                        vals += ", " + make_sql_arg(v);
                    }
                }
                condition += key + " in (" + vals + ")";
            }
            else
            {
                Operation oper = (Operation) value;
                condition += key + " " + oper.operation + " " + make_sql_arg(oper.value);
            }
        }
        String sql = "select * from " + table_name + condition + " "  + orderby;
        //System.out.println(sql);
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            return rs;
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean is_float(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }
    public static boolean is_int(String str)
    {
        try
        {
            int d = Integer.parseInt(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    public static ArrayList<HashMap<String, Object>> read_csv(String filename )
    {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        String cols[] = null;
        ArrayList<HashMap<String, Object>> result = new   ArrayList<HashMap<String, Object>>();
        try {

            br = new BufferedReader(new FileReader(filename));
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] tokens = line.split(cvsSplitBy);
                if (cols == null ) {
                    cols = tokens;
                    continue;
                }
                HashMap<String, Object> data=new HashMap<String, Object>();
                for (int i = 0; i < tokens.length; i++) {
                    Object v = tokens[i];
                    try {
                        v = Integer.parseInt(tokens[i] );
                    }
                    catch (NumberFormatException e) {
                        try {
                            v = Double.parseDouble(tokens[i]);
                        }
                        catch (NumberFormatException e1)
                        {

                        }
                    }
                    //System.out.println(v + " type=" + v.getClass());
                    data.put( cols[i], v);
                }
                result.add( data );

                //System.out.println("Country [code= " + country[4] + " , name=" + country[5] + "]");

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }


    static public ResultSet  execute(Connection conn, String sql)
    {
        try {
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(sql);
            //return rs;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static  void create_tables(Connection conn)
    {
        String sql[] = {
                "create table IF NOT EXISTS stops( stop_id integer  primary key, stop_code integer, stop_name text, stop_desc text, stop_lat REAL, stop_lon REAL, zone_id integer)",
                "create table IF NOT EXISTS stop_times( trip_id integer, arrival_time NUMERIC, departure_time NUMERIC, stop_id integer, stop_sequence integer, pickup_type integer, drop_off_type integer,shape_dist_traveled real, primary key(trip_id, stop_id) )",
                "create table IF NOT EXISTS trips( route_id integer, service_id integer, trip_id integer primary key, trip_headsign text, direction_id integer, block_id integer, shape_id integer )",
                "create table IF NOT EXISTS shapes( shape_id integer, shape_pt_lat real, shape_pt_lon real, shape_pt_sequence integer, shape_dist_traveled real, PRIMARY key (shape_id, shape_pt_sequence) )",
                "create table IF NOT EXISTS routes( route_id integer primary key, agency_id text, route_short_name text, route_long_name text, route_type integer, route_url text, route_color text )",
                "create table IF NOT EXISTS calendar_dates( service_id integer, date integer, exception_type integer, PRIMARY KEY (date, service_id) )"
        };
        for (int i = 0; i < sql.length; i++) {
            execute(conn, sql[i]);
        }
    }
    public static  void create_tables(SQLiteDatabase db)
    {
        String sql[] = {
                "create table IF NOT EXISTS stops( stop_id integer  primary key, stop_code integer, stop_name text, stop_desc text, stop_lat REAL, stop_lon REAL, zone_id integer)",
                "create table IF NOT EXISTS stop_times( trip_id integer, arrival_time NUMERIC, departure_time NUMERIC, stop_id integer, stop_sequence integer, pickup_type integer, drop_off_type integer,shape_dist_traveled real, primary key(trip_id, stop_id) )",
                "create table IF NOT EXISTS trips( route_id integer, service_id integer, trip_id integer primary key, trip_headsign text, direction_id integer, block_id integer, shape_id integer )",
                "create table IF NOT EXISTS shapes( shape_id integer, shape_pt_lat real, shape_pt_lon real, shape_pt_sequence integer, shape_dist_traveled real, PRIMARY key (shape_id, shape_pt_sequence) )",
                "create table IF NOT EXISTS routes( route_id integer primary key, agency_id text, route_short_name text, route_long_name text, route_type integer, route_url text, route_color text )",
                "create table IF NOT EXISTS calendar_dates( service_id integer, date integer, exception_type integer, PRIMARY KEY (date, service_id) )",
                "create table IF NOT EXISTS user_pref( name text primary key, value text, date real )",
                "create table IF NOT EXISTS station_codes( station_name text primary key, station_code text )"
        };
        String tables[] = {"trips", "stops", "routes", "calendar_dates", "stop_times", "shapes"};
        for (int i = 0; i < tables.length; i++) {
            try {
               if ( false ) db.execSQL("DROP TABLE  " + tables[i]);
            } catch (Exception e) {
                e.printStackTrace();;
            }
        }

        for (int i = 0; i < sql.length; i++) {
           db.execSQL(sql[i]);
        }
    }

    public static void insert(Connection conn, String table_name,  ArrayList<HashMap<String, Object>> df )
    {
        if (df.isEmpty()) {
            return;
        }
        String cols = "";
        ArrayList<String> columns = new ArrayList<String>();
        String values = "";
        for(Iterator<String> iterator = df.get(0).keySet().iterator(); iterator.hasNext(); ) {
            String key =  iterator.next();
            if ( !cols.isEmpty()) {
                cols +=",";
                values +=",";
            }
            cols += key;
            values += "?";
            columns.add(key);
        }
        String sql = "INSERT INTO " + table_name  + "(" + cols + ") VALUES(" + values + ")";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            conn.setAutoCommit(false);
            int count = 0;
            for(Iterator<HashMap<String, Object>> iterator = df.iterator(); iterator.hasNext(); ) {
                HashMap<String, Object> data = iterator.next();
                for (int i = 0; i < columns.size(); i++) {
                    String key = columns.get(i);
                    Object v = data.get(key);
                    //System.err.println("Data -" +  key + " " + v + " type=" + v.getClass());
                    if ( v instanceof  Integer) {
                        pstmt.setInt(i+1, (Integer)v);
                    } else if ( v instanceof  Double) {
                        pstmt.setDouble(i+1, (Double)v);
                    } else {
                        pstmt.setString(i+1, (String)v);
                    }
                }
                pstmt.addBatch();
                count ++;
                if (count >= 2000) {
                    System.out.println("Commited "  + pstmt.executeBatch() );
                    //pstmt = conn.prepareStatement(sql);
                    count = 0;
                }

            }
            if (count > 0 ) {
                System.out.println("Commited " + pstmt.executeBatch());
            }
            pstmt.close();

            conn.commit();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public static int get_stop_id(Connection conn, String stop_name )
    {
        HashMap<String, Object> cond = new HashMap<String, Object>();
        cond.put("stop_name", new Operation("like", "%" + stop_name + "%"));
        int stop_stop_id = (Integer) make_result(_query_sql(conn, "stops", cond, "")).get(0).get("stop_id");
        return stop_stop_id;
    }


    public static void test_main(String[] args) {
        // write your code here
        System.out.println("hello world");
        Connection conn = connect();

        if (false) {
            create_tables(conn);
            System.out.println("done");

            String tables[] = {"trips", "stops", "routes", "calendar_dates", "stop_times", "shapes"};
            for (int i = 0; i < tables.length; i++) {
                System.out.println("workiing " + tables[i]);
                ArrayList<HashMap<String, Object>> df = read_csv("e:/data/rail/" + tables[i] + ".txt");
                insert(conn, tables[i], df);
                System.out.println("done " + tables[i]);
            }
            ArrayList<HashMap<String, Object>> df = read_csv("e:/data/rail/stops.txt");
            for (Iterator<HashMap<String, Object>> iterator = df.iterator(); iterator.hasNext(); ) {
                HashMap<String, Object> data = iterator.next();
                System.out.println(data);
            }
            //insert(conn, "stops", df);
            System.exit(0);
        }

        HashMap<String, Object> cond = new HashMap<String, Object>();
        RailHelper alloc = new RailHelper();
        cond.put("stop_name", new Operation("like", "%New York%"));
        //cond.put("route_id", new Operation("=", "name"));
        //ArrayList<Object> services = new ArrayList<Object>();
        //services.add(12);
        //services.add(13);
        //services.add("hell");
        //cond.put("homeid", services);


        ResultSet rs = _query_sql(conn, "stops", cond, "");
        ArrayList<HashMap<String, Object>> result = make_result(rs);
        int start_stop_id = get_stop_id( conn,"%new york%");
        int stop_stop_id =  get_stop_id(conn, "%New Brunswick%");
        String travel_time = "06:00:00";
        int date = 20171029;

        cond = new HashMap<String, Object>();
        cond.put("route_long_name", new Operation("like", "%Corridor%"));
        int route_id = (Integer) make_result(_query_sql(conn, "routes", cond, "")).get(0).get("route_id");

        System.out.println(" route=" + route_id);
        //sid = [x['service_id'] for x in get_cal(db, 20171031)]
        cond = new HashMap<String, Object>();
        cond.put("date", new Operation("=", date));
        ArrayList<HashMap<String, Object>> cals = make_result(_query_sql(conn, "calendar_dates", cond, ""));
        ArrayList<Integer> serviceids = new ArrayList<Integer>();
        for (int i = 0; i < cals.size(); i++) {
            serviceids.add((Integer) cals.get(i).get("service_id"));
        }
        cond = new HashMap<String, Object>();
        cond.put("service_id", serviceids);
        cond.put("route_id", new Operation("=", route_id));
        ArrayList<HashMap<String, Object>> trips = make_result(_query_sql(conn, "trips", cond, ""));

        ArrayList<Integer> trip_ids = new ArrayList<Integer>();
        for (int i = 0; i < trips.size(); i++) {
            //System.out.println(trips.get(i));
            trip_ids.add((Integer) trips.get(i).get("trip_id"));
        }

        cond = new HashMap<String, Object>();
        cond.put("trip_id", trip_ids);
        cond.put("stop_id", new Operation("=", start_stop_id));
        cond.put("departure_time", new Operation(">", travel_time));
        ArrayList<HashMap<String, Object>> stops = make_result(_query_sql(conn, "stop_times", cond, "order by departure_time"));
        for (int i = 0; i < stops.size(); i++) {
            // System.out.println(stops.get(i));
            //_get_sql_query( db, 'stop_times', { 'trip_id':x['trip_id'], 'stop_id':end_stop_id, 'arrival_time': ( '>', str(x['departure_time']))})
            cond = new HashMap<String, Object>();
            cond.put("trip_id", new Operation("=", stops.get(i).get("trip_id")));
            //System.out.println("stopid = " + stop_stop_id);
            cond.put("stop_id", new Operation("=", stop_stop_id));
            // cond.put("arrival_time", new Operation(">",  (String)stops.get(i).get("departure_time")));
            ArrayList<HashMap<String, Object>> valid = make_result(_query_sql(conn, "stop_times", cond, ""));
            for (int j = 0; j < valid.size(); j++) {
                //  System.out.println(valid.get(j));
            }
            //trip_ids.add( (Integer)trips.get(i).get("trip_id"));
        }

        //String sql = "select * from stop_times where stop_id in ( select stop_id from stops where stop_name like '%new york%' or stop_name like '%new brunswick%'  and trip_id in ( select trip_id from trips where route_id in ( select route_id from routes where route_long_name like '%Northeas%') and service_id in ( select service_id from calendar_dates where date = '20171102') )  )and stop_id in ( select stop_id from stops where stop_name like '%new york%' or stop_name like '%new brunswick%' ) and departure_time > '18:10:00' and departure_time < '20:00:00' and  trip_id in ( select st.trip_id from stop_times st, stop_times st2 where st.trip_id = st2.trip_id and st.stop_id = 105 and st.arrival_time < st2.arrival_time and st2.stop_id=103)and trip_id in ( select trip_id from trips where route_id in ( select route_id from routes where route_long_name like '%Northeas%') and service_id in ( select service_id from calendar_dates where date = '20171102') )  order by departure_time;";
        //String sql = "select * from stop_times where stop_id in ( select stop_id from stops where stop_name like '%new york%' or stop_name like '%new brunswick%'  and trip_id in ( select trip_id from trips where service_id in ( select service_id from calendar_dates where date = '20171102') )  )and stop_id in ( select stop_id from stops where stop_name like '%new york%' or stop_name like '%new brunswick%' ) and departure_time > '18:10:00' and departure_time < '20:00:00' and  trip_id in ( select st.trip_id from stop_times st, stop_times st2 where st.trip_id = st2.trip_id and st.stop_id = 105 and st.arrival_time < st2.arrival_time and st2.stop_id=103)and trip_id in ( select trip_id from trips where route_id in ( select route_id from routes where route_long_name like '%Northeas%') and service_id in ( select service_id from calendar_dates where date = '20171102') )  order by departure_time;";
        // all trains that pass through the two stations.
        //String sql = "select * from stop_times where stop_id in (  {start_station}) and departure_time > '18:10:00' and departure_time < '20:00:00' and  trip_id in ( select st.trip_id from stop_times st, stop_times st2 where st.trip_id = st2.trip_id and st.stop_id = {start_station} and st.arrival_time < st2.arrival_time and st2.stop_id={stop_station})and trip_id in ( select trip_id from trips where service_id in ( select service_id from calendar_dates where date = '{travel_date}') )  order by departure_time;";


        // station query.
        String sql_start = "select * from stop_times st, trips t where stop_id in (  {start_station})  and  st.trip_id in ( select st.trip_id from stop_times st, stop_times st2 where st.trip_id = st2.trip_id and st.stop_id = {start_station} and st.arrival_time < st2.arrival_time and st2.stop_id={stop_station})and st.trip_id in ( select trip_id from trips where service_id in ( select service_id from calendar_dates where date = '{travel_date}') ) and st.trip_id = t.trip_id order by departure_time";
        // for travel time.
        String sql_destination = "select trip_id, shape_dist_traveled, arrival_time as destination_time, stop_id as destination_stop from (select * from stop_times where trip_id in (select st.trip_id from stop_times st, trips t where stop_id in (  {start_station})  and  st.trip_id in ( select st.trip_id from stop_times st, stop_times st2 where st.trip_id = st2.trip_id and st.stop_id = {start_station} and st.arrival_time < st2.arrival_time and st2.stop_id={stop_station})and st.trip_id in ( select trip_id from trips where service_id in ( select service_id from calendar_dates where date = '{travel_date}') ) and st.trip_id = t.trip_id order by departure_time) order by stop_sequence asc) and stop_id={stop_station} group by trip_id";

        String sql = "select * from (" + sql_start + ") as st, (" + sql_destination + ") as dt where dt.trip_id = st.trip_id";

        // all stops in a line.
        // select * from stop_times where trip_id = 2547 order by stop_sequence;

        sql = sql.replace("{start_station}", "" + start_stop_id);
        sql = sql.replace("{stop_station}", "" + stop_stop_id);
        sql = sql.replace("{travel_date}", "" + date);
        System.out.println(sql);
        ArrayList<HashMap<String, Object>>
                nstops = make_result(execute(conn, sql));
        for (int i = 0; i < nstops.size(); i++) {
            System.out.println("new sql " + nstops.get(i));
            //trip_ids.add( (Integer)trips.get(i).get("trip_id"));
        }
    }
}

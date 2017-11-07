package com.example.asarma.njrails;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class RouteActivity extends FragmentActivity {
    SQLHelper dbHelper=null;

    protected void onCreate(Bundle savedInstanceState) {
        dbHelper = new SQLHelper(this);

        super.onCreate(savedInstanceState);
        SQLiteDatabase db= dbHelper.getWritableDatabase();

        try {
            if ( SQLHelper.check_table(db, "trips")== 0 ) {
                Toast.makeText(this.getBaseContext(),(String)"updating Database Tables",
                        Toast.LENGTH_SHORT).show();
                RailHelper.create_tables(db);
                dbHelper.update_tables(db);
            }
        }catch (Exception e) {
            Toast.makeText(this.getBaseContext(),(String)"Creating Database Tables",
                    Toast.LENGTH_SHORT).show();
            RailHelper.create_tables(db);
            dbHelper.update_tables(db);
        }

        setContentView(R.layout.route_layout);
        if ( false ) {

          //  SQLiteDatabase db= dbHelper.getWritableDatabase();
            RailHelper.create_tables(db);
            dbHelper.update_tables(db);
        }

        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();

        DateFormat dateFormat = new SimpleDateFormat("YYYYMMdd");
        dateFormat.setTimeZone(tz);
        Date date = new Date();
        int dt = Integer.parseInt(dateFormat.format(date));
        updateRoutes(Utils.parseCursor(SQLHelper.getRoutes(db, "New York", "New Brunswick", dt)));
    }

    TextView addTextView(Context context, ViewGroup parent, String text, int font_size, int padding)
    {
        TextView tv = new TextView(context);
        tv.setText( text);
        tv.setTextSize(Utils.pxFromDp(font_size, this));
        TableRow.LayoutParams params =  new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 5, 0, 5);
        parent.addView(tv, params);
        tv.setPadding(0, padding, 0, 0);
        return  tv;
    }
    void updateRoutes(ArrayList<HashMap<String, Object>> routes)
    {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        dateFormat.setTimeZone(tz);
        SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm:ss");
timeformat.setTimeZone(tz);

        TextView route_header = (TextView)findViewById(R.id.route_header);
        //route_header.setBackgroundColor(Color.BLACK);
        route_header.setTextColor(Color.WHITE);

        route_header.setText( dateFormat.format(date));
        route_header.setTextSize(Utils.pxFromDp(6, this));
        route_header.setTextColor(Color.BLUE);
        //setContentView(route_header);
        TableLayout tl = (TableLayout)findViewById(R.id.routes);
        for(int i=0;i< routes.size();i ++) {
            HashMap<String, Object> data = routes.get(i);
            // departure_time, destnaton arrival time block_id, route_long namem duration
            final String departture_time = data.get("departure_time").toString();
            String destination_time = data.get("destination_time").toString();
            final String block_id = data.get("block_id").toString();
            final String route_name = data.get("route_long_name").toString();
            if ( i == 0 ) {
                route_header.setText(route_name + dateFormat.format(date));
            }
            String time = "";
            try {
                Date st_time = timeformat.parse(departture_time);
                Date end_time = timeformat.parse(destination_time);
                long milli = end_time.getTime() - st_time.getTime();
                long minutes = milli/(60*1000);
                time = "" + new Long(minutes).toString() ;
            } catch (Exception e) {

            }
            TableLayout tl2 = new TableLayout(this);
            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 5, 0, 5);


            tl2.setLayoutParams(params);

            TableRow tr = new TableRow(this);
            tr.setBackgroundColor(Color.LTGRAY);
            TableRow.LayoutParams params0 = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            params0.setMargins(5, 5,5,5);

            tr.setLayoutParams(params0);
            TextView tv = new TextView(this);
            tv.setText( block_id + " " + departture_time + " " + destination_time);
            tv.setTextSize(Utils.pxFromDp(6, this));
            tr.addView(tv);

            TableRow tr2 = new TableRow(this);
            TableRow.LayoutParams params2 = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            params2.setMargins(0, 20, 0, 20);
            tr2.setLayoutParams(params2);

           // tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            tl2.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            tl2.addView(tr2, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            //tl2.addView(tr3, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            //addTextView(this, tl2, "" + block_id + " " + departture_time + " " + destination_time, 6, 10);
            addTextView(this, tl2, "" + route_name + "#" + block_id, 5, 10);
            TextView th = addTextView(this, tl2, "" + time+ " minutes", 5, 150);
            //addTextView(this, tl2, "" , 5, 5);
            //tl2.setOnClickListener( new TableRowListener(th, block_id + " "+ route_name + " " + departture_time));
            tl2.setOnTouchListener( new RouteTouchListener(th, block_id + " "+ route_name + " " + departture_time));

            tl.addView(tl2, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));


            for ( String key:data.keySet()
                 ) {
                Object value = data.get(key);
                System.out.println("key=" + key + "=" + value);
            }
        }
    }

    void buildTable()
    {
        TableLayout tl = (TableLayout)findViewById(R.id.menu);
/* Create a new row to be added. */
        TableRow tr = new TableRow(this);
        tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
/* Create a Button to be the row-content. */

        TextView tv = new TextView(this);
        tv.setText("Some text");
        tr.addView(tv);
        tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        Button b = new Button(this);
        b.setText("Dynamic Button");
        b.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
/* Add Button to row. */
        tr.addView(b);
        final Context context = this;

        /*
        tv.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                TableLayout tl = (TableLayout)findViewById(R.id.menu);

                TableRow tr = new TableRow(v.getContext());
                tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));


                TextView tv = new TextView(v.getContext());
                tv.setText("Some text");
                tr.addView(tv);
                tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                Button b = new Button(v.getContext());
                b.setText("Dynamic Button");
                b.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                tr.addView(b);
                b.setOnClickListener( new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                    }
                });

                //tr.setBackgroundResource(R.drawable.sf_gradient_03);
                tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            }
        });
        */
/* Add row to TableLayout. */
//tr.setBackgroundResource(R.drawable.sf_gradient_03);
        tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

    }

    public void showDialog(final String phone) throws Exception
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(RouteActivity.this);
        builder.setMessage("Ring: " + phone);

        builder.setPositiveButton("Ring", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);// (Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phone));
                //startActivity(callIntent);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Avbryt", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        builder.show();
    }
}
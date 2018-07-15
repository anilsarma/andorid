package com.smartdeviceny.njts;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.smartdeviceny.njts.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * Created by asarma on 11/3/2017.
 */

public class TrainStatusUpdate {

    int multiplier = 3;
    public void updateRoutes(View rootView, ArrayList<HashMap<String, Object>> routes)
    {
        if( routes == null ){
            Toast.makeText(rootView.getContext(), "Unable to get train status ", Toast.LENGTH_LONG).show();
        }
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        dateFormat.setTimeZone(tz);
        SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm:ss");
        timeformat.setTimeZone(tz);

        TextView route_header = (TextView)rootView.findViewById(R.id.route_header);
        //route_header.setBackgroundColor(Color.BLACK);
        route_header.setTextColor(Color.WHITE);

        route_header.setText( dateFormat.format(date));
        route_header.setTextSize(Utils.pxFromDp(5, rootView.getContext()));
        route_header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 5*multiplier);
        // route_header.setTextColor(Color.BLUE);
        //setContentView(route_header);
        TableLayout tl = (TableLayout)rootView.findViewById(R.id.routes);
        tl.removeAllViews();
        if (routes.size() < 3) {
            return;
        }
        for(int i=0;i< routes.size();i ++) {
            HashMap<String, Object> data = routes.get(i);
            // departure_time, destnaton arrival time block_id, route_long namem duration
            final String time = data.get("time").toString();
            String destination = data.get("to").toString().replaceAll("&nbsp;", " ").replaceAll("<i>", "").replaceAll("</i>", "");
            String track = data.get("track").toString();
            String line = data.get("line").toString();
            final String train = data.get("train").toString();
            final String status = data.get("status").toString();
            final String station = data.get("station").toString();
            if ( i == 0 ) {
                route_header.setText("Train Status at " + station + " " + dateFormat.format(date));
            }

            TableLayout tl2 = new TableLayout(rootView.getContext());
            TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 5, 0, 5);


            tl2.setLayoutParams(params);

            TableRow tr = new TableRow(rootView.getContext());
            tr.setBackgroundColor(Color.LTGRAY);
            TableRow.LayoutParams params0 = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            params0.setMargins(5, 5,5,5);

            tr.setLayoutParams(params0);
            TextView tv = new TextView(rootView.getContext());
            tv.setText( line + "#" + train + " " + time  + " " + status);
            if (status.toLowerCase().contains("delay")) {
                tr.setBackgroundColor(Color.RED);
            } else if (status.toLowerCase().contains("board")) {
                tr.setBackgroundColor(Color.GREEN);
            }
            else if (status.toLowerCase().contains("stand")) {
                tr.setBackgroundColor(Color.parseColor("#F57C00"));
            }
            else if (status.toLowerCase().contains("cancel")) {
                tr.setBackgroundColor(Color.parseColor("#FF3D00"));
            } else if ( !track.isEmpty()) {
                tr.setBackgroundColor(Color.YELLOW);
            }
            tv.setTextSize(Utils.pxFromDp(6, rootView.getContext()));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 6*multiplier);
            tr.addView(tv);

            TableRow tr2 = new TableRow(rootView.getContext());
            TableRow.LayoutParams params2 = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            params2.setMargins(0, 20, 0, 20);
            tr2.setLayoutParams(params2);

            // tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            tl2.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            tl2.addView(tr2, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            //tl2.addView(tr3, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            //addTextView(this, tl2, "" + block_id + " " + departture_time + " " + destination_time, 6, 10);
            Utils.addTextView(rootView.getContext(), tl2, "Destination " + destination, 6, 10);
            Utils.addTextView(rootView.getContext(), tl2, "" + line + "#" + train, 6, 10);



            TextView th = Utils.addTextView(rootView.getContext(), tl2, "track " + track + " ", 5, 10);
            //addTextView(this, tl2, "" , 5, 5);
            tl2.setOnClickListener( new TableRowListener(th, train + " "+ line + " " + time));
            tl.addView(tl2, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        }
    }
}

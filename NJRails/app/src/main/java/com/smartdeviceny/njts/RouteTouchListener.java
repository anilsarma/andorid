package com.smartdeviceny.njts;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.smartdeviceny.njts.route.RoutePagerActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;



public class RouteTouchListener implements View.OnTouchListener {

    final String text;
    String train;
    boolean toggle = true;
    SQLHelper dbHelper;
    Context context;
    View view;
    String trip_id;
    GestureDetector gd;


    public RouteTouchListener(SQLHelper dbHelper, Context context, View view, final String trip_id, String train, String msg, boolean initState) {
        this.context = context;
        this.text  = msg;
        this.train = train;
        this.dbHelper = dbHelper;
        this.toggle = initState; /* true not selected */
        this.view = view;
        this.trip_id = trip_id;

        final Context ctx = this.context;
        final String tid = this.trip_id;
        final SQLHelper db = this.dbHelper;
        final View v = this.view;
        final String trn = this.train;

        gd = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                new DownloadFilesTask(RouteTouchListener.this, RouteTouchListener.this.dbHelper).execute(tid);
                return super.onDoubleTap(e);
            }
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                String fav = SQLHelper.get_user_pref_value(db.getReadableDatabase(), "favorites", "");
                ArrayList<String> favA = new ArrayList<String>();
                for (String x: fav.split(",")) {
                    favA.add(x);
                }

                if ( toggle == false ) {
                    toggle = true;
                    //th.setVisibility(View.VISIBLE);
                    v.setBackgroundColor(Color.WHITE);
                    favA.remove(trn);
                }
                else {
                    toggle = false;
                    //th.setVisibility(View.INVISIBLE);
                    v.setBackgroundColor(Color.parseColor("#18FFFF"));
                    favA.add(trn);
                }
                String names[] = favA.toArray(new String[0]);
                SQLHelper.update_user_pref(db.getWritableDatabase(), "favorites", Utils.join(",", names), new Date());
                Toast.makeText(v.getContext(),(String)"Train " + text,
                        Toast.LENGTH_LONG).show();
            }
        });

    }



    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gd.onTouchEvent(event);
        return true;
    }

    public void OnTaskComplete( ArrayList<HashMap<String, String>> data) {
        Intent intent = new Intent(context, RoutePagerActivity.class);
        intent.putExtra("data", new ParcelResult(data));
        context.startActivity(intent);
    }
    class DownloadFilesTask extends AsyncTask<String, Integer, Long> {
        SQLHelper db;
        ArrayList<HashMap<String, String>> data= new ArrayList<>();
        RouteTouchListener parent;
        DownloadFilesTask(RouteTouchListener parent, SQLHelper db) {
            this.parent = parent;
            this.db = db;
        }
        protected Long doInBackground(String... args) {
            //Intent intent = new Intent(ctx, RoutePagerActivity.class);
            String tid = args[0];
            // EditText editText = (EditText) view.getContext().findViewById(R.id.editText);
            //String message = editText.getText().toString();
            //intent.putExtra(EXTRA_MESSAGE, message);
            // query to get the stops for the tri
            String sql = "select st.*, sp.stop_lat, sp.stop_lon, sp.stop_name from stop_times st, stops sp where sp.stop_id = st.stop_id and st.trip_id = {trip_id} order by stop_sequence";
            sql = sql.replace("{trip_id}", tid);
            data = Utils.coerce(SQLHelper.query(db.getReadableDatabase(),sql));
            return new Long(data.size());
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Long result) {
            parent.OnTaskComplete(data);

        }
    }

}


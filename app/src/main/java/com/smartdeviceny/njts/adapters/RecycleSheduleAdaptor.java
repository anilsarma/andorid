package com.smartdeviceny.njts.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.smartdeviceny.njts.MainActivity;
import com.smartdeviceny.njts.R;
import com.smartdeviceny.njts.SystemService;
import com.smartdeviceny.njts.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class RecycleSheduleAdaptor extends RecyclerView.Adapter<RecycleSheduleAdaptor.ViewHolder> {
    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView train_name;
        TextView train_status_header;
        TextView track_status_details;
        TextView track_number;
        TextView train_live_header;
        TextView train_live_details;
        TextView departure_time;
        TextView duration;
        TextView arrival_time;
        LinearLayout train_live_layout;
        LinearLayout train_status_layout;
        LinearLayout details_line_layout;
        TextView  age;

        SystemService.Route route; // will be updated when visible
        int                 position;

        Button detail_button;

        ViewHolder(View itemView) {
            super(itemView);
            train_name = itemView.findViewById(R.id.train_name);
            train_status_header = itemView.findViewById(R.id.train_status_header);
            track_status_details= itemView.findViewById(R.id.track_status_details);
            track_number = itemView.findViewById(R.id.track_number);
            train_live_header = itemView.findViewById(R.id.train_live_header);
            train_live_details = itemView.findViewById(R.id.train_live_details);
            detail_button = itemView.findViewById(R.id.detail_button);
            departure_time = itemView.findViewById(R.id.departure_time);
            duration = itemView.findViewById(R.id.duration);
            arrival_time = itemView.findViewById(R.id.arrival_time);
            age = itemView.findViewById(R.id.age);

            train_live_layout = itemView.findViewById(R.id.train_live_layout);
            train_status_layout = itemView.findViewById(R.id.train_status_layout);
            details_line_layout = itemView.findViewById(R.id.details_line_layout);

            //myTextView = itemView.findViewById(R.id.tvAnimalName);
            //itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }
    private LayoutInflater mInflater;
    public List<SystemService.Route> mRoutes;
    int resid_delayed;
    int resid_normal;
    int resid_selected;
    int resid_round_gray;
    int resid_round_red;
    int resid_round_green;
    int resid_green;
    int resid_gray;

    // data is passed into the constructor
    public RecycleSheduleAdaptor(Context context, List<SystemService.Route> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mRoutes = data;
        Resources resources = mInflater.getContext().getApplicationContext().getResources();
        resid_delayed = resources.getIdentifier("route_background_delayed", "drawable", mInflater.getContext().getApplicationContext().getPackageName());
        resid_normal = resources.getIdentifier("route_background", "drawable", mInflater.getContext().getApplicationContext().getPackageName());
        resid_selected = resources.getIdentifier("route_background_selected", "drawable", mInflater.getContext().getApplicationContext().getPackageName());

        resid_round_green = resources.getIdentifier("card_text_border_green", "drawable", mInflater.getContext().getApplicationContext().getPackageName());
        resid_round_red = resources.getIdentifier("card_text_border_red", "drawable", mInflater.getContext().getApplicationContext().getPackageName());
        resid_round_gray= resources.getIdentifier("card_text_border_gray", "drawable", mInflater.getContext().getApplicationContext().getPackageName());
        resid_green= resources.getIdentifier("@android:color/holo_green_dark", "color", mInflater.getContext().getApplicationContext().getPackageName());
        resid_gray= resources.getIdentifier("@android:color/darker_gray", "color", mInflater.getContext().getApplicationContext().getPackageName());

        Log.d("REC", "resource green " + resid_green  + " " + resid_gray);
        //@android:color/holo_green_dark

    }
    public HashMap<String, SystemService.DepartureVisionData> departureVision = new HashMap<>();

    String make_key(String station, String block_id) {
        return station + "::" + block_id;
    }
    public void updateDepartureVision(@Nullable HashMap<String, SystemService.DepartureVisionData> departureVision) {
        HashMap<String, SystemService.DepartureVisionData> data = new HashMap<>();
        for(SystemService.DepartureVisionData dv:departureVision.values()) {
            data.put(make_key(dv.station, dv.block_id), dv);
        }
        this.departureVision = data;
        Log.d("REC", "DV Size:" + departureVision.size());
    }

    public void clearData() { this.mRoutes.clear(); }
    public void updateRoutes( List<SystemService.Route> routes) {
        this.mRoutes = routes;
    }
    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.template_schedule_entry, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setOnLongClickListener(view1 -> {
            if(holder.route!=null) {
                holder.route.favorite = !holder.route.favorite;
                ((MainActivity)RecycleSheduleAdaptor.this.mInflater.getContext()).updateFavorite( holder.route.favorite, holder.route.block_id);
                RecycleSheduleAdaptor.this.notifyItemChanged(holder.position);
            }
            return false;
        });
        view.setOnClickListener(view13 -> showDetailDailog(holder));
        Button button = view.findViewById(R.id.detail_button);
        view.setOnClickListener(view12 -> {
            showDetailDailog(holder);
        });
        return holder;
    }

    void showDetailDailog(ViewHolder holder) {
        SystemService systemService = ((MainActivity)mInflater.getContext()).systemService;
        if(systemService!=null && holder.route != null ) {
            ArrayList<HashMap<String, Object>> stops = systemService.getTripStops(holder.route.trip_id);
            ArrayList<HashMap<String, Object>> tmp = new ArrayList<>();
            HashMap<String, Object> header = new HashMap<>();
            header.put("arrival_time", "");
            SimpleDateFormat printFormat = new SimpleDateFormat("hh:mm a");
            header.put("stop_name", "#" + holder.route.block_id + " " + printFormat.format(holder.route.departure_time_as_date));
            tmp.add(header);
            for(HashMap<String, Object> o : stops) {
                tmp.add(o);
            }
            stops = tmp;
            //for(HashMap<String, Object> e:stops) {
            //    Log.d("REC", " " + Utils.capitalize(e.get("stop_name").toString()) + " " + e.get("arrival_time") + " " + e.get("departure_time"));
            //}
            //}
            //((MainActivity) mInflater.getContext()).getLayoutInflater().inflate()
            //TableLayout stopLayout = (TableLayout)((MainActivity) mInflater.getContext()).getLayoutInflater().inflate(R.layout.stop_entry_layout, null);
            ListView stopLayout = (ListView)((MainActivity) mInflater.getContext()).getLayoutInflater().inflate(R.layout.stop_entry_layout, null);

            Object data[] = stops.toArray();
            StopViewAdaptor adapter=new StopViewAdaptor(mInflater.getContext(), data );

            stopLayout.setAdapter(adapter);
            AlertDialog.Builder builder=new AlertDialog.Builder(mInflater.getContext());
            builder.setCancelable(true);

            builder.setView(stopLayout);
            AlertDialog dialog=builder.create();
            dialog.show();
        }
    }


    // binds the data to the TextView in each row TODO :: cleanup.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Date now = new Date();
        SystemService.Route route = mRoutes.get(position);
        holder.route = route;
        holder.position = position;

        String train_header = mRoutes.get(position).route_name + " #" + route.block_id;
        holder.train_name.setText(train_header);
        holder.track_number.setVisibility(View.INVISIBLE);

        holder.train_status_header.setVisibility(View.INVISIBLE);
        holder.track_status_details.setVisibility(View.INVISIBLE);

        holder.train_live_header.setVisibility(View.INVISIBLE);
        holder.train_live_details.setVisibility(View.INVISIBLE);

        holder.train_status_layout.setVisibility(View.GONE);
        holder.train_live_details.setVisibility(View.GONE);

        holder.detail_button.setVisibility(View.GONE);
        holder.details_line_layout.setVisibility(View.GONE);

        holder.itemView.setBackgroundResource(resid_normal);
        if (route.favorite) {
            holder.itemView.setBackgroundResource(resid_selected);
        }

        boolean canceled = false;
        boolean oldEntry = false;

        SimpleDateFormat printFormat = new SimpleDateFormat("hh:mm a");
        SystemService.DepartureVisionData dv = departureVision.get(make_key(route.station_code, route.block_id));

        if( dv !=null ) {
            // we need to check for time.
           boolean current_trains = false; // current train could
           try {
                Date tm = Utils.makeDate(Utils.getTodayYYYYMMDD(null), dv.time, "yyyyMMdd HH:mm"); // always today's date for this
                long diff = route.departure_time_as_date.getTime() - tm.getTime();
                if( diff < Math.abs( 60 * 1000 * 60 )) {
                    current_trains=true;
                }
            } catch (Exception e) {
               e.printStackTrace();
           }
            if( !dv.track.isEmpty()) {
                try {
                    if( current_trains) {
                        holder.track_number.setVisibility(View.VISIBLE);
                        holder.track_number.setText(dv.track);
                        holder.track_number.setBackgroundResource(resid_round_green);
                        holder.train_live_details.setText(dv.status);
                        holder.age.setText("updated " + printFormat.format(dv.createTime));
                        holder.details_line_layout.setVisibility(View.VISIBLE);

                        // diff will become negative once departure time passes the now.
                        // check againt the creation time, sometime the train may not have departed
                        // and the departure vision status has not yet updated.
                        { // more than x minutes
                            // check the creation time
                            long cdiff = now.getTime() - dv.createTime.getTime();
                            if (cdiff > 10 * 1000 * 60) {
                                holder.track_number.setBackgroundResource(resid_round_gray);
                                oldEntry = true;
                                holder.details_line_layout.setVisibility(View.GONE);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if( !dv.status.isEmpty() && current_trains) {
                if (!dv.stale && !oldEntry) {
                    holder.train_live_layout.setVisibility(View.VISIBLE);
                    holder.train_live_header.setVisibility(View.VISIBLE);
                    holder.train_live_details.setVisibility(View.VISIBLE);

                    holder.train_live_details.setText(dv.status);
                    holder.age.setText("updated " + printFormat.format(dv.createTime));
                    holder.details_line_layout.setVisibility(View.VISIBLE);

                }
                String s = dv.status.toUpperCase();
                if (s.contains("CANCEL") || s.contains("DELAY")) {
                    holder.itemView.setBackgroundResource(resid_delayed);
                    holder.track_number.setBackgroundResource(resid_round_red);
                    if( s.contains("CANCEL")) {
                        canceled = true;
                    }
                }
            }
        }

        String duration = "";
        try {
            Date st_time = route.getDate(route.departture_time);
            Date end_time = route.getDate(route.arrival_time);

            holder.departure_time.setText(printFormat.format(st_time ));
            holder.arrival_time.setText(printFormat.format(end_time ));

            long milli = end_time.getTime() - st_time.getTime();
            long minutes = milli/(60*1000);
            duration ="" + minutes + " min";

            long diff = (st_time.getTime() - now.getTime())/(1000*60);
            if ((diff >=-10) && (diff < 120)) {
                String schedule  = "in " + diff + " min" ;
                holder.train_status_header.setBackgroundResource(resid_green);
                if ( diff < 0 ) {
                    schedule= "" + Math.abs(diff) + " minutes ago ";
                    holder.train_status_header.setBackgroundResource(resid_gray);
                }
                if (!canceled ) {
                    // if the train has been canceled, the status is rubbish, for delayed we will use the
                    // original departure time for now.
                    holder.train_status_layout.setVisibility(View.VISIBLE);
                    holder.train_status_header.setVisibility(View.VISIBLE);
                    holder.track_status_details.setVisibility(View.VISIBLE);
                }
                holder.track_status_details.setText(schedule);
            }

        } catch (Exception e) {
            Log.d("REC", "issue here " +  e.getMessage() );
            e.printStackTrace();
        }
        holder.duration.setText(duration);
        //Log.d("REC", "setting holder for " + position);

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mRoutes.size();
    }

}


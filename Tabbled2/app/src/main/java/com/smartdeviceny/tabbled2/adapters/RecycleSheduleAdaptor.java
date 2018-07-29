package com.smartdeviceny.tabbled2.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartdeviceny.tabbled2.R;
import com.smartdeviceny.tabbled2.SystemService;
import com.smartdeviceny.tabbled2.utils.Utils;

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

            train_live_layout = itemView.findViewById(R.id.train_live_layout);
            train_status_layout = itemView.findViewById(R.id.train_status_layout);



            //myTextView = itemView.findViewById(R.id.tvAnimalName);
            //itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }
    private LayoutInflater mInflater;
    public List<SystemService.Route> mData ;
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
        this.mData = data;
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

    public void updateDepartureVision(@Nullable HashMap<String, SystemService.DepartureVisionData> departureVision) {
        HashMap<String, SystemService.DepartureVisionData> tmp = new HashMap<>();
        HashMap<String, SystemService.DepartureVisionData> track = new HashMap<>();
        for( String key:this.departureVision.keySet()) {
            SystemService.DepartureVisionData data = this.departureVision.get(key);
            if ( data.track.isEmpty() && data.status.isEmpty()) {
                //this.departureVision.remove(key);
            } else {
                data = data.clone();
                tmp.put(key, data);
                if(!data.track.isEmpty()) {
                    track.put(data.track, data);
                }
            }
        }
        for( String key:departureVision.keySet()) {
            SystemService.DepartureVisionData data = departureVision.get(key);
            SystemService.DepartureVisionData old = tmp.get(key);
            if(old != null ) {
                data = data.clone();
                if( data.track.isEmpty() && !old.track.isEmpty()) {
                    data.track = old.track;
                    data.stale = true;
                }
            }
            tmp.put(key, data);
            if(!data.track.isEmpty()) {
                SystemService.DepartureVisionData td = track.get(data.track);
                if(td!=null) {
                    //td.track = ""; // do we want to really do this , history is nice
                    td.status = ""; // new entry exits so clear the old entry.
                }
            }
        }
        this.departureVision = tmp;
    }

    public void updateRoutes( List<SystemService.Route> routes) {
        this.mData = routes;
    }
    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.template_schedule_entry, parent, false);
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                return false;
            }
        });
        return new ViewHolder(view);
        //return null;
    }



    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Date now = new Date();
        SystemService.Route route = mData.get(position);


        String train_header = mData.get(position).route_name + " #" + route.block_id;
        holder.train_name.setText(train_header);
        holder.track_number.setVisibility(View.INVISIBLE);

        holder.train_status_header.setVisibility(View.INVISIBLE);
        holder.track_status_details.setVisibility(View.INVISIBLE);

        holder.train_live_header.setVisibility(View.INVISIBLE);
        holder.train_live_details.setVisibility(View.INVISIBLE);

        holder.train_status_layout.setVisibility(View.GONE);
        holder.train_live_details.setVisibility(View.GONE);

        holder.detail_button.setVisibility(View.GONE);

        holder.itemView.setBackgroundResource(resid_normal);

        boolean canceled = false;
        boolean oldEntry = false;
        //SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm:ss");

        SimpleDateFormat printFormat = new SimpleDateFormat("hh:mm a");
        SystemService.DepartureVisionData dv = departureVision.get(route.block_id);
        if( dv !=null ) {
            // we need to check for time.
            boolean current_train = false;
            Log.d("REC", "got departure vision train:" + dv.block_id + " track:" + dv.track + " status:" + dv.status);
            if( !dv.track.isEmpty()) {
                try {
                    Date tm = Utils.makeDate(Utils.getTodayYYYYMMDD(null), dv.time, "yyyyMMdd HH:mm"); // always today's date for this
                    long diff = route.departure_time_as_date.getTime() - tm.getTime();
                    // more than an hour old must be a previous day
                    if( diff > -( 60 * 1000 * 60 )) {
                        current_train = true;
                        holder.track_number.setVisibility(View.VISIBLE);
                        holder.track_number.setText(dv.track);
                        holder.track_number.setBackgroundResource(resid_round_green);


                        if (diff > 2 * 1000 * 60) { // more than 5 minutes
                            // check the creation time
                            long cdiff = now.getTime() - dv.createTime.getTime();
                            if (cdiff > 2 * 1000 * 60) {
                                holder.track_number.setBackgroundResource(resid_round_gray);
                                oldEntry = true;
                            }
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            if( !dv.status.isEmpty() && current_train) {
                if (!oldEntry) {
                    holder.train_live_layout.setVisibility(View.VISIBLE);
                    holder.train_live_header.setVisibility(View.VISIBLE);
                    holder.train_live_details.setVisibility(View.VISIBLE);
                    holder.train_live_details.setText(dv.status);
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
        else {
            //Log.d("REC", "DV not found for block_id:" + route.block_id);
        }

        String duration = "";
        try {
            //Date st_time = timeformat.parse(route.departture_time);
           // Date end_time = timeformat.parse(route.arrival_time);

            Date st_time = route.getDate(route.departture_time);
            Date end_time = route.getDate(route.arrival_time);

            holder.departure_time.setText(printFormat.format(st_time ));
            holder.arrival_time.setText(printFormat.format(end_time ));

            long milli = end_time.getTime() - st_time.getTime();
            long minutes = milli/(60*1000);
            duration ="" + minutes + " min";

            long diff = (st_time.getTime() - now.getTime())/(1000*60);
            if ((diff >=-10) && (diff < 120)) {
                String schedule  = "departs in " + diff + " min" ;
                holder.train_status_header.setBackgroundResource(resid_green);
                if ( diff < 0 ) {
                    schedule= "departed " + Math.abs(diff) + " minutes ago ";
                    holder.train_status_header.setBackgroundResource(resid_gray);
                }
                if (!canceled ) {
                    // if the train has been canceled, the status is rubbish, for delayed we will use the
                    // original departure time for now.
                    holder.train_status_layout.setVisibility(View.VISIBLE);
                    holder.train_status_header.setVisibility(View.VISIBLE);
                    holder.track_status_details.setVisibility(View.VISIBLE);
                }
                //holder.train_status_header.
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
        return mData.size();
    }

}


package com.smartdeviceny.tabbled2.adapters;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.smartdeviceny.tabbled2.R;
import com.smartdeviceny.tabbled2.SystemService;

import java.text.SimpleDateFormat;
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


            //myTextView = itemView.findViewById(R.id.tvAnimalName);
            //itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }
    private LayoutInflater mInflater;
    private List<SystemService.Route> mData;

    // data is passed into the constructor
    public RecycleSheduleAdaptor(Context context, List<SystemService.Route> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }
    HashMap<String, SystemService.DepartureVisionData> departureVision = new HashMap<>();

    public void updateDepartureVision(HashMap<String, SystemService.DepartureVisionData> departureVision) {
        this.departureVision = departureVision;
    }

    public void updateRoutes( List<SystemService.Route> routes) {
        this.mData = routes;
    }
    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.template_schedule_entry, parent, false);
        return new ViewHolder(view);
        //return null;
    }



    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SystemService.Route rt = mData.get(position);

        SystemService.Route route = mData.get(position);
        SystemService.DepartureVisionData dv = departureVision.get(route.block_id);

        String train_header = mData.get(position).route_name + " #" + route.block_id;
        holder.train_name.setText(train_header);
        holder.track_number.setVisibility(View.INVISIBLE);

        holder.train_status_header.setVisibility(View.INVISIBLE);
        holder.track_status_details.setVisibility(View.INVISIBLE);

        holder.train_live_header.setVisibility(View.INVISIBLE);
        holder.train_live_details.setVisibility(View.INVISIBLE);

        holder.detail_button.setVisibility(View.GONE);
        SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat printFormat = new SimpleDateFormat("HH:mm");
        if( dv !=null ) {
            Log.d("REC", "got departure vision train:" + dv.block_id + " track:" + dv.track + " status:" + dv.status);
            if( !dv.track.isEmpty()) {
                holder.track_number.setVisibility(View.VISIBLE);
                holder.track_number.setText(dv.track);
            }

            holder.train_live_header.setVisibility(View.VISIBLE);
            holder.train_live_details.setVisibility(View.VISIBLE);
            holder.train_live_details.setText(dv.status);
        }
        else {
            Log.d("REC", "DV not found for block_id:" + route.block_id);
        }



        String duration = "";
        try {
            Date st_time = timeformat.parse(route.departture_time);
            Date end_time = timeformat.parse(route.arrival_time);
            Date now = new Date();
            holder.departure_time.setText(printFormat.format(st_time ));
            holder.arrival_time.setText(printFormat.format(end_time ));

            long milli = end_time.getTime() - st_time.getTime();
            long minutes = milli/(60*1000);
            duration ="" + minutes + " mins";

            long diff = (st_time.getTime() - (now.getTime()))/(1000*60);
            if ((diff >=-10) && (diff < 120) && (dv !=null)) {
                String schedule  = "Scheduled in " + diff + " mins" ;
                if ( diff < 0 ) {
                    schedule= "Departed " + Math.abs(diff) + " minutes ago ";
                }
                holder.train_status_header.setVisibility(View.VISIBLE);
                holder.track_status_details.setVisibility(View.VISIBLE);
                //holder.train_status_header.
                holder.track_status_details.setText(schedule);
//                if (diff > 0 ) { // &&  fav.contains("" + block_id )) {
//                    if(!notification) {
//                        NotificationCompat.Builder mBuilder =
//                                new NotificationCompat.Builder(getContext().getApplicationContext())
//                                        .setSmallIcon(R.mipmap.app_njs_icon)
//                                        .setTicker("Upgrade (Open to see the info).")
//                                        .setContentTitle("Train " + block_id + " Track# " + platform  )
//                                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//                                        .setContentText( Utils.formatToLocalTime(Utils.parseLocalTime(departture_time)) + " departure in " +  Math.abs(diff) + "(mins)" );
//                        // NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//                        Notification notify = mBuilder.build();
//
//                        notify.flags|= Notification.FLAG_AUTO_CANCEL;
//                        //notify.defaults |= Notification.DEFAULT_VIBRATE;
//                        final NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
//                        mNotificationManager.notify(R.integer.NOTIFICATION_ROUTE_STATUS, notify);
//
//                        Toast.makeText(getContext(), (String) "sent notification ", Toast.LENGTH_SHORT).show();
//                        notification = true;
//                    }
//                }
            }

        } catch (Exception e) {
        }
        holder.duration.setText(duration);
        Log.d("REC", "setting holder for " + position);

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

}


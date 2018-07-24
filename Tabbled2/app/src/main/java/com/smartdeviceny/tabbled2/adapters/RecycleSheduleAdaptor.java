package com.smartdeviceny.tabbled2.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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
        SystemService.DepartureVisionData dv = departureVision.get(route.trip_id);

        String train_header = mData.get(position).route_name + " #" + mData.get(position).trip_id;
        holder.train_name.setText(train_header);
        holder.track_number.setVisibility(View.INVISIBLE);

        holder.train_status_header.setVisibility(View.INVISIBLE);
        holder.track_status_details.setVisibility(View.INVISIBLE);

        holder.train_live_header.setVisibility(View.INVISIBLE);
        holder.train_live_details.setVisibility(View.INVISIBLE);

        holder.detail_button.setVisibility(View.GONE);

        if( dv !=null ) {
            holder.track_number.setVisibility(View.VISIBLE);
            holder.track_number.setText(dv.trip);

            holder.train_live_header.setVisibility(View.VISIBLE);
            holder.train_live_details.setVisibility(View.VISIBLE);
            holder.train_live_details.setText(dv.status);
        }



        SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat printFormat = new SimpleDateFormat("HH:mm");

        String duration = "";
        try {
            Date st_time = timeformat.parse(mData.get(position).departture_time);
            Date end_time = timeformat.parse(mData.get(position).destination_time);

            holder.departure_time.setText(printFormat.format(st_time ));
            holder.arrival_time.setText(printFormat.format(end_time ));

            long milli = end_time.getTime() - st_time.getTime();
            long minutes = milli/(60*1000);
            duration = "" + new Long(minutes).toString() + " mins" ;
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


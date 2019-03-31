package com.smartdeviceny.dev.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;



public class RecyclerAdapter extends StickHeaderRecyclerView<CustomerData, HeaderDataImpl> {


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case HeaderDataImpl.HEADER_TYPE_1:
                return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.header1_item_recycler, parent, false));
            case HeaderDataImpl.HEADER_TYPE_2:
                return new Header2ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.header2_item_recycler, parent, false));
            default:
                return new ViewHolder(parent.getContext(), LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {


        AlphaAnimation aa = new AlphaAnimation(0.1f, 1.0f);
        aa.setDuration(400);
        if (holder instanceof ViewHolder) {
            ((ViewHolder) holder).bindData(position);
        } else if (holder instanceof HeaderViewHolder){
            ((HeaderViewHolder) holder).bindData(position);
        } else if (holder instanceof Header2ViewHolder){
            ((Header2ViewHolder) holder).bindData(position);
        }
    }

    @Override
    public void bindHeaderData(View header, int headerPosition) {

        /*TextView tv = header.findViewById(R.id.tvHeader);
        tv.setText(String.valueOf(headerPosition / 5));*/
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;

        HeaderViewHolder(View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvHeader);
        }

        void bindData(int position) {
            tvHeader.setText(String.valueOf(position / 5));
        }
    }

    class Header2ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;

        Header2ViewHolder(View itemView) {
            super(itemView);
            tvHeader = itemView.findViewById(R.id.tvHeader);
        }

        void bindData(int position) {
            tvHeader.setText(String.valueOf(position / 5));
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        View recyclerView;
        TextView tvRows;
        Context context;

        ViewHolder(Context context, View itemView) {
            super(itemView);
            recyclerView = itemView;
            this.context = context;
            tvRows = itemView.findViewById(R.id.tv_travel_sq_time);
        }

        void bindData(int position) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_recycler_item_show);
            recyclerView.startAnimation(animation);

            getDataInPosition(position).getFelan();
           // tvRows.setText("saber" + position);
            //((ViewGroup) tvRows.getParent()).setBackgroundColor(Color.parseColor("#ffffff"));
        }
    }
}
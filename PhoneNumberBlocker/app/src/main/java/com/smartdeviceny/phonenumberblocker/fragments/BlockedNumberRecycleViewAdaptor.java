package com.smartdeviceny.phonenumberblocker.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.telecom.Call;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smartdeviceny.phonenumberblocker.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BlockedNumberRecycleViewAdaptor extends RecyclerView.Adapter<BlockedNumberRecycleViewAdaptor.ViewHolder> {
    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvBlockedNumber;

        ViewHolder(View itemView) {
            super(itemView);
            tvBlockedNumber = itemView.findViewById(R.id.tvBlockedNumber);
        }

        @Override
        public void onClick(View view) {
            //if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }
    private LayoutInflater mInflater;
    ArrayList<String> numbers;
    public BlockedNumberRecycleViewAdaptor(Context context,ArrayList<String> numbers) {
        this.mInflater = LayoutInflater.from(context);
        this.numbers = numbers;
    }
    void updateData(ArrayList<String> numbers) {
        this.numbers = numbers;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.blocked_numbers, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    // binds the data to the TextView in each row TODO :: cleanup.
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        String msg = numbers.get(position);
        try {
            CallRecord rec = new CallRecord(msg);
            if( rec.comment.contains("not blocked")) {
                msg = "\u2705";
            } else {
                msg = "\u274c"; // blocked.
            }
            msg += " " + rec.number + " " + rec.dateFormat.format(rec.date) + "\u279F" + rec.count;
        }catch (Exception e) {}
        holder.tvBlockedNumber.setText(msg);
        //Log.d("REC", "setting holder for " + position);

    }

    public void removeAt(int position) {
        if( position < numbers.size()) {
            String msg = numbers.get(position);
            SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(mInflater.getContext());
            Set<String> entries = new HashSet<>();
            entries = config.getStringSet("TerminatedNumbers", entries);
            ArrayList<String> c = new ArrayList<>(entries);
            c.remove(msg);
            config.edit().putStringSet("TerminatedNumbers",new HashSet<String>(c)).commit();
            this.numbers = c;
        }
    }
    // total number of rows
    @Override
    public int getItemCount() {
        return numbers.size();
    }

}


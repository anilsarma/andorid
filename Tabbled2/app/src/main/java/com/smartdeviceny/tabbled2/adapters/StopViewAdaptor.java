package com.smartdeviceny.tabbled2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.smartdeviceny.tabbled2.R;
import com.smartdeviceny.tabbled2.utils.Utils;

import java.util.HashMap;

public class StopViewAdaptor extends ArrayAdapter {
    Context context;
    LayoutInflater mInflator;
    Object [] data;
    public StopViewAdaptor(Context context, Object[] data) {
        super(context, R.layout.stop_entry_layout, /*objects*/data);
        mInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = mInflator.inflate(R.layout.template_stop_entry, parent, false);
        }
        HashMap<String, Object> p = (HashMap<String, Object>)data[position];
        View tablerow = view.findViewById(R.id.stop_entry_row);
        ((TextView) view.findViewById(R.id.arrival_time)).setText(p.get("arrival_time").toString());
        ((TextView) view.findViewById(R.id.stop_name)).setText(Utils.capitalize(p.get("stop_name").toString()));
        if( position%2==0) {
            tablerow.setBackgroundResource(R.drawable.stop_background_even);
        } else {
            tablerow.setBackgroundResource(R.drawable.stop_background_odd);
        }
        return view;
    }

}

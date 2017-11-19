package com.example.asarma.njrails;


import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.Toast;

import com.example.asarma.njrails.route.RoutePagerActivity;

import java.util.ArrayList;
import java.util.Date;

public class RouteLongClickListener implements   View.OnLongClickListener {

    final String text;
    String train;
    boolean toggle = true;
    SQLHelper helper;
    public RouteLongClickListener( SQLHelper helper,  String train, String msg, boolean initState) {

        this.text = msg;
        this.train = train;
        this.helper = helper;
        this.toggle = initState; /* true not selected */
    }
    @Override
    public boolean onLongClick(View view) {
        String fav = SQLHelper.get_user_pref_value(helper.getReadableDatabase(), "favorites", "");
        ArrayList<String> favA = new ArrayList<String>();
        for (String x: fav.split(",")) {
            favA.add(x);
        }

        if ( toggle == false ) {
            toggle = true;
            //th.setVisibility(View.VISIBLE);
            view.setBackgroundColor(Color.WHITE);
            favA.remove(train);
        }
        else {
            toggle = false;
            //th.setVisibility(View.INVISIBLE);
            view.setBackgroundColor(Color.parseColor("#18FFFF"));
            favA.add(train);
        }
        String names[] = favA.toArray(new String[0]);
        SQLHelper.update_user_pref(helper.getWritableDatabase(), "favorites", Utils.join(",", names), new Date());
        Toast.makeText(view.getContext(),(String)"Train " + text,
                Toast.LENGTH_LONG).show();


        Intent intent = new Intent(view.getContext(), RoutePagerActivity.class);
       // EditText editText = (EditText) view.getContext().findViewById(R.id.editText);
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        //view.getContext().startActivity(intent);

        return true;
    }
}


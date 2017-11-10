package com.example.asarma.njrails;


import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.Toast;

import com.example.asarma.njrails.route.RoutePagerActivity;

import java.util.ArrayList;
import java.util.Date;

public class RouteShortClickListener implements   View.OnClickListener {
    final String text;
    String train;
    boolean toggle = true;
    SQLHelper helper;
    public RouteShortClickListener(SQLHelper helper, String train, String msg, boolean initState) {

        this.text = msg;
        this.train = train;
        this.helper = helper;
        this.toggle = initState; /* true not selected */
    }


    @Override
    public void onClick(View view) {

        Intent intent = new Intent(view.getContext(), RoutePagerActivity.class);
        // EditText editText = (EditText) view.getContext().findViewById(R.id.editText);
        //String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        view.getContext().startActivity(intent);

    }
}


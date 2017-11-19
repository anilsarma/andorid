package com.example.asarma.njrails;

import android.content.Context;
import android.graphics.Color;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

/**
 * Created by asarma on 11/3/2017.
 */

public class TableRowListener implements View.OnClickListener {
    TextView th;
    String text;
    boolean toggle = true;
    public TableRowListener(TextView th, String text)
    {
        this.th = th;
        this.text = text;
    }
    @Override
    public void onClick(View view) {
        if ( toggle == false ) {
            toggle = true;
            //th.setVisibility(View.VISIBLE);
            view.setBackgroundColor(Color.WHITE);
        }
        else {
            toggle = false;
            //th.setVisibility(View.INVISIBLE);
            view.setBackgroundColor(Color.YELLOW);
        }
        Toast.makeText(view.getContext(),(String)"Train " + text,
                Toast.LENGTH_LONG).show();
    }
}

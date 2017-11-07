package com.example.asarma.njrails;


import android.graphics.Color;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class RouteTouchListener implements   View.OnTouchListener {
    final View view;
    final String text;
    GestureDetector gestureDetector;

    public RouteTouchListener(View viewe, String msg) {
        this.view = viewe;
        this.text=msg;
        gestureDetector = new GestureDetector(view.getContext(), new GestureDetector.SimpleOnGestureListener() {
            boolean toggle = true;
            public void onLongPress(MotionEvent e) {
               // super.onLongPress(e);
                if ( toggle == false ) {
                    toggle = true;
                    //th.setVisibility(View.VISIBLE);
                    view.setBackgroundColor(Color.WHITE);
                }
                else {
                    toggle = false;
                    //th.setVisibility(View.INVISIBLE);
                    view.setBackgroundColor(Color.parseColor("#18FFFF"));
                }
                Toast.makeText(view.getContext(),(String)"Train " + text,
                        Toast.LENGTH_LONG).show();
            }
            public boolean onSingleTapUP(MotionEvent e) {
                return super.onSingleTapUp(e);
            }
        });
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return gestureDetector.onTouchEvent(motionEvent);
    }
}


package com.smartdeviceny.njts;

import android.app.Activity;
import android.os.Bundle;

import com.smartdeviceny.njts.R;

public class NotificationReceiverActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result);
       // Toast.makeText(NotificationReceiverActivity.this.getApplicationContext(), "Got a Notification");

    }
}
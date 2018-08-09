package com.smartdeviceny.phonenumberblocker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.smartdeviceny.phonenumberblocker.fragments.CallRecord;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
//import com.android.internal.telephony.ITelephony;

public class IncomingCallReceiver extends BroadcastReceiver {
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
    @Override
    public void onReceive(Context context, Intent intent) {

        //Log.d("BLOCK", "incoming call " + intent);
        Object  telephonyService; //ITelephony
        try {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            //Toast.makeText(context, "Incoming call : " + number, Toast.LENGTH_LONG).show();
            Log.d("BLOCK", "call detected " + number + " State:" + state );
            if(state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)){
                Date now = new Date();
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                try {
                    Method m = tm.getClass().getDeclaredMethod("getITelephony");
                    m.setAccessible(true);
                    telephonyService =  m.invoke(tm); // ITelephony
                    if ((number != null)) {
                        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
                        Set<String> entries = config.getStringSet("TerminatedNumbers", new HashSet<String>());
                        ArrayList<String> numbers = new ArrayList<>(entries);

                        CallRecord rec = new CallRecord(number, now, "", 1);
                        for(String e:numbers) {
                            if(e.startsWith(rec.number)) {
                                rec = new CallRecord(e);
                                rec.count ++;
                                break;
                            }
                        }
                        numbers = clean(numbers, number);

                        if( number.startsWith("510757") || number.startsWith("6469682305")) {
                            rec.comment = "blocked";
                            numbers.add(rec.toString());
                            SharedPreferences.Editor editor = config.edit();
                            editor.putStringSet("TerminatedNumbers", new HashSet<>(numbers));
                            editor.commit();

                            createNotificationChannel(context, "BC");

                            Method endCall = telephonyService.getClass().getDeclaredMethod("endCall");
                            endCall.invoke(telephonyService);
                            //telephonyService.endCall()
                            Log.d("BC", "Ending call from " + number );
                            Toast.makeText(context, "Ending the call from: " + number + " State:" + state, Toast.LENGTH_SHORT).show();
                            NotificationCompat.Builder mBuilder = makeNotificationBuilder(context, "BC",
                                    "Call blocked", "From Number :" + number+ " State:" + state);
                            Notification notification = mBuilder.build();
                            notification.flags |= Notification.FLAG_AUTO_CANCEL;
                            notify(context, notification, 1);
                        } else {
                            //numbers.add(number + " not blocked " + dateFormat.format(now));
                            rec.comment = "not blocked";
                            numbers.add(rec.toString());
                            SharedPreferences.Editor editor = config.edit();
                            editor.putStringSet("TerminatedNumbers", new HashSet<>(numbers));
                            editor.commit();
                        };
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                //Toast.makeText(context, "Ring " + number, Toast.LENGTH_SHORT).show();

            }
            if(state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)){
                //Toast.makeText(context, "Answered " + number, Toast.LENGTH_SHORT).show();
            }
            if(state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)){
               // Toast.makeText(context, "Idle "+ number, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    ArrayList<String> clean(ArrayList<String> numbers, String number) {
        ArrayList<String> entries = new ArrayList<>();
        for(String e:numbers) {
            if(e.startsWith(number)) {
                entries.add(e);
            }
        }
        for(String e:entries) {
            numbers.remove(e);
        }
        return numbers;
    }

    private static void createNotificationChannel(Context context, String CHANNEL_ID) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    public static NotificationCompat.Builder makeNotificationBuilder(Context context, String channelID, String title, String msg) {
        createNotificationChannel( context, channelID);
        //int icon = R.mipmap.ic_launcher;
        //long when = System.currentTimeMillis();
//        Notification notification = new Notification(icon, getString(R.string.app_name), when);
//        notification.flags |= Notification.FLAG_NO_CLEAR; //Do not clear the notification
//        notification.defaults |= Notification.DEFAULT_LIGHTS; // LED
//        notification.defaults |= Notification.DEFAULT_VIBRATE; //Vibration
//        notification.defaults |= Notification.DEFAULT_SOUND; // Sound

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, channelID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setContentText(msg);
        return mBuilder;
    }
    public static void notify(Context context, NotificationCompat.Builder builder, @Nullable Integer id) {
        final NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = builder.build();
        //notification.flags |= Notification.FLAG_AUTO_CANCEL;
        if(id == null ) {
            id = new Integer(1);
        }
        mNotificationManager.notify(id, notification);
    }
    public static void notify(Context context, Notification notification, @Nullable Integer id) {
        final NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(id == null ) {
            id = new Integer(1);
        }
        mNotificationManager.notify(id, notification);
    }
    public static void notify_user(Context context, String channelID, String title, String msg, @Nullable  Integer id) {

        NotificationCompat.Builder mBuilder =makeNotificationBuilder( context, channelID, title, msg);
        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        //notification.defaults |= Notification.DEFAULT_SOUND;

        Log.d("SVC", "notification sent " + msg );
        if(id == null ) {
            id = new Integer(1);
        }
        final NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id, notification);
    }
}
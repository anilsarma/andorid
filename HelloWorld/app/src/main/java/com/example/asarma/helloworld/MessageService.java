package com.example.asarma.helloworld;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Set;

public class MessageService extends Service {
    class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            Log.d("msg_service", "Got Message " + msg.what);
            switch (msg.what) {
                case Config.MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case Config.MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case Config.MSG_SET_INT_VALUE:
                    incrementby = msg.arg1;
                    sendMessageToUI( 12);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    private int counter = 0, incrementby = 1;

    public MessageService()
    {

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        sendMessage();
        return super.onStartCommand(intent, flags, startId);
    }

    // Send an Intent with an action named "custom-event-name". The Intent
    // sent should
    // be received by the ReceiverActivity.
    private void sendMessage() {
        Config config = new Config(getApplicationContext());
        Log.d("sender", "Config content " + config.getString("SOME_FLAG", "N?A") );

        Set<String> values = config.getStringSet("SET", null);
        for(String s :values ) {
            Log.d("sender", "Config item " + s );
        }
        try {
            JSONArray jarray = new JSONArray(values.toArray());
            Log.d("sender", "Config json " + new Gson().toJson(values));
        }
        catch(Exception e) {

        }
        System.out.print("MessageService::sendMessage");
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("custom-event-name");
        // You can also include some extra data.
        intent.putExtra("message", "This is my message!");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    private void sendMessageToUI(int intvaluetosend) {
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                // Send data as an Integer
                mClients.get(i).send(Message.obtain(null, Config.MSG_SET_INT_VALUE, intvaluetosend, 0));

                //Send data as a String
                Bundle b = new Bundle();
                b.putString("str1", "ab" + intvaluetosend + "cd");
                Message msg = Message.obtain(null, Config.MSG_SET_STRING_VALUE);
                msg.setData(b);
                mClients.get(i).send(msg);

            }
            catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }

}
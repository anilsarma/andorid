package com.example.asarma.helloworld;

import android.app.DownloadManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public class SystemService extends Service {

    boolean           checkingVersion;
    DownloadFile      downloader;

    DownloadFile.Callback callback = new DownloadFile.Callback() {
        @Override
        public void downloadFailed(DownloadFile d, long id, String url) {
            checkingVersion = false;
            // we need to redo again.
        }

        @Override
        public boolean downloadComplete(DownloadFile d, long id, String url, File file) {
            checkingVersion = false;
            return true; // remove the file.
        }
    };

    public SystemService()
    {}

    @Override
    public void onCreate() {

        downloader = new DownloadFile(this.getApplicationContext(),  callback);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if(downloader !=null) {
            downloader.cleanup();
        }
        super.onDestroy();
    }

    boolean checkForUpdate() {
        if (checkingVersion) {
            return false; // already running.
        }

        // download the file ..
        String url = "https://raw.githubusercontent.com/anilsarma/misc/master/njt/version.txt";
        downloader.downloadFile(url, "", "", DownloadManager.Request.NETWORK_MOBILE| DownloadManager.Request.NETWORK_WIFI, null);
        checkingVersion = true;
        return true;
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;// for now
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendMessage() {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("custom-event-name");
        // You can also include some extra data.
        intent.putExtra("message", "This is my message!");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}
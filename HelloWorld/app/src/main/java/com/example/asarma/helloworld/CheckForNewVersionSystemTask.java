package com.example.asarma.helloworld;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.io.File;

// the main purpose of this is to check for new version in the remote
// server.
public class CheckForNewVersionSystemTask {
    Context systemService;
    boolean  checking = false;
    DownloadManager   manager;
    long              requestid = 0;
    DownloadFile      downloader;

    DownloadFile.Callback callback = new DownloadFile.Callback() {
        @Override
        public void downloadFailed(DownloadFile d, long id, String url) {
            checking = false;
        }

        @Override
        public boolean downloadComplete(DownloadFile d,long id, String url, File file) {
            checking = false;
            return true; // remove the file.
        }
    };
    public CheckForNewVersionSystemTask(SystemService parent) {
        systemService = parent;

    }
    void init()
    {
        downloader = new DownloadFile(systemService,  callback);
    }
    void cleanup() {
        if(downloader !=null) {
            downloader.cleanup();
        }
    }
    boolean checkForUpdate() {
        if (checking) {
            return false; // already running.
        }

        // download the file ..
        String url = "https://raw.githubusercontent.com/anilsarma/misc/master/njt/version.txt";
        downloader.downloadFile(url, "", "", DownloadManager.Request.NETWORK_MOBILE| DownloadManager.Request.NETWORK_WIFI, null);
        checking = true;
        return true;
    }


}

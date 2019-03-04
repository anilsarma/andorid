package com.smartdeviceny.njts.utils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

// a general purpose code to download using the download manager.
public class DownloadFile {
    Context context;
    DownloadManager manager;
    LocalBcstReceiver receiver = new LocalBcstReceiver();
    ArrayList<Long> requestid = new ArrayList<>();
    Callback callback;

    public interface Callback {
        boolean downloadComplete(DownloadFile d, long id, String url, File file);
        void downloadFailed(DownloadFile d, long id, String url);
    }

    public DownloadFile(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;
        manager = (DownloadManager) context.getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
        context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public void cleanup() {
        context.unregisterReceiver(receiver);
    }

    public DownloadManager.Request buildRequest(String url, String title, String description, int request_flags, @Nullable String mimetype) {
        //String url = "https://raw.githubusercontent.com/anilsarma/misc/master/njt/version.txt";
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        if (mimetype == null) {
            //request.setMimeType("text/plain");//application/x-compressed
        }
        request.setDescription(description);
        request.setVisibleInDownloadsUi(false);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setAllowedNetworkTypes(request_flags);
        request.setRequiresDeviceIdle(false);
        request.setRequiresCharging(false);
        request.setTitle(title);
        request.addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36");

        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, title);
        return request;
    }

    public long downloadFile(String url, String title, String description, int request_flags, @Nullable String mimetype) {
        //String url = "https://raw.githubusercontent.com/anilsarma/misc/master/njt/version.txt";
        DownloadManager.Request request = buildRequest(url, title, description, request_flags, mimetype);
        return enqueue(request);

    }

    public long enqueue(DownloadManager.Request request) {
        long id = manager.enqueue(request);
        this.requestid.add(id);
        return id;
    }

    long downloadFile(DownloadManager.Request request) {
        long id = manager.enqueue(request);
        this.requestid.add(id);
        Log.d("download", "downloadFile scheduling .. " + id);
        return id;
    }

    // classes ..
    public class LocalBcstReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action))
            handle_download_complete();
        }
        public void handle_download_complete() {
            DownloadManager.Query query = new DownloadManager.Query();
            long rids[] = requestid.stream().mapToLong(l -> l).toArray();
            query.setFilterById(rids);
            if (rids.length == 0) {
                return;
            }
            Cursor c = manager.query(query);
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                int ID = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_ID));

                if ((status & DownloadManager.STATUS_SUCCESSFUL) == DownloadManager.STATUS_SUCCESSFUL) {
                    File mFile = new File(Uri.parse(c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))).getPath());
                    boolean removeFile = true;
                    if (DownloadFile.this.callback != null) {
                        String url = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
                        removeFile = DownloadFile.this.callback.downloadComplete(DownloadFile.this, ID, url, mFile);
                    }
                    if (removeFile) {
                        Utils.delete(mFile);
                    }
                    requestid.remove(new Long(ID));
                }
                if ((status & DownloadManager.STATUS_FAILED) == DownloadManager.STATUS_FAILED) {
                    if (DownloadFile.this.callback != null) {
                        String url = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
                        DownloadFile.this.callback.downloadFailed(DownloadFile.this, ID, url);
                    }
                    requestid.remove(new Long(ID));
                }
                if ((status & DownloadManager.STATUS_PAUSED) == DownloadManager.STATUS_PAUSED) {
                    String url = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
                    Log.d("DNLD", "Paused ID " + ID + " url:" + url);
                }

                if ((status & DownloadManager.STATUS_PENDING) == DownloadManager.STATUS_PENDING) {
                    String url = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
                    Log.d("DNLD", "Pending ID " + ID + " url:" + url);
                }
            } // for loop
            //Log.d("DNLD", "loop done");
        }
    } // Receiver


}

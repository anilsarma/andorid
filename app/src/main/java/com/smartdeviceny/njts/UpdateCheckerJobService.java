package com.smartdeviceny.njts;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.smartdeviceny.njts.values.NotificationValues;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UpdateCheckerJobService extends JobService {

    public UpdateCheckerJobService() {
        super();
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        try {
            ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
            boolean isMobile = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;

            Log.d("UPDJOB", "onStartJob - periodic job. " + isConnected + " wifi:" + isWiFi + " isMobile:" + isMobile);
            if(!isConnected) {

            }
            // we should do this in the background on wifi only.
            SharedPreferences config  = PreferenceManager.getDefaultSharedPreferences(this);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            Date now = new Date();
            String last = config.getString("LAST_TIME", formatter.format(now));
            Date lasttime = formatter.parse(last);
            long diff = now.getTime()- lasttime.getTime();
            //Log.d("UPDJOB", "Time diffence :"  + (diff/(1000 * 60)) + " minutes");
            //if( diff  > (1000* 60 * 60 * 6 )) { // 6 hrs.
                sendCheckForUpdate();
            //}
        } catch(Exception e) {
          e.printStackTrace();
        } finally {
            jobFinished(jobParameters, false);
            //Log.d("UPDJOB", "onStartJob - periodic job, complete");
        }
        return false; // let the system know we have no job running ..
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d("UPDJOB", "onStopJob - done");
        return false;
    }

    public void sendCheckForUpdate() {
        Intent intent = new Intent(NotificationValues.BROADCAT_CHECK_FOR_UPDATE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        //Log.d("UPDJOB", "sending " + NotificationValues.BROADCAT_CHECK_FOR_UPDATE);
    }
}

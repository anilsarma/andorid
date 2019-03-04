package com.smartdeviceny.njts;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.smartdeviceny.njts.utils.Utils;
import com.smartdeviceny.njts.values.Config;
import com.smartdeviceny.njts.values.ConfigDefault;
import com.smartdeviceny.njts.values.NotificationValues;

import java.util.Date;

public class DepartureVisionJobService extends JobService {

    public DepartureVisionJobService() {
        super();
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        //Log.d("JOB", "onStartJob - periodic job.");

        try {
            ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
            boolean isMobile = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;

            Log.d("JOB", "DepartureVisionJobService - isConnected:" + isConnected + " wifi:" + isWiFi + " isMobile:" + isMobile + " ");
            if(isConnected) {
                sendDepartureVisionPings();
               // Utils.notify_user(this.getApplicationContext(), "NJTS", "NJTS", "Ping Sent " + new Date(), 1);
            }
            sendTimerEvent();
        } catch(Exception e) {
          e.printStackTrace();
        } finally {
            String time = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Config.POLLING_TIME, ConfigDefault.POLLING_TIME);
            int polling_time = 30000;
            try { polling_time = Integer.parseInt(time); } catch(Exception e) {}
            polling_time = Math.max(10000, polling_time);

            Date now = new Date();
            long epoch_time = now.getTime();
            epoch_time += polling_time; // next polling time
            epoch_time  = (epoch_time/polling_time) * polling_time; // to the next clock time.
            long diff = epoch_time - now.getTime();

            Utils.scheduleJob(this.getApplicationContext(), DepartureVisionJobService.class, (int)diff, false);

            jobFinished(jobParameters, true);
            //Log.d("JOB", "onStartJob - periodic job, complete " + time);
            return false; // let the system know we have no job running ..
        }
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d("JOB", "onStopJob - done");
        return false;
    }

    public void sendTimerEvent() {
        Intent intent = new Intent(NotificationValues.BROADCAT_PERIODIC_TIMER);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        //Log.d("JOB", "sending " + NotificationValues.BROADCAT_PERIODIC_TIMER);
    }
    public void sendDepartureVisionPings() {
        Intent intent = new Intent(NotificationValues.BROADCAT_SEND_DEPARTURE_VISION_PING);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        //Log.d("JOB", "sending " + NotificationValues.BROADCAT_SEND_DEPARTURE_VISION_PING);
    }
}

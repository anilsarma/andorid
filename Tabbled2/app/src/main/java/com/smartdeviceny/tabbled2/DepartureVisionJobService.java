package com.smartdeviceny.tabbled2;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.smartdeviceny.tabbled2.utils.Utils;
import com.smartdeviceny.tabbled2.values.Config;
import com.smartdeviceny.tabbled2.values.ConfigDefault;
import com.smartdeviceny.tabbled2.values.NotificationValues;

public class DepartureVisionJobService extends JobService {

    public DepartureVisionJobService() {
        super();
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d("JOB", "onStartJob - periodic job.");

        try {
            sendDepartureVisionPings();
            sendTimerEvent();
        } catch(Exception e) {
          e.printStackTrace();
        } finally {
            String time = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(Config.POLLING_TIME, ConfigDefault.POLLING_TIME);
            int int_time = 30000;
            try { int_time = Integer.parseInt(time); } catch(Exception e) {}
            int_time = Math.max(10000, int_time);
            Utils.scheduleJob(this.getApplicationContext(), DepartureVisionJobService.class, int_time, false);
            jobFinished(jobParameters, true);
            Log.d("JOB", "onStartJob - periodic job, complete " + time);
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
        Log.d("JOB", "sending " + NotificationValues.BROADCAT_PERIODIC_TIMER);
    }
    public void sendDepartureVisionPings() {
        Intent intent = new Intent(NotificationValues.BROADCAT_SEND_DEPARTURE_VISION_PING);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d("JOB", "sending " + NotificationValues.BROADCAT_SEND_DEPARTURE_VISION_PING);
    }
}

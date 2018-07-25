package com.smartdeviceny.tabbled2;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class DepartureVisionJobService extends JobService {

    public DepartureVisionJobService() {
        super();
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d("JOB", "onStartJob - periodic job.");

        sendDepartureVisionPings();
        sendTimerEvent();
        jobFinished(jobParameters, true);
        Log.d("JOB", "onStartJob - periodic job, complete");
        return true; // let the system know we have no job running ..
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d("JOB", "onStopJob - done");
        return true;
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

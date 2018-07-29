package com.smartdeviceny.tabbled2;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.smartdeviceny.tabbled2.utils.Utils;

public class UpdateCheckerJobService extends JobService {

    public UpdateCheckerJobService() {
        super();
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d("UPDJOB", "onStartJob - periodic job.");
        try {
            sendCheckForUpdate();
        } catch(Exception e) {
          e.printStackTrace();
        } finally {
            jobFinished(jobParameters, false);
            Log.d("UPDJOB", "onStartJob - periodic job, complete");
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
        Log.d("UPDJOB", "sending " + NotificationValues.BROADCAT_CHECK_FOR_UPDATE);
    }
}

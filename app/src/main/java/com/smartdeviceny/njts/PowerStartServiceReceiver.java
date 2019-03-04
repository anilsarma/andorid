package com.smartdeviceny.njts;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PowerStartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        schdeulJob(context.getApplicationContext());
    }


    public static void schdeulJob(Context context) {
        Log.d("PWR", "Scheduling test job.");
        // schedule a job for upgrade check
        long ms_frequency = 1 * 60 * 1000;
        ComponentName serviceComponent = new ComponentName(context, UpdateCheckerJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(2, serviceComponent);
        builder.setPeriodic(ms_frequency);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network wifi
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(true);
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        if(jobScheduler.schedule(builder.build()) <= 0) {
            Log.e("PWR", "error: Some error while scheduling the job");
        }
        else {
            Log.d("PWR", "job scheduled " + ms_frequency);
        }
    }
}
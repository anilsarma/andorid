package com.smartdeviceny.tabbled2;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class DepartureVisionJobService extends JobService {
    SystemService systemService;
    boolean mIsBound= false;

    JobParameters params;

    public DepartureVisionJobService() {
        super();
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d("JOB", "onStartJob - periodic job.");
        Toast.makeText(getApplicationContext(), "Job Starting", Toast.LENGTH_SHORT).show();
        params = jobParameters;
        mIsBound=false;
        systemService=null;
        //startService(new Intent(this, SystemService.class));
        doBindService();
        return true; // let the system know we have some job running ..
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.d("JOB", "onStopJob - done");
        return true;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            systemService = ((RemoteBinder)service).getService();
            // check if we need to reconnect.
            systemService.sendDepartureVisionPings();
            systemService.sendTimerEvent();
            doUnbindService();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            systemService = null;
            //Log.d("JOB", "SystemService disconnected");

        }
    };

    void doBindService() {
        if (!mIsBound) {
            //Log.d("JOB", "SystemService binding.");
            bindService(new Intent(this, SystemService.class), mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }
    }

    void doUnbindService() {
        if (mIsBound) {
            //Log.d("JOB", "SystemService doUnbindService.");
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            unbindService(mConnection);
            jobFinished(params, true);
            mIsBound = false;
            systemService=null;
            //textStatus.setText("Unbinding.");
        }
    }

}

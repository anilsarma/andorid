package com.example.asarma.helloworld;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.asarma.helloworld.utils.Utils;

/**
 * JobService to be scheduled by the JobScheduler.
 * start another service
 */
public class TestJobService extends JobService {

	@Override
	public boolean onStartJob(JobParameters params) {
		Log.d("JOB", "onStartJob scheduled job just ran, will check for status un bind passes");
		mConnection = new LocalServiceConnection(this, params);
		bindService(new Intent(this, SystemService.class), mConnection, Context.BIND_AUTO_CREATE);
		//jobFinished(params, true);
		return true;
	}

	@Override
	public boolean onStopJob(JobParameters params) {
		Log.d("JOB", "onStartJob scheduled job just ran, will check for status un bind passes");
		unbindService(mConnection);
		mConnection  = null;
		return true;
	}

	private ServiceConnection mConnection =  null;

	class LocalServiceConnection implements ServiceConnection {
		TestJobService job;
		JobParameters params;
		LocalServiceConnection(TestJobService job, JobParameters params) {
			this.job  = job;
			this.params = params;
		}
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.d("JOB", "TestJobService onServiceConnected");
			SystemService systemService = ((RemoteBinder)service).getService();
			systemService.checkForUpdate();
            job.jobFinished(params, true);
            unbindService(mConnection);
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.d("JOB", "TestJobService onServiceDisconnected");

		}
	}

}

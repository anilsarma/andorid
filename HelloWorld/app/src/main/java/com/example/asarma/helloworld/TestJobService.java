package com.example.asarma.helloworld;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.backup.SharedPreferencesBackupHelper;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.asarma.helloworld.utils.ConfigUtils;
import com.example.asarma.helloworld.utils.SQLHelper;
import com.example.asarma.helloworld.utils.Utils;
import com.example.asarma.helloworld.values.Config;
import com.example.asarma.helloworld.values.ConfigDefault;

import junit.framework.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * JobService to be scheduled by the JobScheduler.
 * start another service
 */
public class TestJobService extends JobService {
	SharedPreferences config;
	SimpleDateFormat printFormat = new SimpleDateFormat("hh:mm a");
	@Override
	public boolean onStartJob(JobParameters params) {
		config = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		Log.d("JOB", "onStartJob scheduled job just ran, will check for status un bind passes");
		try {
            mConnection = new LocalServiceConnection(this, params);
            bindService(new Intent(this, SystemService.class), mConnection, Context.BIND_AUTO_CREATE);
        } catch(Exception e) {
		    Log.e("JOB", "failed to bind to service " + e.getMessage());
            jobFinished(params, true);
        }
		//jobFinished(params, true);
		return true;
	}

	@Override
	public boolean onStopJob(JobParameters params) {
		Log.d("JOB", "onStartJob scheduled job just ran, will check for status un bind passes");
		if(mConnection!=null){
		    unbindService(mConnection);mConnection  = null;
        }
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
			//systemService.sendMessage();


			// query the next time.
			String start = ConfigUtils.getConfig(config, Config.START_STATION, ConfigDefault.START_STATION);
			String stop = ConfigUtils.getConfig(config, Config.STOP_STATION, ConfigDefault.STOP_STATION);

			ArrayList<SystemService.Route > routes = systemService.getRoutes(start, stop, null, 1);
			Date now = new Date();
			int index =0;
			systemService.schdeuleDepartureVision( ConfigUtils.getConfig(config, Config.DV_STATION, ConfigDefault.DV_STATION), 30000);
			HashMap<String, SystemService.DepartureVisionData> dvmap = systemService.getCachedDepartureVisionStatus_byTrip();
			for(SystemService.Route rt:routes ) {
				if( rt.departure_time_as_date.getTime() < now.getTime()) {
					continue;
				}
				SystemService.DepartureVisionData dv = dvmap.get(rt.block_id);
				if( dv !=null  && dv.station != rt.station_code) {
					dv = null;
				}
				Log.d("JOB", "route:" + rt.block_id + " " + rt.departure_time_as_date + " " + rt.favorite );

				String msg = "NJT #" + rt.block_id;
				if( dv !=null && !dv.track.isEmpty() ) {
					msg += " track#" + dv.track;
				}
				msg += " " + printFormat.format(rt.departure_time_as_date) ;
				String  msg_next = "From "  + rt.from + " " + rt.favorite;
				NotificationCompat.Builder  builder = Utils.makeNotificationBuilder(TestJobService.this, "NJTS", msg, msg_next);
				// goes on the header will show as <appname> - Next Train
				builder.setSubText("Next Train");

				Notification notification = builder.build();
				notification.flags |=Notification.FLAG_NO_CLEAR;

				//Utils.notify_user(TestJobService.this, "NJTS",  "NJTS", msg + new Date(), 12 + index);
				Utils.notify(TestJobService.this, builder, 12 + index);
				index ++;
				if( index > 3) {
					break;
				}
				if( (rt.departure_time_as_date.getTime() - now.getTime() ) > 30 * 60 * 1000) {
					break;
				}

			}
			NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			while(index < 3) {
				notificationManager.cancel(12 + index);
				index ++;
			}

			try {

            } finally {
                job.jobFinished(params, true);
                unbindService(mConnection);mConnection=null;

                long tp = 1* 1000 * 60; // 5 minutes in milli
                long epoch = now.getTime() + tp;
                epoch = (epoch/tp) * tp;
                long next_beat = epoch - now.getTime();
                Log.d("JOB", "Next Beat at " + next_beat);
				Utils.scheduleJob(TestJobService.this.getApplicationContext(), TestJobService.class, (int)next_beat, false);
            }
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.d("JOB", "TestJobService onServiceDisconnected");
		}
	}

}

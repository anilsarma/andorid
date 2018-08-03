package com.example.asarma.helloworld;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.asarma.helloworld.utils.ConfigUtils;
import com.example.asarma.helloworld.utils.Utils;
import com.example.asarma.helloworld.values.Config;
import com.example.asarma.helloworld.values.ConfigDefault;
import com.example.asarma.helloworld.values.NotificationValues;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * JobService to be scheduled by the JobScheduler.
 * start another service
 */
public class TestJobService extends JobService {
	SharedPreferences config;
	SimpleDateFormat printFormat = new SimpleDateFormat("hh:mm a");
    JobParameters params;
    SystemService systemService;
    SharedPreferences myConfig;

	@Override
	public boolean onStartJob(JobParameters params) {
        this.params = params;
        IntentFilter filter = new IntentFilter();
        filter.addAction(NotificationValues.BROADCAT_DEPARTURE_VISION_UPDATED);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReciver, filter);

		config = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		myConfig = getSharedPreferences( "my.job.service.pref", MODE_PRIVATE);
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

	void updateNotification() {
		if( systemService == null ) {
			return;
		}
		Set<String> data = new HashSet<>();
		data = myConfig.getStringSet("notifications", data);
		data = new HashSet<>(data); // make a copy.

		// query the next time.
		String start = ConfigUtils.getConfig(config, Config.START_STATION, ConfigDefault.START_STATION);
		String stop = ConfigUtils.getConfig(config, Config.STOP_STATION, ConfigDefault.STOP_STATION);

		ArrayList<SystemService.Route > routes = systemService.getRoutes(start, stop, null, 1);
		Date now = new Date();
		int index =0;

		HashMap<String, SystemService.DepartureVisionData> dvmap = systemService.getCachedDepartureVisionStatus_byTrip();
//		for(String key: dvmap.keySet()) {
//			SystemService.DepartureVisionData dv = dvmap.get(key);
//			Log.d("JOB", "DV " + dv.block_id +  " " + key + " " + dv.track + " " + dv.time + " " + dv.station + "  " );
//		}
		Log.d("JOB", "Total depoarture vision " + dvmap.size());
		StringBuffer buf = new StringBuffer();
		for(SystemService.Route rt:routes ) {
			if( rt.departure_time_as_date.getTime()+60000 < now.getTime()) {
				continue;
			}
			SystemService.DepartureVisionData dv = dvmap.get(rt.block_id);
			if( dv !=null  && !dv.station.equals(rt.station_code)) {
				dv = null;
			}
			// time in the past and we don't have a track status why bother, it is already gone.
			if( dv == null && rt.departure_time_as_date.getTime() < now.getTime() ) {
				continue;
			}
			Log.d("JOB", "route:" + rt.block_id + " " + rt.departure_time_as_date + " " + rt.favorite  + " " + dv);

			String msg = rt.station_code + " #" + rt.block_id + " " + printFormat.format(rt.departure_time_as_date) ;
			if( dv !=null && !dv.track.isEmpty() ) {
				msg += " track#" + dv.track;
			}
			if(dv !=null ) {
				String key = dv.block_id + ":" + dv.station;
				if (data.contains(key)) {
					// we already published this should we publish a new entry ??
				}
			}

			//String  msg_next = "From "  + rt.station_code + " " + rt.favorite;
			if(index>0) {
				buf.append("\n");
			}
			buf.append(msg);
//			NotificationCompat.Builder  builder = Utils.makeNotificationBuilder(TestJobService.this, "NJTS", msg, msg_next);
//			// goes on the header will show as <appname> - Next Train
//			builder.setSubText("Next Train");
//			Notification notification = builder.build();


//			notification.flags |=Notification.FLAG_NO_CLEAR;
//
//			//Utils.notify_user(TestJobService.this, "NJTS",  "NJTS", msg + new Date(), 12 + index);
//			Utils.notify(TestJobService.this, builder, 12 + index);
			index ++;
			if( index > 3) {
				break;
			}
			if( (rt.departure_time_as_date.getTime() - now.getTime() ) > 30 * 60 * 1000) {
				break;
			}
		}

		{
			String msg = buf.toString();
			if(!msg.isEmpty()) {
				NotificationCompat.Builder  builder = Utils.makeNotificationBuilder(TestJobService.this, "NJTS", "NJ Transit upcoming trains", "Details");
				// goes on the header will show as <appname> - Next Train
				builder.setSubText("Next Train");
				builder.setStyle(new NotificationCompat.BigTextStyle().bigText("Train Details\n" + msg));
				Notification notification = builder.build();
				notification.flags |=Notification.FLAG_NO_CLEAR;
				Utils.notify(TestJobService.this, builder, 120);
			}
		}
		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		while(index < 3) {
			notificationManager.cancel(12);
			index ++;
		}

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
			systemService = ((RemoteBinder)service).getService();

			boolean scheduled = systemService._getDepartureVision( ConfigUtils.getConfig(config, Config.DV_STATION, ConfigDefault.DV_STATION), 30000);

			try {

            } finally {

				// some means of timeout.
				new AsyncTask<Integer, Integer, String>( ) {
					@Override
					protected String doInBackground(Integer ... param) {
						try {
							Thread.sleep(6000);
							scheduleNextJob(params);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						return "";
					}
				}.execute(12);


//                job.jobFinished(params, true);
//                unbindService(mConnection);mConnection=null;
//
//                long tp = 1* 1000 * 60; // 5 minutes in milli
//                long epoch = now.getTime() + tp;
//                epoch = (epoch/tp) * tp;
//                long next_beat = epoch - now.getTime();
//                Log.d("JOB", "Next Beat at " + next_beat);
//				Utils.scheduleJob(TestJobService.this.getApplicationContext(), TestJobService.class, (int)next_beat, false);
            }
		}

		public void onServiceDisconnected(ComponentName className) {
			Log.d("JOB", "TestJobService onServiceDisconnected");
		}
	}

	void scheduleNextJob(@Nullable  JobParameters params) {
	    Date now = new Date();
	    if( params == null  ) {
	        params = this.params;
        }
        jobFinished(params, true);
	    if(mConnection!=null) {
			unbindService(mConnection);
			mConnection = null;
		}

        long tp = 1* 1000 * 60; // 5 minutes in milli
        long epoch = now.getTime() + tp;
        epoch = (epoch/tp) * tp;
        long next_beat = epoch - now.getTime();
        Log.d("JOB", "Next Beat at " + next_beat);
        Utils.scheduleJob(TestJobService.this.getApplicationContext(), TestJobService.class, (int)next_beat, false);
    }

    LocalBcstReceiver broadcastReciver = new LocalBcstReceiver();
    public class LocalBcstReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            //Log.d("MAIN", "onReceive " + intent.getAction());
            if (intent.getAction().equals(NotificationValues.BROADCAT_DEPARTURE_VISION_UPDATED )) {
            	updateNotification();
                scheduleNextJob(null);
            }
        }
    }


}

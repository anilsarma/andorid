package com.smartdeviceny.njts;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class PowerStartService extends Service {
    public PowerStartService()
    {}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PowerStartServiceReceiver.schdeulJob(getApplicationContext());
        return super.onStartCommand(intent, flags, startId);
    }
}
package com.smartdeviceny.tabbled2;

import android.os.Binder;

public class RemoteBinder extends Binder {
    SystemService service;

    public RemoteBinder(SystemService serivce){
        this.service = serivce;
    }
    public SystemService getService() {
        return service;
    }


}

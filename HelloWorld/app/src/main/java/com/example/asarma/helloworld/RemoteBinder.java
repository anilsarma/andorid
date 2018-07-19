package com.example.asarma.helloworld;

import android.os.Binder;

public class RemoteBinder extends Binder {
    MessageService service;

    public RemoteBinder(MessageService serivce){
        this.service = serivce;
    }
    public MessageService getService() {
        return service;
    }


}

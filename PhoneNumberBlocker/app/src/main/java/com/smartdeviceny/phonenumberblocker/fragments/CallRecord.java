package com.smartdeviceny.phonenumberblocker.fragments;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CallRecord {
    public SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
    public String number="";
    public Date date=new Date();
    public String comment = "";
    public int count=0;

    public CallRecord(String number, Date date, String comment, int count) {
        this.number = number;
        this.date = date;
        this.comment = comment;
        this.count = count;
    }

    public CallRecord(String tokens) {
        tokens = tokens.replace("%semicolon%", ";");
        String t[] = tokens.split(";");
        if(t.length>0) {
            this.number = t[0];
        }
        if(t.length>1) {
            try {this.date = dateFormat.parse(t[1]);} catch (Exception e){}
        }
        if(t.length>2) {
            this.comment = t[2];
        }
        if(t.length>3) {
            try {this.count = Integer.parseInt(t[3]);} catch (Exception e){}
        }
    }

    public String toString() {
        this.number = this.number.replace(";", "%semicolon%");
        this.comment = this.comment.replace(";", "%semicolon%");
        return this.number + ";" + dateFormat.format(this.date) + ";" + this.comment + ";" + this.count;
    }



}

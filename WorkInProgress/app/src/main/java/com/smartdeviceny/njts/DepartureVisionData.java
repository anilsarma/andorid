package com.smartdeviceny.njts;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class DepartureVisionData {

    @Persist
    public String tableTime;  // in 8:23 AM
    @Persist
    public String header = "";
    @Persist
    public String time = "";
    @Persist
    public String to = "";
    @Persist
    public String track = "";
    @Persist
    public String line = "";
    @Persist
    public String status = "";
    @Persist
    public String block_id = "";
    @Persist
    public String station_code = "";
    @Persist
    public Date createTime = new Date(); // time this object was created
    @Persist
    public boolean stale = false;
    @Persist(state = Persist.State.NO)
    public boolean favorite = false;
    @Persist
    public ArrayList<String> messages = new ArrayList<>();
    public DepartureVisionData() {
    }

    public DepartureVisionData(HashMap<String, Object> data) {
        time = data.get("time").toString();
        to = data.get("to").toString();
        track = data.get("track").toString();
        line = data.get("line").toString();
        status = data.get("status").toString();
        block_id = data.get("train").toString();
        station_code = data.get("station").toString();
        favorite = false;
        header = " " + createTime + " " + to;
        createTime = new Date();
    }


    public DepartureVisionData clone() {
        DepartureVisionData obj = new DepartureVisionData();
        // TODO not sure how to clone strings ..
        obj.time = "" + this.time;
        obj.to = "" + this.to;
        obj.track = "" + this.track;
        obj.line = "" + this.line;
        obj.status = "" + this.status;
        obj.block_id = "" + this.block_id;
        obj.station_code = "" + this.station_code;
        obj.favorite = this.favorite;
        obj.createTime = this.createTime;
        obj.header = this.header;

        return obj;
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("time=" + time);
        str.append(" to=" + to);
        str.append(" track=" + track);
        str.append(" line=" + line);
        str.append(" status=" + status);
        str.append(" block_id=" + block_id);
        str.append(" station=" + station_code);
        str.append(" favorite=" + favorite);
        str.append(" createTime=" + createTime);
        str.append(" header=" + header);
        str.append(" messages=" + messages);
        return str.toString();
    }


}

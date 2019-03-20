package com.smartdeviceny.workinprogress;

import java.util.Date;

import androidx.annotation.NonNull;

public class RailAlertDetails {
    String long_name;
    String short_code;
    boolean alert;
    String alertText;
    public long   time;
    @NonNull
    @Override
    public String toString() {
        return getTimeDate() + " [ServiceStatus " + long_name + "(" + short_code + ") " + alertText + "]";
    }

    public RailAlertDetails(String long_name, String short_code, boolean alert, String text) {
        this.long_name = long_name;
        this.short_code = short_code;
        this.alert = alert;
        this.alertText = text;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }
    public Date getTimeDate() {
        return new Date(time);
    }

    public String getLong_name() {
        return long_name;
    }

    public String getShort_code() {
        return short_code;
    }

    public boolean isAlert() {
        return alert;
    }

    public String getAlertText() {
        return alertText;
    }

    public void setLong_name(String long_name) {
        this.long_name = long_name;
    }

    public void setShort_code(String short_code) {
        this.short_code = short_code;
    }

    public void setAlert(boolean alert) {
        this.alert = alert;
    }

    public void setAlertText(String alertText) {
        this.alertText = alertText;
    }


}

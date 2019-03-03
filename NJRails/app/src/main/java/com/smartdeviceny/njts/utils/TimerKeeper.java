package com.smartdeviceny.njts.utils;

import android.support.annotation.Nullable;
import android.text.format.Time;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class TimerKeeper {
    class TimerKeeperRecord {
        String key;
        boolean active = false;
        Date lastRequestTime = Utils.adddays(new Date(), -10);
        int pending = 0;

        TimerKeeperRecord(String key) {
            this.key = key;
        }
    }

    HashMap<String, TimerKeeperRecord> entries = new HashMap<>();

    public synchronized ArrayList<String> getActiveStations() {
        ArrayList<String> activeStations = new ArrayList<>();
        for (TimerKeeperRecord rec : entries.values()) {
            if (rec.active) {
                activeStations.add(rec.key);
            }
        }
        return activeStations;
    }

    public synchronized Date getLastRequestTime(String key) {
        TimerKeeperRecord rec = entries.get(key);
        if (rec == null) {
            return Utils.adddays(new Date(), -10); // some large time in the past
        }
        return rec.lastRequestTime;
    }

    public synchronized int getPending(String key) {
        TimerKeeperRecord rec = entries.get(key);
        if (rec == null) {
            return 0;
        }
        return rec.pending;
    }

    public synchronized int updatePending(String key, int num, @Nullable Boolean active) {
        TimerKeeperRecord rec = entries.get(key);
        if (rec == null) {
            rec = new TimerKeeperRecord(key);
            entries.put(key, rec);
            if (active != null && active) {
                this.active(key);
            }
        }
        rec.pending += num;
        rec.lastRequestTime = new Date(); // just now,
        return rec.pending;
    }

    public synchronized void updateTime(String key, @Nullable Boolean active) {
        TimerKeeperRecord rec = entries.get(key);
        if (rec == null) {
            rec = new TimerKeeperRecord(key);
            entries.put(key, rec);
            if (active != null && active) {
                this.active(key);
            }
        }
        rec.lastRequestTime = new Date();
    }

    public synchronized void active(String key) {
        for (TimerKeeperRecord rec : entries.values()) {
            rec.active = false;
            if (rec.key.equals(key)) {
                rec.active = true;
            }
        }
    }

}

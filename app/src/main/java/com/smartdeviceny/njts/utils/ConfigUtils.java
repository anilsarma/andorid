package com.smartdeviceny.njts.utils;

import android.content.SharedPreferences;

public class ConfigUtils {

    static public void setupConfigDefaults(SharedPreferences config, String name, String defaultValue) {
        String value = config.getString(name, "");
        if( value.isEmpty()) {
            SharedPreferences.Editor editor  = config.edit();
            editor.putString(name, defaultValue);
            editor.commit();
        }
    }
    static public String getConfig(SharedPreferences config, String name, String defaultValue) {
        return config.getString(name, defaultValue);
    }
    static public void setConfig(SharedPreferences config, String name, String value) {
        SharedPreferences.Editor editor  = config.edit();
        editor.putString(name, value);
        editor.commit();
    }
}

package com.example.asarma.helloworld;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.util.Set;

public class Config {
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_SET_INT_VALUE = 3;
    static final int MSG_SET_STRING_VALUE = 4;

    Context context;

    public Config(Context context)
    {
        this.context = context;
    }
    public void set(String name, int value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(name, value);
        editor.commit();
    }

    public void set(String name, String value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(name, value);
        editor.commit();
    }
    public void set(String name, double value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putFloat(name, (float)value);
        editor.commit();
    }
    public void set(String name, Set<String> values) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putStringSet(name, values);
        editor.commit();
    }

    public int getInt(String name, @Nullable int value) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(name, value);
    }
    public String getString(String name, @Nullable String value) {
        if(value ==null) {
            value = "";
        }
        return PreferenceManager.getDefaultSharedPreferences(context).getString(name, value);
    }
    public double getFloat(String name, @Nullable double value) {
        return (double) PreferenceManager.getDefaultSharedPreferences(context).getFloat(name, (float)value);
    }

    public Set<String> getStringSet(String name, @Nullable Set<String> values) {
        return PreferenceManager.getDefaultSharedPreferences(context).getStringSet(name, values);
    }
}

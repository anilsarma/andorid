package com.smartdeviceny.njts;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.preference.PreferenceManager;
import android.util.Log;

public class NJTSBackupAgent extends BackupAgentHelper {

    static final String PREFS_BACKUP_KEY = "njts_default_pref";

    // Allocate a helper and add it to the backup agent
    @Override
    public void onCreate() {
        Log.d("BACKUP", "created");
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, PreferenceManager.getDefaultSharedPreferencesName(this));
        addHelper(PREFS_BACKUP_KEY, helper);
    }
}

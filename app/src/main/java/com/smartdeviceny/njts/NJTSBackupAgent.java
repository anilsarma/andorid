package com.smartdeviceny.njts;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;

public class NJTSBackupAgent extends BackupAgentHelper {

    static final String PREFS_BACKUP_KEY = "njts_default_pref";
    public NJTSBackupAgent() {
        Log.d("BACKUP", "created");
    }
    // Allocate a helper and add it to the backup agent
    @Override
    public void onCreate() {
        Log.d("BACKUP", "created");
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, PreferenceManager.getDefaultSharedPreferencesName(this));
        addHelper(PREFS_BACKUP_KEY, helper);
    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
        super.onBackup(oldState, data, newState);
        Log.i("backup", "OnBackUp");
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        super.onRestore(data, appVersionCode, newState);
        Log.i("restore", "onRestore");
    }

    @Override
    public void onRestoreFinished() {
        super.onRestoreFinished();
        Log.i("restore", "onRestoreFinished");
    }
}

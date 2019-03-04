package com.smartdeviceny.njts.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;

// the main purpose of this is to check for new version in the remote
// server.
public class UtilsDBVerCheck {

    static public SQLiteLocalDatabase getSQLDatabase(Context appContext, File dbFilePath) {
        if (!dbFilePath.exists()) {
            return null;
        }
        SQLiteLocalDatabase sql = new SQLiteLocalDatabase(appContext, dbFilePath.getName(), dbFilePath.getParent());
        sql.getWritableDatabase();
        try {
            sql.getWritableDatabase(); // force and open
        } catch (Exception e) {
            Log.d("SQL", "get routes failed need to download");
            sql.close();
            Utils.delete(dbFilePath);
            sql = null;
        }
        return sql;
    }

    // extract the specified name to a temp file.
    public static File createTempFile(File downloadedZipFile, File outputDir, String nameToExtract) {
        File tmpFilename = null;
        try {
            tmpFilename = File.createTempFile(Utils.getBasename(nameToExtract), "." + Utils.getExtension(nameToExtract), outputDir);
            ZipInputStream zis = Utils.getFileFromZip(new FileInputStream(downloadedZipFile), nameToExtract);
            Utils.writeExtractedFileToDisk(zis, new FileOutputStream(tmpFilename));
        } catch (IOException e) {
        }
        return tmpFilename;
    }

    static public boolean matchDBVersion(SQLiteLocalDatabase sql, String version_str) {
        try {
            if (sql == null) {
                return false;
            }
            if (version_str.equals("")) {
                return false; // bad database or not upgraded.
            }
            if (SqlUtils.check_if_user_pref_exists(sql.getWritableDatabase())) {
                // chck the database.
                String db_ver = SqlUtils.get_user_pref_value(sql.getWritableDatabase(), "version", "");
                if (db_ver.equals(version_str)) {
                    return true;
                }
            }
            return false; // different version or db is not available.
        } catch (Exception e) {
            Log.e("DBU", "matchDBVersion failed" + e.getMessage());
        }
        return false;
    }

    static public String getDBVersion(SQLiteLocalDatabase sql) {
        try {
            if (sql == null) {
                return "";
            }
            return SqlUtils.get_user_pref_value(sql.getWritableDatabase(), "version", "");
        } catch (Exception e) {
            Log.e("DBU", "matchDBVersion failed" + e.getMessage());
        }
        return "";

    }


}

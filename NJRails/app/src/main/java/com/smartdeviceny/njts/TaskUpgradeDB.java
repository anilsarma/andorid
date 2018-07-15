package com.smartdeviceny.njts;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Anil Sarma on 11/24/2017.
 */
//https://raw.githubusercontent.com/anilsarma/misc/master/njt/version.txt
public class TaskUpgradeDB extends AsyncTask<String, Integer, String> {
    Context context;
    ITaskUpgradeComplete callback;
    private ProgressDialog dialog;
    SQLHelper dbHelper;
    public TaskUpgradeDB(Context context, ITaskUpgradeComplete callback) {

        this.context = context;
        this.callback = callback;
        dialog= new ProgressDialog(context);
        if (dbHelper == null) {
            dbHelper = new SQLHelper(context);
        }
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Toast.makeText(context, (String) "creating tables", Toast.LENGTH_SHORT).show();
        dialog.setMessage("Updating Schedule database");
        dialog.show();
    }

    @Override
    protected String doInBackground(String... sUrl) {
        SQLiteDatabase db= dbHelper.getWritableDatabase();
        dbHelper.update_tables(db, false);
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(context, (String) "database tables created", Toast.LENGTH_SHORT).show();
        dialog.dismiss();
        if(callback != null) {
            callback.onUpgradeTaskComplete();
        }

    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
    }



}

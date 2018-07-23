package com.example.asarma.helloworld;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * Created by Anil Sarma on 11/24/2017.
 */
//https://raw.githubusercontent.com/anilsarma/misc/master/njt/version.txt
public class UpgradeProcessDialog extends AsyncTask<String, Integer, String> {
    Context context;
    private ProgressDialog dialog;
    public UpgradeProcessDialog(Context context) {
        this.context = context;
        dialog= new ProgressDialog(context);
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
        // do some work in the background.
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(context, (String) "database tables created", Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
    }



}

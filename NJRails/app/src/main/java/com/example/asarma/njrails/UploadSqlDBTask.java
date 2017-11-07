package com.example.asarma.njrails;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UploadSqlDBTask extends AsyncTask<Integer, Integer, Long> {

    SQLHelper dbHelper=null;
    MainActivityFragment parent;
    View view;
    public UploadSqlDBTask(View view, MainActivityFragment parent)
    {
        this.parent = parent;
        this.view = view;
    }
    ArrayList<HashMap<String, Object>> result;
    protected Long doInBackground(Integer... value) {
        dbHelper = new SQLHelper(this.view.getContext());
        SQLiteDatabase db= dbHelper.getWritableDatabase();

        try {
            if ( SQLHelper.check_table(db, "trips")== 0 ) {
                Toast.makeText(this.view.getContext(),(String)"updating Database Tables",
                        Toast.LENGTH_SHORT).show();
                RailHelper.create_tables(db);
                dbHelper.update_tables(db);
            }
        }catch (Exception e) {
            Toast.makeText(this.view.getContext(),(String)"Creating Database Tables",
                    Toast.LENGTH_SHORT).show();
            RailHelper.create_tables(db);
            dbHelper.update_tables(db);
        }
        return new Long(0);
    }

    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }

    protected void onPostExecute(Long xx) {
        parent.sqlUpdateComplete();
    }
}

package com.smartdeviceny.njts;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class UploadSqlDBTask extends AsyncTask<Integer, Integer, Long> {

    SQLHelper dbHelper=null;
    MainActivityFragment parent;
    View view;
    boolean force = false;
    public UploadSqlDBTask(View view, MainActivityFragment parent, boolean force)
    {
        this.parent = parent;
        this.view = view;
        this.force = force;
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
                dbHelper.update_tables(db, force);
            }
        }catch (Exception e) {
            Toast.makeText(this.view.getContext(),(String)"Creating Database Tables",
                    Toast.LENGTH_SHORT).show();
            RailHelper.create_tables(db);
            dbHelper.update_tables(db, force);
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

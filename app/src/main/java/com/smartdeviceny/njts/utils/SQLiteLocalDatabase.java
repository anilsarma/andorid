package com.smartdeviceny.njts.utils;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipInputStream;

public class SQLiteLocalDatabase extends SQLiteOpenHelper {
    private String databaseDir =null; // "/data/data/"+BuildConfig.APPLICATION_ID+"/databases/";
    private String databaseFullPath =null;
    private final Context mContext;
    private SQLiteDatabase myDataBase;

    // we will deal with versions later.
    public SQLiteLocalDatabase(Context context, String name, @Nullable String dbDir) {
        super(context, name, null, 1);
        mContext = context;
        //databaseName = name;
        databaseDir = context.getApplicationInfo().dataDir;
        if( dbDir != null ) {
            databaseDir = dbDir;
        }
        databaseFullPath = databaseDir + File.separator + getDatabaseName();
        //System.out.println("checking for file " + databaseFullPath);
        if (checkdatabase(new File(databaseFullPath))) {
            //System.out.println(databaseFullPath + "length " + new File(databaseFullPath).length());
            opendatabase();
        } else {
            try {
                createdatabase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void reopen(InputStream src) throws IOException
    {
        if ( myDataBase != null && myDataBase.isOpen()) {
            try {myDataBase.close();} catch(Exception e){}
        }
        // update from source if necessary
        //InputStream src = mContext.getAssets().open("databases/rail_data_db.zip");
        ZipInputStream zip = getFileFromZip(src);
        OutputStream dest = new FileOutputStream(databaseFullPath);
        copyStream( zip, dest);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // nothing to do
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // we will not be upgrading anything.
    }

    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {
        if (myDataBase != null && myDataBase.isOpen()) {
            return myDataBase;  // The database is already open for business
        }
        return getWritableDatabase();
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        if (myDataBase != null && myDataBase.isOpen() && !myDataBase.isReadOnly()) {
            return myDataBase;
        }
        boolean success = false;
        SQLiteDatabase db = null;

        try {
            db = opendatabase();
            onOpen(db);
            success = true;
            return db;
        } finally {
            if (success) {
                // close the old db that was opened.
                if (myDataBase != null) {
                    try {myDataBase.close();} catch (Exception e) {}
                }
                myDataBase = db;
            } else {
                if (db != null) db.close();
            }
        }
    }

    private void copydatabase() throws IOException {
        InputStream src = mContext.getAssets().open(getDatabaseName());
        OutputStream dest = new FileOutputStream(databaseFullPath);
        copyStream( src, dest);
    }

    public SQLiteDatabase opendatabase() throws SQLException {
        myDataBase = SQLiteDatabase.openDatabase(databaseFullPath, null, SQLiteDatabase.OPEN_READWRITE);
        return myDataBase;
    }
    private void createdatabase() throws IOException {
        if (!checkdatabase(new File(databaseFullPath))) {
            this.getReadableDatabase();// ?? why do we need this ??
            try {
                copydatabase();
                opendatabase(); // we should be able to open it now.
            } catch (IOException e) {
                e.printStackTrace();
                throw new IOException("Error copying database during createdatabase");
            }
        }
    }
    // Utility
    public static boolean checkdatabase(File dbFile)
    {
        return  dbFile.exists();
    }
    public static void copyStream(InputStream from, OutputStream to) throws IOException {
        // transfer byte to inputfile to outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = from.read(buffer)) > 0) {
            to.write(buffer, 0, length);
        }
        to.flush();
        to.close();
        from.close();
    }
    public synchronized void close() {
        if (myDataBase != null) {
            myDataBase.close();
            myDataBase  = null;
        }
        super.close();
    }

    public void copyZipFile(ZipInputStream zip)
    {
        File dbFullNameFile = new File(databaseFullPath);
        File parent = dbFullNameFile.getParentFile();
        if(!parent.exists()) {
            parent.mkdirs();
        }

    }
    public static ZipInputStream getFileFromZip(InputStream zipFileStream) throws IOException {
        return Utils.getFileFromZip(zipFileStream);
//        ZipInputStream zis = new ZipInputStream(zipFileStream);
//        while ((zis.getNextEntry()) != null) {
//            return zis;
//        }
//        return null;
    }
}
package com.example.asarma.njrails;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends FragmentActivity {
    protected  static final boolean FORCE_DOWNLOAD=false;
    protected static final int REQUEST_CODE_SIGN_IN = 0;
    protected static final int REQUEST_CODE_OPEN_ITEM = 1;
    private static final String TAG = "BaseDriveActivity";
    DriveClient mDriveClient;
    DriveResourceClient mDriveResourceClient;
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    DemoCollectionPagerAdapter mDemoCollectionPagerAdapter;
    ViewPager mViewPager;
    SQLHelper dbHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_pager);

        mDemoCollectionPagerAdapter =  new DemoCollectionPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null ) {
            Boolean value = extras.getBoolean("UPGRADE");
            if (value!=null && value) {
                Toast.makeText(MainActivity.this.getApplicationContext(), "Got upgrade intent " + intent, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        final DownloadNJTGitHubFile github = new DownloadNJTGitHubFile(getApplicationContext(), "", "", null);
        File version_upgrade = github.getCacheDir("version_upgrade.txt");
        long diffms = (System.currentTimeMillis() - version_upgrade.lastModified());

        long hours =  0;
        long minutes = 30;
        long seconds = 5;
        //showCustomNotification(null, null);
        if (diffms < (( (hours *60 + minutes) *60  + seconds) *1000) ) {
            if (!FORCE_DOWNLOAD) {
                //Toast.makeText(getApplicationContext(), "Skipping check Modified time is" + diffms, Toast.LENGTH_LONG).show();
                return;
            }
        }

        //Toast.makeText(MainActivity.this.getApplicationContext(), "Modified time is" + diffms,Toast.LENGTH_LONG).show();
        new DownloadNJTGitHubFile(getApplicationContext(), "version.txt", "version_upgrade.txt", new IGitHubDownloadComple() {
            @Override
            public void onDownloadComplete(String filename, File folder, File destination) {
                // download the zip file nao
                MainActivity.this.downloadZipFile();
            }

            @Override
            public void onFailed(String filename) {

            }
        }).execute("");
    }
    private void showCustomNotification(String from, String to){
        final int NOTIFICATION_ID = 1;
        String ns = Context.NOTIFICATION_SERVICE;
        final NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

        //int icon = R.mipmap.ic_launcher;
        //long when = System.currentTimeMillis();
//        Notification notification = new Notification(icon, getString(R.string.app_name), when);
//        notification.flags |= Notification.FLAG_NO_CLEAR; //Do not clear the notification
//        notification.defaults |= Notification.DEFAULT_LIGHTS; // LED
//        notification.defaults |= Notification.DEFAULT_VIBRATE; //Vibration
//        notification.defaults |= Notification.DEFAULT_SOUND; // Sound
        String msg = "upgrade";
        if (from != null  && to != null ) {
            msg = "upgrade required from '" + from + "' to '" + to + "'";
        }
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.app_njs_icon)
                        .setContentTitle("NJRails Upgrade Required")
                        .setTicker("Upgrade (Open to see the info).")
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setContentText(msg);
        // NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent_upgrade = new Intent(this, MainActivity.class);
        intent_upgrade.putExtra("UPGRADE", true ); //If you wan to send data also
        PendingIntent pIntentUpgrade = PendingIntent.getActivity(this, 1000, intent_upgrade, 0);
        PendingIntent pIntentIgnore = PendingIntent.getActivity(this, 1001, new Intent(this, MainActivity.class), 0);
        mBuilder.addAction(R.mipmap.ic_launcher, "Upgrade", pIntentUpgrade)
                .addAction(R.mipmap.ic_launcher, "Ignore", pIntentIgnore)
                //.addAction(R.mipmap.ic_launcher, "Later", pIntent);
        ;
        Notification notification = mBuilder.build();

        notification.flags|= Notification.FLAG_AUTO_CANCEL;
        //notification.defaults |= Notification.DEFAULT_SOUND;

        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    // called multiple times ...
    void downloadZipFile()
    {
        System.out.println("in downloadZipFile");
        if (dbHelper == null) {
            dbHelper = new SQLHelper(getApplicationContext());
        }
        //using this just as a ulitiy functon
        final DownloadNJTGitHubFile utilsNJGitHub = new DownloadNJTGitHubFile(getApplicationContext(), "", "", null);
        final File version = utilsNJGitHub.getCacheDir("version.txt");
        File version_upgrade = utilsNJGitHub.getCacheDir("version_upgrade.txt");
        File rail_data = utilsNJGitHub.getCacheDir("rail_data.zip");

        boolean download=true;

        final String upgrade_version_str = utilsNJGitHub.readFile(version_upgrade);
        final String version_str = utilsNJGitHub.readFile(version);
        try {
            if (version_str.equals(upgrade_version_str)) {
                download = false;
            }
            if(upgrade_version_str.isEmpty()) {
                download = false;
            }
        }
        catch(Exception e) {
        }
        if(FORCE_DOWNLOAD) {
            download = true;
        }

        final File download_complete = utilsNJGitHub.getCacheDir("download_complete.txt");
        if (download) {
            showCustomNotification(version_str, upgrade_version_str);
            if (rail_data.exists()) {
                rail_data.delete();
            }
        }

        // check if the rail_data in archive is good.
        if (rail_data.exists()) {
            if (download_complete.exists()) {
                String s = utilsNJGitHub.readFile(download_complete);
                if (!s.equals(upgrade_version_str)) {
                    rail_data.delete();
                    download = true;
                }
            }
            else {
                // download did not complete
                rail_data.delete();
                download = true;
            }
        }

        // if the rail data file exists, mean that we have successfuly downloaded the rail data file.
        if (rail_data.exists()) {
            //Toast.makeText(MainActivity.this.getApplicationContext(), "no download required of rail_data.zip version:" + version_str + " remote:" + upgrade_version_str, Toast.LENGTH_LONG).show();
            // download_complete
            File destination = utilsNJGitHub.getCacheDir("rail_data.zip");
            File dir = utilsNJGitHub.getCacheDir("nj_rails_cache");
            if (!dir.exists()) {
                dir.mkdir();
            }
            utilsNJGitHub.removeFiles(dir);

            try {
                if (download) {
                    ArrayList<File> o = utilsNJGitHub.unzipfile(destination, dir);
                    String s = "" + o.size();
                    for (String f : dir.list()) {
                        s += " " + f;
                    }
                    Toast.makeText(MainActivity.this.getApplicationContext(), "unzipped " + s, Toast.LENGTH_LONG).show();

                    try {
                        utilsNJGitHub.writeFile(download_complete, upgrade_version_str);

                        SQLiteDatabase db= dbHelper.getWritableDatabase();
                        dbHelper.useAsset=false;
                        Toast.makeText(MainActivity.this.getApplicationContext(), "DB upgrade starting rail_data.zip", Toast.LENGTH_LONG).show();
                        dbHelper.update_tables(db, true);
                        utilsNJGitHub.writeFile(version, upgrade_version_str);
                        Toast.makeText(MainActivity.this.getApplicationContext(), "DB upgrade Complete rail_data.zip", Toast.LENGTH_LONG).show();
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(MainActivity.this.getApplicationContext(), "DB upgrade failed rail_data.zip", Toast.LENGTH_LONG).show();
                    }
                }

            }
            catch (IOException e)
            {
                Toast.makeText(MainActivity.this.getApplicationContext(), "unzipped failed " + e, Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(MainActivity.this.getApplicationContext(), "Starting download of rail_data.zip", Toast.LENGTH_LONG).show();
            new DownloadNJTGitHubFile(getApplicationContext(), "rail_data.zip", "rail_data.zip", new IGitHubDownloadComple() {
                @Override
                public void onDownloadComplete(String filename, File folder, File destination) {
                    Toast.makeText(MainActivity.this.getApplicationContext(), "Download Complete, upgrading rail_data.zip", Toast.LENGTH_LONG).show();
                    try {
                        utilsNJGitHub.writeFile(download_complete, upgrade_version_str);

                        /*unzip the file */
                        File dir = utilsNJGitHub.getCacheDir("nj_rails_cache");
                        if (!dir.exists()) {
                            dir.mkdir();
                        }

                        ArrayList<File> o = utilsNJGitHub.unzipfile(destination, dir);
                        Toast.makeText(MainActivity.this.getApplicationContext(), "total unzipped rail_data.zip " + o.size(), Toast.LENGTH_LONG).show();

                        SQLiteDatabase db= dbHelper.getWritableDatabase();
                        dbHelper.useAsset=false;
                        Toast.makeText(MainActivity.this.getApplicationContext(), "DB2 upgrade starting rail_data.zip", Toast.LENGTH_LONG).show();
                        dbHelper.update_tables(db, true);
                        utilsNJGitHub.writeFile(version, upgrade_version_str);
                        Toast.makeText(MainActivity.this.getApplicationContext(), "DB2 upgrade Complete rail_data.zip", Toast.LENGTH_LONG).show();
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(MainActivity.this.getApplicationContext(), "DB upgrade failed rail_data.zip", Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onFailed(String filename) {

                }
            }).execute("");
        }
    }


    private GoogleSignInClient buildGoogleSignInClient() {
        Scope publicFolder = new Scope("1yc6JGDvqO9BzVa7oAfjFO53pgiTJr9me");
        //publicFolder = Drive.SCOPE_FILE;
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestScopes(publicFolder).build();
        return GoogleSignIn.getClient(this, signInOptions);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode != RESULT_OK) {
                    // Sign-in may fail or be cancelled by the user. For this sample, sign-in is
                    // required and is fatal. For apps where sign-in is optional, handle
                    // appropriately
                    Log.e(TAG, "Sign-in failed.");
                    finish();
                    return;
                }

                Task<GoogleSignInAccount> getAccountTask =   GoogleSignIn.getSignedInAccountFromIntent(data);
                if (getAccountTask.isSuccessful()) {
                    initializeDriveClient(getAccountTask.getResult());

                    Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, "public")).build();

                    Task<MetadataBuffer> buffer = mDriveResourceClient.query(query);
                    buffer.addOnSuccessListener(new OnSuccessListener<MetadataBuffer>() {
                        @Override
                        public void onSuccess(MetadataBuffer metadata) {
                            Toast.makeText(getApplicationContext(), "file good" + metadata.getCount() + " " , Toast.LENGTH_LONG).show();
                        }
                    }) .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle failure...
                            // [START_EXCLUDE]
                            Log.e(TAG, "Error retrieving files", e);
                            Toast.makeText(getApplicationContext(), "fill fialed", Toast.LENGTH_LONG).show();
                            finish();
                            // [END_EXCLUDE]
                        }
                    });


                } else {
                    Log.e(TAG, "Sign-in failed.");
                    finish();
                }
                break;
            case REQUEST_CODE_OPEN_ITEM:

                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initializeDriveClient(GoogleSignInAccount signInAccount) {
        mDriveClient = Drive.getDriveClient(getApplicationContext(), signInAccount);
        mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), signInAccount);
        //    onDriveClientReady();
    }
}
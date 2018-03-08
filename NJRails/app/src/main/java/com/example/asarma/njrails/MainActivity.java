package com.example.asarma.njrails;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
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
import java.util.concurrent.TimeUnit;


public class MainActivity extends FragmentActivity {
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

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mDemoCollectionPagerAdapter =  new DemoCollectionPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);

//
//        try {
//            GoogleDriveTest.main();
//            GoogleSignInClient googleSignInClient = buildGoogleSignInClient();;
//            startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
//
//            GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
//            if ( signInAccount != null ) {
//                initializeDriveClient(signInAccount);
//            }
//            //mDriveClient = Drive.getDriveClient(this, GoogleSignIn.getLastSignedInAccount(this));
//            //mDriveResourceClient = Drive.getDriveResourceClient(this, GoogleSignIn.getLastSignedInAccount(this));
//
//        }  catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        final DownloadNJTGitHubFile github = new DownloadNJTGitHubFile(getApplicationContext(), "", "", null);
        //new DownloadGitHubFile(getApplicationContext(), "trips.txt").execute("");
        File version_upgrade = github.cacheDir("version_upgrade.txt");
        long diffms = (System.currentTimeMillis() - version_upgrade.lastModified());

        long hours =  0;
        long minutes = 0;
        long seconds = 30;

        if (diffms < (( (hours *60 + minutes) *60  + seconds) *1000) ) {
            Toast.makeText(getApplicationContext(), "Skipping check Modified time is" + diffms,Toast.LENGTH_LONG).show();
            return;
        }
        //Toast.makeText(MainActivity.this.getApplicationContext(), "Modified time is" + diffms,Toast.LENGTH_LONG).show();
        new DownloadNJTGitHubFile(getApplicationContext(), "version.txt", "version_upgrade.txt", new IGitHubDownloadComple() {
            @Override
            public void onDownloadComplete(String filename, File folder, File destination) {
                // download the zip file nao
                MainActivity.this.downloadZipFile();
                //Toast.makeText(MainActivity.this.getApplicationContext(), "Download Complete",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailed(String filename) {

            }
        }).execute("");
    }

    void downloadZipFile()
    {
        if (dbHelper == null) {
            dbHelper = new SQLHelper(getApplicationContext());
        }
        final DownloadNJTGitHubFile tmp = new DownloadNJTGitHubFile(getApplicationContext(), "", "", null);
        final File version = tmp.cacheDir("version.txt");
        File version_upgrade = tmp.cacheDir("version_upgrade.txt");
        File rail_data = tmp.cacheDir("rail_data.zip");

        boolean download=true;
        final String upgrade_version_str = tmp.readFile(version_upgrade);
        final String version_str = tmp.readFile(version);
        try {
            if (version_str.equals(upgrade_version_str)) {
                download = false;
            }
        }
        catch(Exception e) {

        }

        final File download_complete = tmp.cacheDir("download_complete.txt");
        if (download) {
            if (rail_data.exists()) {
                if (download_complete.exists()) {
                    String s = tmp.readFile(download_complete);
                    if (s == upgrade_version_str) {

                    }
                    else {
                        rail_data.delete();
                    }
                }
            }
            //rail_data.delete();
        }
        if(download) {
            if (rail_data.exists()) {
                rail_data.delete();
            }
        }
        if (rail_data.exists()) {
            Toast.makeText(MainActivity.this.getApplicationContext(), "no download required of rail_data.zip version:" + version_str + " remote:" + upgrade_version_str, Toast.LENGTH_LONG).show();

            // download_complete
            File destination = tmp.cacheDir("rail_data.zip");
            File dir = tmp.cacheDir("nj_rails_cache");
            if (!dir.exists()) {
                dir.mkdir();
            }
            tmp.removeFiles(dir);

            try {
                if (download) {
                    ArrayList<File> o = tmp.unzipfile(destination, dir);
                    String s = "" + o.size();
                    for (String f : dir.list()) {
                        s += " " + f;
                    }
                    Toast.makeText(MainActivity.this.getApplicationContext(), "unzipped " + s, Toast.LENGTH_LONG).show();

                    try {
                        tmp.writeFile(download_complete, upgrade_version_str);

                        SQLiteDatabase db= dbHelper.getWritableDatabase();
                        dbHelper.useAsset=false;
                        Toast.makeText(MainActivity.this.getApplicationContext(), "DB upgrade starting rail_data.zip", Toast.LENGTH_LONG).show();
                        dbHelper.update_tables(db, false);
                        tmp.writeFile(version, upgrade_version_str);
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
                        tmp.writeFile(download_complete, upgrade_version_str);

                        SQLiteDatabase db= dbHelper.getWritableDatabase();
                        dbHelper.useAsset=false;
                        Toast.makeText(MainActivity.this.getApplicationContext(), "DB2 upgrade starting rail_data.zip", Toast.LENGTH_LONG).show();
                        dbHelper.update_tables(db, false);
                        tmp.writeFile(version, upgrade_version_str);
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
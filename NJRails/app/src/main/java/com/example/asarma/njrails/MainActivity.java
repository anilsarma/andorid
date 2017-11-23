package com.example.asarma.njrails;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.example.asarma.njrails.route.GoogleDriveTest;
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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_pager);

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mDemoCollectionPagerAdapter =  new DemoCollectionPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mDemoCollectionPagerAdapter);


        try {
            GoogleDriveTest.main();
            GoogleSignInClient googleSignInClient = buildGoogleSignInClient();;
            startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);

            GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
            if ( signInAccount != null ) {
                initializeDriveClient(signInAccount);
            }
            //mDriveClient = Drive.getDriveClient(this, GoogleSignIn.getLastSignedInAccount(this));
            //mDriveResourceClient = Drive.getDriveResourceClient(this, GoogleSignIn.getLastSignedInAccount(this));


        }  catch (Exception e) {
            e.printStackTrace();
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
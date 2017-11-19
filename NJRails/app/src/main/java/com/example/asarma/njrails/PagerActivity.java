package com.example.asarma.njrails;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filter;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Task;

import java.io.File;

public class PagerActivity extends FragmentActivity {
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
        mViewPager.postDelayed(new Runnable() {
            @Override
            public void run() {
                mViewPager.setCurrentItem(MainActivityFragment.FIRST_PAGE + 2);
                Toast.makeText(mViewPager.getContext(), "Current Act"+ mViewPager.getCurrentItem(), Toast.LENGTH_LONG).show();
            }
        }, 5000);
       // mViewPager.setCurrentItem(MainActivityFragment.FIRST_PAGE+1);
       // Drive.g
        String id = "1yc6JGDvqO9BzVa7oAfjFO53pgiTJr9me";

        mDriveClient = Drive.getDriveClient(this, GoogleSignIn.getLastSignedInAccount(this));
        mDriveResourceClient = Drive.getDriveResourceClient(this, GoogleSignIn.getLastSignedInAccount(this));


        Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, "public")).build();

        Task<MetadataBuffer> buffer = mDriveResourceClient.query(query);
       //= mDriveResourceClient.getRootFolder().
//        File fileMetadata = new File();
//        fileMetadata.setName("photo.jpg");
//        fileMetadata.setParents(Collections.singletonList(folderId));
//        java.io.File filePath = new java.io.File("files/photo.jpg");
//        FileContent mediaContent = new FileContent("image/jpeg", filePath);
//        File file = driveService.files().create(fileMetadata, mediaContent)
//                .setFields("id, parents")
//                .execute();
//        System.out.println("File ID: " + file.getId());

        //mDriveResourceClient.openFile(file, DriveFile.MODE_READ_ONLY);
    }


}

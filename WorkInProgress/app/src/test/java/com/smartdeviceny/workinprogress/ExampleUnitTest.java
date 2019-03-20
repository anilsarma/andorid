package com.smartdeviceny.workinprogress;

import android.app.Activity;
import android.app.DownloadManager;

import com.smartdeviceny.njts.DownloadFile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.util.Scheduler;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=24)
//@Config(constants = BuildConfig.class)
public class ExampleUnitTest {
    //@Rule
   // public WireMockRule wireMockRule = new WireMockRule(1111);
    public static final int placementID = 1;
    public static final int width = 320;
    public static final int height = 50;
    Activity activity;
    Scheduler uiScheduler, bgScheduler;
    public MockWebServer server;
DownloadFile downloadFile;
    @Before
    public void setUp() {
        Robolectric.getBackgroundThreadScheduler().reset();
        Robolectric.getForegroundThreadScheduler().reset();
        ShadowLog.stream = System.out;
         activity = Robolectric.buildActivity( EmptyActivity.class )
                .create()
                .resume()
                .get();
        //Robolectric.buildActivity(MainActivity.class).create().get();//.start();//.resume().visible().get();
        Shadows.shadowOf(activity).grantPermissions("android.permission.INTERNET");
        server= new MockWebServer();
        try {
            server.start();
            HttpUrl url= server.url("/");
            //UTConstants.REQUEST_BASE_URL_UT_V2 =
            String result = url.toString();
            System.out.println(result);
            //ShadowSettings.set(url.toString());
            //TestResponsesUT.setTestURL(url.toString());
        } catch (IOException e) {
            System.out.print("IOException");
        }


       downloadFile = new DownloadFile(activity.getApplicationContext(), new DownloadFile.Callback() {
           @Override
           public boolean downloadComplete(DownloadFile d, long id, String url, File file) {
               System.out.println("done with download");
               return false;
           }

           @Override
           public void downloadFailed(DownloadFile d, long id, String url) {
               System.out.println("done with downloa failed d");
           }
       });
        bgScheduler = Robolectric.getBackgroundThreadScheduler();
        uiScheduler = Robolectric.getForegroundThreadScheduler();

        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        bgScheduler.pause();
        uiScheduler.pause();
    }


    @After
    public void tearDown() {
        try {
            server.shutdown();
            bgScheduler.reset();
            uiScheduler.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        activity.finish();
    }


    private void scheduleTimerToCheckForTasks() {
        Timer timer = new Timer();
        final int[] counter = {330};
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                counter[0]--;
                if (uiScheduler.areAnyRunnable() || bgScheduler.areAnyRunnable() || counter[0] == 0) {
                    Lock.unpause();
                    this.cancel();
                }
            }
        }, 0, 100);
    }

    public void waitForTasks() {
        scheduleTimerToCheckForTasks();
        Lock.pause();
    }


    @Test
    public void test1CookiesSync() {

        server.enqueue(new MockResponse().setResponseCode(200).setHeader("Set-Cookie", TestResponsesUT.UUID_COOKIE_1).setBody(TestResponsesUT.banner()));

        // do something with download manager.
        DownloadManager.Request request = downloadFile.buildRequest("http://www.google.com", "text.html", "test.html", DownloadManager.Request.NETWORK_WIFI, null);
        downloadFile.enqueue(request);

        System.out.println("runnable, " + uiScheduler.areAnyRunnable() + " "  + bgScheduler.areAnyRunnable());
       // requestManager.execute();
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

//        DownloadManager manager = (DownloadManager) activity.getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
//        DownloadManager.Query query = new DownloadManager.Query();

//        Cursor c = manager.query(query);
//
//        System.out.println( Shadows.shadowOf(query).getIds());
//        int count=0;
//        int entries = 0;
//        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
//            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
//            String uri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
//            entries ++;
//            System.out.println("status :" + status + " " + uri);
//            if( status == DownloadManager.STATUS_FAILED || status == DownloadManager.STATUS_SUCCESSFUL) {
//                continue;
//            }
//
//        }

        //String wvcookie = WebviewUtil.getCookie();
        //Asserts the Cookie stored in the device is the same as that of the one we sent back in the response.
        //assertEquals(getUUId2(wvcookie), getUUId2(TestResponsesUT.UUID_COOKIE_1));
    }
}
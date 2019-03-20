package com.smartdeviceny.workinprogress;



import com.smartdeviceny.njts.Utils;

import org.apache.tools.ant.filters.StringInputStream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Cache;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=24)
public class CacheManagerTest {


    @Test
    public void cacheManagerTest() throws IOException {
        EmptyActivity activity = Robolectric.buildActivity( EmptyActivity.class )
                .create()
                .resume()
                .get();

        CacheManager manager = new CacheManager(activity.getApplicationContext(), "cache", null);
        File cachedFile = manager.getCachedFile("test_file");
        if(cachedFile.exists()) {
            cachedFile.delete();
        }
        String STR1="hello there";
        StringInputStream str0 = new StringInputStream(STR1);
        com.smartdeviceny.njts.Utils.writeExtractedFileToDisk(str0, new FileOutputStream(cachedFile));

        CacheManager manager2 = new CacheManager(activity.getApplicationContext(), "cache2", null);
        File cachedFile2 = manager2.getCachedFile("test_file");
        if(cachedFile2.exists()) {
            cachedFile2.delete();
        }

        try { Thread.sleep(1000); } catch(Exception e ) {}
        String STR2 = "hello there STRING 2";
        StringInputStream str2 = new StringInputStream(STR2);
        Utils.writeExtractedFileToDisk(str2, new FileOutputStream(cachedFile2));
        System.out.println(cachedFile2);

        long before = cachedFile2.lastModified();
        Assert.assertEquals(false, manager2.saveCache(cachedFile, cachedFile2.getName(), true));
        Assert.assertEquals(true, manager.saveCache(cachedFile2, cachedFile2.getName(), true));
//        System.out.print(Utils.getEntireFileContent(manager2.getCachedFile(cachedFile.getName())) + "\n" + STR2);
//        Assert.assertEquals(before, manager.getCachedFile(cachedFile2.getName()).lastModified());
    }
}

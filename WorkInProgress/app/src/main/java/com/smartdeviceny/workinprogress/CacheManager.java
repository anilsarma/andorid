package com.smartdeviceny.workinprogress;

import android.content.Context;
import android.support.annotation.Nullable;

import com.smartdeviceny.njts.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CacheManager {
    private Context context;
    private File dir;
    static Object lock = new Object();

    public CacheManager(Context context, @Nullable String cacheName, @Nullable File dir) {
        if(lock ==null) {
            lock = new Object();
        }
        this.context = context.getApplicationContext();
        this.dir = dir;
        if(cacheName ==null) {
            cacheName = "cache";
        }
        if (this.dir == null) {
            this.dir = new File(this.context.getApplicationInfo().dataDir, cacheName);
        }
        makeDir();
    }

    public void makeDir() {
        dir.mkdirs();
    }

    public File getDir() {
        return dir;
    }

    public void setDir(File dir) {
        this.dir = dir;
        makeDir();
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public @Nullable
    File getCachedFile(String file) {
        File cachedFile = new File(dir, file);
        return cachedFile;
    }

    public void clearCache(String file) {
        File cachedFile = getCachedFile(file);
        if (cachedFile != null) {
            try {
                synchronized (lock) {
                    cachedFile.delete();
                }
            } catch (Exception e) {

            }
        }
    }

    public boolean saveCache(File src, @Nullable  String dest, boolean updateIfNewer) {
        // don't use the name of the src use the dest if available
        try {
            dest=(dest==null)?src.getName():dest;
            File cachedFile = getCachedFile(dest);
            boolean update = !updateIfNewer;
            if(!update) {
                if( updateIfNewer) {
                    if( !cachedFile.exists()) {
                        update = true;
                    } else if( src.lastModified() > cachedFile.lastModified()) {
                        update = true;
                    }
                }

            }

            if( update) {
                InputStream in = new FileInputStream(src);
                OutputStream os = new FileOutputStream(dest);
                synchronized (lock) {
                    Utils.writeExtractedFileToDisk(in, os);
                }
            }
            return update;
        } catch (IOException e) {
            e.printStackTrace();

        }
        return false;
    }
}

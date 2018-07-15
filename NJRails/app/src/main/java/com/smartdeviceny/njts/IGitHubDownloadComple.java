package com.smartdeviceny.njts;

import java.io.File;

/**
 * Created by asarma on 11/17/2017.
 */

public interface IGitHubDownloadComple {
    void onDownloadComplete(String filename, File folder, File destination);
    void onFailed(String filename);
}

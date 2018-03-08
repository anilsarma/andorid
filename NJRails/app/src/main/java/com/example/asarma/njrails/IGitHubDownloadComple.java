package com.example.asarma.njrails;

import android.content.Context;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by asarma on 11/17/2017.
 */

public interface IGitHubDownloadComple {
    void onDownloadComplete(String filename, File folder, File destination);
    void onFailed(String filename);
}

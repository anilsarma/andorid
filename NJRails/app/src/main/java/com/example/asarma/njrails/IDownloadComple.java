package com.example.asarma.njrails;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by asarma on 11/17/2017.
 */

public interface IDownloadComple {
    public Context getContext();
    public void updateAdapter(View view, Long s, ArrayList<HashMap<String, Object>> result);
}

package com.smartdeviceny.dev.myapplication;

import android.support.annotation.LayoutRes;

public interface HeaderData extends  StickyMainData{
    @LayoutRes
    int getHeaderLayout();
    int getHeaderType();
}

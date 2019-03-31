package com.smartdeviceny.workinprogress;

import com.smartdeviceny.njts.JSONObjectSerializer;
import com.smartdeviceny.njts.Persist;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.NonNull;

class TestObjectHash {

    @Persist
    HashMap<String, String> tos = new HashMap<tring, String>();

    public TestObjectHash() {

    }

    @NonNull
    @Override
    public String toString() {
        return JSONObjectSerializer.stringify(this);
    }
}

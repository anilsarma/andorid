package com.smartdeviceny.workinprogress;

import com.smartdeviceny.njts.JSONObjectSerializer;
import com.smartdeviceny.njts.Persist;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.NonNull;

class TestObject {
    @Persist
    Date date;

    @Persist
    Double class_double = new Double(100);

    @Persist
    double primitive_double;

    @Persist
    Integer class_integer;

    @Persist
    int primitive_integer = 12;

   // @Persist
    ArrayList<Integer> ids = new ArrayList<Integer>();
    @Persist
    ArrayList<TestObject> tos = new ArrayList<TestObject>();
    public TestObject() {

    }

    @NonNull
    @Override
    public String toString() {
        return JSONObjectSerializer.stringify(this);
    }
}

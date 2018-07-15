package com.smartdeviceny.njts;


/**
 * Created by asarma on 11/2/2017.
 */

public class Operation {
    public String operation = "=";
    public Object value;

    public Operation(String operation, Object value)
    {
        this.operation = operation;
        this.value = value;
    }
};
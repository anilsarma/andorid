package com.smartdeviceny.njts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD) //on field level
public @interface Persist {
    public enum State {
        NO, YES
    }
//    int value() default  0;
//    double value() default  0;
    State state() default State.YES;
}
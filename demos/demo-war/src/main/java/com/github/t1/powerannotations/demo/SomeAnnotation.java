package com.github.t1.powerannotations.demo;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface SomeAnnotation {
    String value();
}

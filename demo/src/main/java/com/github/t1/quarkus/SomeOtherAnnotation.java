package com.github.t1.quarkus;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface SomeOtherAnnotation {
    String value();
}

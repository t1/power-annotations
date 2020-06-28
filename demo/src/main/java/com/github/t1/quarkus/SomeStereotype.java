package com.github.t1.quarkus;

import com.github.t1.annotations.Stereotype;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Stereotype
@SomeOtherAnnotation("stereotyped")
public @interface SomeStereotype {}

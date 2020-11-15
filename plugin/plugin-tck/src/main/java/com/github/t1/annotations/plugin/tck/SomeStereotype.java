package com.github.t1.annotations.plugin.tck;

import com.github.t1.annotations.Stereotype;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Stereotype
@StereotypedAnnotation("stereotyped")
@Retention(RUNTIME)
public @interface SomeStereotype {}

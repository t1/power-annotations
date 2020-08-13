package com.github.t1.annotations.tck;

import com.github.t1.annotations.Stereotype;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class CombinedAnnotationClasses {
    @Stereotype
    @SomeAnnotation("from-stereotype")
    @Retention(RUNTIME)
    public @interface SomeStereotype {}

    @SomeStereotype
    public interface SomeInterface {
        @SuppressWarnings("unused")
        String foo();
    }

    @SomeStereotype
    public static class SomeClass {
        @SuppressWarnings("unused")
        public String foo() { return null; }
    }
}

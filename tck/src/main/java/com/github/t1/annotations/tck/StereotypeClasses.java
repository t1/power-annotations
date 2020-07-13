package com.github.t1.annotations.tck;

import com.github.t1.annotations.Stereotype;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class StereotypeClasses {
    @Retention(RUNTIME)
    @Target(ANNOTATION_TYPE)
    public @interface SomeMetaAnnotation {}

    @Stereotype
    @Retention(RUNTIME)
    @SomeAnnotation("some-stereotype")
    @RepeatableAnnotation(1)
    @RepeatableAnnotation(2)
    @SomeMetaAnnotation
    public @interface SomeStereotype {}

    @Stereotype
    @Retention(RUNTIME)
    @SomeAnnotation("another-stereotype")
    @RepeatableAnnotation(3)
    @RepeatableAnnotation(4)
    public @interface AnotherStereotype {}


    @SomeStereotype
    @RepeatableAnnotation(5)
    public static class StereotypedClass {}

    @SomeStereotype
    @SomeAnnotation("on-class")
    public static class StereotypedClassWithSomeAnnotation {}

    @SomeStereotype
    @AnotherStereotype
    @RepeatableAnnotation(6)
    public static class DoubleStereotypedClass {}


    @SuppressWarnings("unused")
    public static class ClassWithStereotypedField {
        @SomeStereotype
        @RepeatableAnnotation(7)
        String foo;
        boolean bar;
    }

    @SuppressWarnings("unused")
    public static class ClassWithStereotypedMethod {
        @SomeStereotype
        @RepeatableAnnotation(7)
        String foo() { return "foo"; }

        String bar() { return "bar"; }
    }
}

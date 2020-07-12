package com.github.t1.annotations.tck;

import com.github.t1.annotations.Stereotype;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class StereotypeClasses {
    @Stereotype
    @Retention(RUNTIME)
    @SomeAnnotation("stereotype")
    @RepeatableAnnotation(1)
    @RepeatableAnnotation(2)
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

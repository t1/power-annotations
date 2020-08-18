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
    public interface SomeStereotypedInterface {
        @SuppressWarnings("unused")
        void foo();
    }

    @SomeStereotype
    public static class SomeStereotypedClass {
        @SuppressWarnings("unused")
        public void foo() {}
    }

    @SomeAnnotation("from-sub-interface")
    public interface SomeInheritingInterface extends SomeInheritedInterface {
    }

    public interface SomeInheritedInterface {
        @SuppressWarnings("unused")
        void foo();
    }
}

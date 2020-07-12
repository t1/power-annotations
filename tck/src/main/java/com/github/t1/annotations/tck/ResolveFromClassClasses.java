package com.github.t1.annotations.tck;

public class ResolveFromClassClasses {
    @SomeAnnotation("class-annotation")
    public static class ClassWithField {
        @SuppressWarnings("unused")
        String someField;
    }

    @RepeatableAnnotation(1)
    @RepeatableAnnotation(2)
    public static class ClassWithRepeatedAnnotationsForField {
        @SuppressWarnings("unused")
        String someField;
    }

    @RepeatableAnnotation(2)
    public static class ClassWithRepeatableAnnotationOnClassAndField {
        @RepeatableAnnotation(1)
        @SuppressWarnings("unused")
        String someField;
    }

    @SomeAnnotation("class-annotation")
    public static class ClassWithAnnotationsOnClassAndField {
        @SuppressWarnings("unused")
        @RepeatableAnnotation(1)
        String someField;
    }


    @SomeAnnotation("class-annotation")
    public static class ClassWithMethod {
        @SuppressWarnings("unused")
        void someMethod() {}
    }

    @RepeatableAnnotation(1)
    @RepeatableAnnotation(2)
    public static class ClassWithRepeatedAnnotationsForMethod {
        @SuppressWarnings("unused")
        void someMethod() {}
    }

    @RepeatableAnnotation(2)
    public static class ClassWithRepeatableAnnotationOnClassAndMethod {
        @RepeatableAnnotation(1)
        @SuppressWarnings("unused")
        void someMethod() {}
    }

    @SomeAnnotation("class-annotation")
    public static class ClassWithAnnotationsOnClassAndMethod {
        @SuppressWarnings("unused")
        @RepeatableAnnotation(1)
        void someMethod() {}
    }
}

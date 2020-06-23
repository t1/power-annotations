package test.plain;

import test.jandexed.SomeAnnotation;

@SuppressWarnings("unused")
@SomeAnnotation("some-reflection-class")
public class SomeReflectionClass {
    @SomeAnnotation("some-reflection-method")
    void foo(String x) {}

    @SomeAnnotation("some-reflection-field")
    String bar;
}

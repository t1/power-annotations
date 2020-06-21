package test.plain;

import test.jandexed.DummyAnnotation;

@SuppressWarnings("unused")
@DummyAnnotation("reflection-dummy-class")
public class ReflectionDummyClass {
    @DummyAnnotation("reflection-dummy-method")
    void foo(String x) {}

    @DummyAnnotation("reflection-dummy-field")
    String bar;
}

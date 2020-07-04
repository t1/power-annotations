package com.github.t1.annotations.index;

public class AnnotationValue {
    private final Index index;
    private final org.jboss.jandex.AnnotationValue value;

    public AnnotationValue(Index index, org.jboss.jandex.AnnotationValue value) {
        this.index = index;
        this.value = value;
    }

    public ClassInfo classValue() {
        return new ClassInfo(index, this.value.asClass().name());
    }

    public Object value() {
        return this.value.value();
    }
}

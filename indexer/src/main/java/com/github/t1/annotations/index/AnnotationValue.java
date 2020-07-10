package com.github.t1.annotations.index;

import static java.util.Objects.requireNonNull;

public class AnnotationValue {
    public static Object of(Object value) {
        return ((org.jboss.jandex.AnnotationValue)value).value();
    }

    private final Index index;
    private final org.jboss.jandex.AnnotationValue value;

    public AnnotationValue(Index index, org.jboss.jandex.AnnotationValue value) {
        this.index = index;
        this.value = requireNonNull(value);
    }

    public ClassInfo classValue() {
        return new ClassInfo(index(), this.value.asClass().name());
    }

    private Index index() { return requireNonNull(index); }

    public Object value() {
        return this.value.value();
    }
}

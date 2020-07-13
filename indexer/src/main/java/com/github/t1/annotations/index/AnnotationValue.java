package com.github.t1.annotations.index;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class AnnotationValue {
    public static Object of(Object value) {
        return ((org.jboss.jandex.AnnotationValue) value).value();
    }

    private final Optional<Index> index;
    private final org.jboss.jandex.AnnotationValue value;

    AnnotationValue(Optional<Index> index, org.jboss.jandex.AnnotationValue value) {
        this.index = index;
        this.value = requireNonNull(value);
    }

    public ClassInfo classValue() {
        return index().classInfo(this.value.asClass().name());
    }

    private Index index() { return index.orElseThrow(() -> new UnsupportedOperationException("not supported for meta annotations")); }

    public Object value() {
        return this.value.value();
    }
}

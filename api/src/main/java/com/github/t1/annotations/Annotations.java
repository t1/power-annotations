package com.github.t1.annotations;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

public interface Annotations {
    static Annotations on(Class<?> type) {
        return AnnotationsLoader.INSTANCE.onType(type);
    }

    static Annotations onField(Class<?> type, String fieldName) {
        return AnnotationsLoader.INSTANCE.onField(type, fieldName);
    }

    static Annotations onMethod(Class<?> type, String methodName, Class<?>... argTypes) {
        return AnnotationsLoader.INSTANCE.onMethod(type, methodName, argTypes);
    }

    List<Annotation> all();

    <T extends Annotation> Optional<T> get(Class<T> type);
}

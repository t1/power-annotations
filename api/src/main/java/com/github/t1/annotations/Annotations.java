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

    /**
     * Get all {@link Annotation} instances.
     * If the annotation type is {@link java.lang.annotation.Repeatable}, the same type
     * can show up several times, eventually with different properties.
     */
    List<Annotation> all();

    /**
     * Get the {@link Annotation} instance of this type.
     * If the annotation type is {@link java.lang.annotation.Repeatable},
     * this method throws a {@link RepeatableAnnotationAccessedWithGetException},
     * so making an annotation repeatable is a breaking change, but that's better than
     * breaking, because indirectly available annotations (e.g. from stereotypes)
     * become suddenly visible.
     *
     * TODO discuss
     */
    <T extends Annotation> Optional<T> get(Class<T> type);

    // TODO <T extends Annotation> Stream<T> all(Class<T> type);
}

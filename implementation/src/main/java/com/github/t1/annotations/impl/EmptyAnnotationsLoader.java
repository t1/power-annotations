package com.github.t1.annotations.impl;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.AnnotationsLoader;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

class EmptyAnnotationsLoader extends AnnotationsLoader {
    @Override public Annotations onType(Class<?> type) { return new EmptyAnnotations(); }

    @Override public Annotations onField(Class<?> type, String fieldName) { return new EmptyAnnotations(); }

    @Override public Annotations onMethod(Class<?> type, String methodName, Class<?>... argTypes) { return new EmptyAnnotations(); }

    private static class EmptyAnnotations implements Annotations {
        @Override public List<Annotation> all() { return emptyList(); }

        @Override public <T extends Annotation> Optional<T> get(Class<T> type) { return Optional.empty(); }

        @Override public <T extends Annotation> Stream<T> all(Class<T> type) { return Stream.empty(); }
    }
}

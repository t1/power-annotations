package com.github.t1.annotations.impl;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.AnnotationsLoader;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Arrays.asList;

/**
 * {@link Annotations} collected via JDK reflection. Directly returns the 'real'
 * annotation instances, not {@link AnnotationProxy proxies} and {@link AbstractAnnotation}s,
 * to keep the overhead minimal.
 */
public class ReflectionAnnotationsLoader extends AnnotationsLoader {
    private final AtomicBoolean skip;
    private final AnnotationsLoader delegate;

    public ReflectionAnnotationsLoader(AtomicBoolean skip, AnnotationsLoader delegate) {
        this.skip = skip;
        this.delegate = delegate;
    }

    public Annotations onType(Class<?> type) {
        if (skip.get())
            return delegate.onType(type);
        return new ReflectionAnnotations(type);
    }

    public Annotations onField(Class<?> type, String fieldName) {
        if (skip.get())
            return delegate.onField(type, fieldName);
        try {
            return new ReflectionAnnotations(type.getDeclaredField(fieldName));
        } catch (NoSuchFieldException e) {
            throw new FieldNotFoundException(fieldName, type, e);
        }
    }

    public Annotations onMethod(Class<?> type, String methodName, Class<?>... argTypes) {
        if (skip.get())
            return delegate.onMethod(type, methodName, argTypes);
        try {
            return new ReflectionAnnotations(type.getDeclaredMethod(methodName, argTypes));
        } catch (NoSuchMethodException e) {
            throw new MethodNotFoundException(methodName, argTypes, type, e);
        }
    }

    private static class ReflectionAnnotations implements Annotations {
        private final AnnotatedElement element;

        ReflectionAnnotations(AnnotatedElement element) { this.element = element; }

        @Override public List<Annotation> all() {
            return asList(element.getAnnotations());
        }

        @Override public <T extends Annotation> Optional<T> get(Class<T> type) {
            return Optional.ofNullable(element.getAnnotation(type));
        }
    }
}

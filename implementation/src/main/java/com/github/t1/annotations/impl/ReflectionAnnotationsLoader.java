package com.github.t1.annotations.impl;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.AnnotationsLoader;
import com.github.t1.annotations.RepeatableAnnotationAccessedWithGetException;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

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
            return Stream.of(element.getAnnotations())
                .flatMap(ReflectionAnnotationsLoader::resolveRepeatableAnnotations)
                .collect(toList());
        }

        @Override public <T extends Annotation> Optional<T> get(Class<T> type) {
            if (type.isAnnotationPresent(Repeatable.class))
                throw new RepeatableAnnotationAccessedWithGetException(type);
            return Optional.ofNullable(element.getAnnotation(type));
        }
    }


    private static Stream<Annotation> resolveRepeatableAnnotations(Annotation annotation) {
        if (isRepeatable(annotation.annotationType()))
            return repeatedAnnotation(annotation);
        return Stream.of(annotation);
    }

    private static boolean isRepeatable(Class<? extends Annotation> type) {
        for (Method method : type.getMethods())
            if ("value".equals(method.getName())
                && method.getReturnType().isArray()
                && method.getReturnType().getComponentType().isAnnotation()
                && method.getReturnType().getComponentType().isAnnotationPresent(Repeatable.class))
                return true;
        return false;
    }

    private static Stream<Annotation> repeatedAnnotation(Annotation annotation) {
        try {
            Method method = annotation.annotationType().getMethod("value");
            Annotation[] value = (Annotation[]) method.invoke(annotation);
            return Stream.of(value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("assumed " + annotation + " to be repeatable", e);
        }
    }
}

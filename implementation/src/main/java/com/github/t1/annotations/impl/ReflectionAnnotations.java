package com.github.t1.annotations.impl;

import com.github.t1.annotations.Annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;

class ReflectionAnnotations implements Annotations {
    public static Annotations on(Class<?> type) {
        return new ReflectionAnnotations(type);
    }

    public static Annotations onField(Class<?> type, String fieldName) {
        try {
            return new ReflectionAnnotations(type.getDeclaredField(fieldName));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("no field '" + fieldName + "' in " + type, e);
        }
    }

    public static Annotations onMethod(Class<?> type, String methodName, Class<?>... argTypes) {
        try {
            return new ReflectionAnnotations(type.getDeclaredMethod(methodName, argTypes));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("no method " + signature(methodName, argTypes) + " in " + type, e);
        }
    }

    static String signature(String methodName, Class<?>... argTypes) {
        StringBuilder out = new StringBuilder(methodName).append("(");
        for (int i = 0; i < argTypes.length; i++) {
            if (i > 0)
                out.append(", ");
            out.append(argTypes[i].getSimpleName());
        }
        return out.append(")").toString();
    }

    private final AnnotatedElement element;

    private ReflectionAnnotations(AnnotatedElement element) { this.element = element; }

    @Override public List<Annotation> all() {
        return asList(element.getAnnotations());
    }

    @Override public <T extends Annotation> Optional<T> get(Class<T> type) {
        return Optional.ofNullable(element.getAnnotation(type));
    }
}

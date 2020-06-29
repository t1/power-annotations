package com.github.t1.annotations.impl;

import com.github.t1.annotations.index.AnnotationInstance;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * {@link #build() Builds} a {@link Proxy dynamic proxy} that delegates to three
 * function objects for the implementation.
 */
class AnnotationProxy {
    static Annotation proxy(AnnotationInstance annotationInstance) {
        return new AnnotationProxy(
            annotationInstance::name,
            annotationInstance::toString,
            name -> annotationInstance.value(name).value())
            .build();
    }

    private final Supplier<String> typeName;
    private final Supplier<String> toString;
    private final Function<String, Object> property;

    private AnnotationProxy(Supplier<String> typeName, Supplier<String> toString, Function<String, Object> property) {
        this.property = property;
        this.toString = toString;
        this.typeName = typeName;
    }

    private Annotation build() {
        Class<?>[] interfaces = new Class[]{getAnnotationType(), Annotation.class};
        return (Annotation) Proxy.newProxyInstance(getClassLoader(), interfaces, this::invoke);
    }

    private Class<?> getAnnotationType() {
        return loadClass(typeName.get());
    }

    private static Class<?> loadClass(String typeName) {
        try {
            return getClassLoader().loadClass(typeName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("can't load annotation type " + typeName, e);
        }
    }

    private static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return (classLoader == null) ? ClassLoader.getSystemClassLoader() : classLoader;
    }

    Object invoke(Object proxy, Method method, Object... args) {
        String name = method.getName();
        if (method.getParameterCount() == 1 && "equals".equals(name))
            return toString.get().equals(args[0].toString());
        // no other methods on annotations can have arguments (except for one `wait`)
        assert method.getParameterCount() == 0;
        assert args == null || args.length == 0;
        if ("hashCode".equals(name))
            return toString.get().hashCode();
        if ("annotationType".equals(name))
            return getAnnotationType();
        if ("toString".equals(name))
            return toString.get();
        return property.apply(name);
    }
}

package com.github.t1.annotations.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * {@link #build() Builds} a {@link Proxy dynamic proxy} that delegates to an {@link AbstractAnnotation} instance
 * holding the properties of an annotation.
 */
class AnnotationProxy {
    private final AbstractAnnotation abstractAnnotation;

    AnnotationProxy(AbstractAnnotation abstractAnnotation) { this.abstractAnnotation = abstractAnnotation; }

    Annotation build() {
        Class<?>[] interfaces = new Class[]{getAnnotationType(), Annotation.class};
        return (Annotation) Proxy.newProxyInstance(getClassLoader(), interfaces, this::invoke);
    }

    private Class<?> getAnnotationType() {
        String typeName = abstractAnnotation.getTypeName();
        try {
            return getClassLoader().loadClass(typeName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("can't load annotation type " + typeName, e);
        }
    }

    static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return (classLoader == null) ? ClassLoader.getSystemClassLoader() : classLoader;
    }

    Object invoke(Object proxy, Method method, Object... args) {
        String name = method.getName();
        if (method.getParameterCount() == 1 && "equals".equals(name))
            return proxy == args[0];
        // no other methods on annotations can have arguments (except for `wait`)
        assert method.getParameterCount() == 0;
        assert args == null || args.length == 0;
        if ("hashCode".equals(name))
            return abstractAnnotation.toString().hashCode();
        if ("annotationType".equals(name))
            return getAnnotationType();
        if ("toString".equals(name))
            return abstractAnnotation.toString();
        return abstractAnnotation.property(name);
    }
}

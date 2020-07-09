package com.github.t1.annotations.impl;

import com.github.t1.annotations.index.AnnotationInstance;
import com.github.t1.annotations.index.AnnotationValue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.t1.annotations.impl.Utils.enumValue;
import static com.github.t1.annotations.index.ClassInfo.toClass;

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

        Object value = property.apply(name);

        return toType(value, method.getReturnType());
    }

    private Object toType(Object value, Class<?> returnType) {
        if (returnType.isAnnotation())
            return proxy(AnnotationInstance.from(value));
        if (returnType.isEnum())
            return enumValue(returnType, (String) value);
        if (returnType.equals(Class.class))
            return toClass(value);
        if (returnType.isArray())
            return toArray(returnType.getComponentType(), (Object[]) value);
        return value;
    }

    private Object toArray(Class<?> componentType, Object[] values) {
        Object array = Array.newInstance(componentType, values.length);
        for (int i = 0; i < values.length; i++)
            Array.set(array, i, toType(AnnotationValue.of(values[i]), componentType));
        return array;
    }
}

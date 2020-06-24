package com.github.t1.annotations.impl;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.AnnotationsLoader;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import java.util.List;
import java.util.Optional;

public class JandexAnnotationsLoader extends AnnotationsLoader {
    public static AnnotationsLoader of(Index index, AnnotationsLoader delegate) {
        if (index == null)
            return delegate;
        return new JandexAnnotationsLoader(index, delegate);
    }

    private final Index index;
    private final AnnotationsLoader delegate;

    private JandexAnnotationsLoader(Index index, AnnotationsLoader delegate) {
        this.index = index;
        this.delegate = delegate;
    }

    public Annotations onType(Class<?> type) {
        ClassInfo classInfo = info(type);
        if (classInfo == null)
            return delegate.onType(type);
        return new JandexAnnotations(classInfo.classAnnotations());
    }

    public Annotations onField(Class<?> type, String fieldName) {
        ClassInfo classInfo = info(type);
        if (classInfo == null)
            return delegate.onField(type, fieldName);
        FieldInfo field = classInfo.field(fieldName);
        if (field == null)
            throw new FieldNotFoundException(fieldName, type);
        return new JandexAnnotations(field.annotations());
    }

    public Annotations onMethod(Class<?> type, String methodName, Class<?>... argTypes) {
        ClassInfo classInfo = info(type);
        if (classInfo == null)
            return delegate.onMethod(type, methodName, argTypes);
        return findMethod(classInfo, methodName, argTypes)
            .map(method -> new JandexAnnotations(method.annotations()))
            .orElseThrow(() -> new MethodNotFoundException(methodName, argTypes, type));
    }

    private ClassInfo info(Class<?> type) {
        DotName name = DotName.createSimple(type.getName());
        return index.getClassByName(name);
    }

    private static Optional<MethodInfo> findMethod(ClassInfo classInfo, String methodName, Class<?>... argTypes) {
        return classInfo.methods().stream()
            .filter(methodInfo -> methodInfo.name().equals(methodName))
            .filter(methodInfo -> methodInfo.parameters().size() == argTypes.length)
            .filter(methodInfo -> matchTypes(methodInfo.parameters(), argTypes))
            .findFirst();
    }

    private static boolean matchTypes(List<Type> parameters, Class<?>[] argTypes) {
        for (int i = 0; i < parameters.size(); i++) {
            Type paramType = parameters.get(i);
            Class<?> argType = argTypes[i];
            if (!paramType.name().toString().equals(argType.getName()))
                return false;
        }
        return true;
    }
}

package com.github.t1.annotations.index;

import org.jboss.jandex.Type;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.t1.annotations.index.AnnotationInstance.resolveRepeatables;
import static java.util.Objects.requireNonNull;

public class ClassInfo {
    private final Index index;
    private final org.jboss.jandex.ClassInfo delegate;

    ClassInfo(Index index, org.jboss.jandex.ClassInfo delegate) {
        this.index = requireNonNull(index);
        this.delegate = requireNonNull(delegate);
    }

    @Override public String toString() { return delegate.toString(); }


    public String simpleName() { return delegate.simpleName(); }

    public Stream<AnnotationInstance> annotations() {
        return delegate.classAnnotations().stream()
            .flatMap(instance -> resolveRepeatables(index, instance));
    }

    public Stream<AnnotationInstance> annotations(String name) {
        return delegate.classAnnotations().stream()
            .flatMap(instance -> resolveRepeatables(index, instance))
            .filter(annotationInstance -> annotationInstance.name().equals(name));
    }

    public Optional<FieldInfo> field(String fieldName) {
        return Optional.ofNullable(delegate.field(fieldName))
            .map(fieldInfo -> new FieldInfo(index, fieldInfo));
    }

    public Optional<MethodInfo> method(String methodName, Class<?>... argTypes) {
        Type[] delegateArgTypes = {}; // FIXME test missing!
        return Optional.ofNullable(delegate.method(methodName, delegateArgTypes))
            .map(methodInfo -> new MethodInfo(index, methodInfo));
    }

    public Optional<MethodInfo> findMethod(String methodName, Class<?>... argTypes) {
        return delegate.methods().stream()
            .filter(methodInfo -> methodInfo.name().equals(methodName))
            .filter(methodInfo -> methodInfo.parameters().size() == argTypes.length)
            .filter(methodInfo -> matchTypes(methodInfo.parameters(), argTypes))
            .findFirst()
            .map(methodInfo -> new MethodInfo(index, methodInfo));
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

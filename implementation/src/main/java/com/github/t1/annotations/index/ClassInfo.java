package com.github.t1.annotations.index;

import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.t1.annotations.index.AnnotationInstance.resolveRepeatables;
import static java.lang.reflect.Modifier.PUBLIC;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

public class ClassInfo {
    private final Index index;
    private final org.jboss.jandex.ClassInfo delegate;

    ClassInfo(Index index, org.jboss.jandex.ClassInfo delegate) {
        this.index = requireNonNull(index);
        this.delegate = requireNonNull(delegate);
    }

    ClassInfo(Index index, DotName name) {
        this.index = index;
        this.delegate = Optional.ofNullable(byName(name)).orElseGet(() -> mock(name));
    }

    private org.jboss.jandex.ClassInfo byName(DotName name) {
        return this.index.jandex.getClassByName(name);
    }

    @SuppressWarnings("deprecation")
    private org.jboss.jandex.ClassInfo mock(DotName name) {
        return org.jboss.jandex.ClassInfo.create(
            name, null, (short) PUBLIC, new DotName[0], emptyMap(), true);
    }

    @Override public String toString() { return delegate.toString(); }

    @Override public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;
        ClassInfo that = (ClassInfo) other;
        return delegate.name().equals(that.delegate.name());
    }

    @Override public int hashCode() { return delegate.name().hashCode(); }


    public String getName() { return delegate.name().toString(); }

    public String simpleName() { return delegate.simpleName(); }

    public Stream<AnnotationInstance> annotations() {
        return delegate.classAnnotations().stream()
            .flatMap(instance -> resolveRepeatables(index, instance));
    }

    public Stream<AnnotationInstance> annotations(String name) {
        return annotations()
            .filter(annotationInstance -> annotationInstance.name().equals(name));
    }

    public Optional<FieldInfo> field(String fieldName) {
        return Optional.ofNullable(delegate.field(fieldName))
            .map(fieldInfo -> new FieldInfo(index, fieldInfo));
    }

    public Optional<MethodInfo> method(String methodName, Class<?>... argTypes) {
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

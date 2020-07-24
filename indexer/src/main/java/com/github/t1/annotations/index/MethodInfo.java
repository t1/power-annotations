package com.github.t1.annotations.index;

import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import java.lang.annotation.ElementType;
import java.util.stream.Stream;

import static com.github.t1.annotations.index.Utils.toArray;
import static java.lang.annotation.ElementType.METHOD;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public class MethodInfo extends AnnotationTarget {
    private final org.jboss.jandex.MethodInfo delegate;

    MethodInfo(Index index, org.jboss.jandex.MethodInfo delegate) {
        super(index);
        this.delegate = requireNonNull(delegate);
    }

    @Override public String toString() { return delegate.declaringClass().name() + "." + signature(delegate); }


    @Override public ElementType elementType() { return METHOD; }

    @Override public String name() { return delegate.name(); }

    @Override protected Stream<org.jboss.jandex.AnnotationInstance> rawAnnotations() {
        return delegate.annotations().stream()
            .filter(instance -> instance.target().kind() == Kind.METHOD); // Jandex also returns METHOD_PARAMETER or TYPE
    }

    public boolean isDefaultConstructor() { return isConstructor() && delegate.parameters().size() == 0; }

    public boolean isConstructor() { return name().equals("<init>"); }

    public boolean hasNoAnnotations() { return delegate.annotations().isEmpty(); }

    public AnnotationValue defaultValue() { return delegate.defaultValue(); }

    public String[] parameterTypeNames() { return parameterTypeNames(delegate); }

    static String signature(org.jboss.jandex.MethodInfo methodInfo) {
        return methodInfo.name() + Stream.of(parameterTypeNames(methodInfo)).collect(joining(", ", "(", ")"));
    }

    private static String[] parameterTypeNames(org.jboss.jandex.MethodInfo methodInfo) {
        return methodInfo.parameters().stream()
            .map(Type::name)
            .map(DotName::toString)
            .collect(toArray(String.class));
    }
}

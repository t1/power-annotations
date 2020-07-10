package com.github.t1.annotations.index;

import static java.util.Objects.requireNonNull;

import static org.jboss.jandex.AnnotationTarget.Kind.METHOD;

import static com.github.t1.annotations.index.AnnotationInstance.resolveRepeatables;

import java.util.List;
import java.util.stream.Stream;

import org.jboss.jandex.AnnotationValue;

public class MethodInfo implements Annotatable {
    private final Index index;
    private final org.jboss.jandex.MethodInfo delegate;

    public MethodInfo(Index index, org.jboss.jandex.MethodInfo delegate) {
        this.index = requireNonNull(index);
        this.delegate = requireNonNull(delegate);
    }

    @Override public String toString() { return delegate.toString(); }


    @Override public String name() { return delegate.name(); }

    @Override public Stream<AnnotationInstance> annotations() {
        return delegate.annotations().stream()
            .filter(instance -> instance.target().kind() == METHOD) // Jandex also returns METHOD_PARAMETER or TYPE
            .flatMap(instance -> resolveRepeatables(index, instance));
    }

    public List<?> parameters() {
        return delegate.parameters();
    }

    public AnnotationValue defaultValue() {
        return delegate.defaultValue();
    }
}

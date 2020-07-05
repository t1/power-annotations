package com.github.t1.annotations.index;

import org.jboss.jandex.AnnotationValue;

import java.util.List;
import java.util.stream.Stream;

import static com.github.t1.annotations.index.AnnotationInstance.resolveRepeatables;
import static java.util.Objects.requireNonNull;

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
            .flatMap(instance -> resolveRepeatables(index, instance));
    }

    public List<?> parameters() {
        return delegate.parameters();
    }

    public AnnotationValue defaultValue() {
        return delegate.defaultValue();
    }
}

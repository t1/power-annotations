package com.github.t1.annotations.index;

import java.util.stream.Stream;

import static com.github.t1.annotations.index.AnnotationInstance.resolveRepeatables;
import static java.util.Objects.requireNonNull;

public class FieldInfo implements Annotatable {
    private final Index index;
    private final org.jboss.jandex.FieldInfo delegate;

    public FieldInfo(Index index, org.jboss.jandex.FieldInfo delegate) {
        this.index = requireNonNull(index);
        this.delegate = requireNonNull(delegate);
    }

    @Override public String toString() { return delegate.toString(); }


    @Override public String name() { return delegate.name(); }

    @Override public Stream<AnnotationInstance> annotations() {
        return delegate.annotations().stream()
            .flatMap(instance -> resolveRepeatables(index, instance));
    }
}

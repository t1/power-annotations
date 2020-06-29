package com.github.t1.annotations.index;

import org.jboss.jandex.DotName;

import java.util.stream.Stream;

import static com.github.t1.annotations.index.AnnotationInstance.resolveRepeatables;
import static com.github.t1.annotations.index.Utils.streamOfNullable;
import static java.util.Objects.requireNonNull;

public class FieldInfo {
    private final Index index;
    private final org.jboss.jandex.FieldInfo delegate;

    public FieldInfo(Index index, org.jboss.jandex.FieldInfo delegate) {
        this.index = requireNonNull(index);
        this.delegate = requireNonNull(delegate);
    }

    @Override public String toString() { return delegate.toString(); }


    public Stream<AnnotationInstance> annotations() {
        return delegate.annotations().stream()
            .flatMap(instance -> resolveRepeatables(index, instance));
    }

    public Stream<AnnotationInstance> annotations(String name) {
        DotName dotName = DotName.createSimple(name);
        return streamOfNullable(delegate.annotation(dotName))
            .flatMap(instance -> resolveRepeatables(index, instance));
    }
}

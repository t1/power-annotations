package com.github.t1.annotations.index;

import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;

import java.lang.annotation.Repeatable;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.jboss.jandex.AnnotationValue.Kind.ARRAY;
import static org.jboss.jandex.AnnotationValue.Kind.NESTED;

public class AnnotationInstance {
    /** the stream of one single or several repeatable annotations */
    static Stream<AnnotationInstance> resolveRepeatables(Index index, org.jboss.jandex.AnnotationInstance delegate) {
        if (isRepeatable(index, delegate))
            return resolveRepeatable(index, delegate);
        return Stream.of(new AnnotationInstance(index, delegate));
    }

    private static boolean isRepeatable(Index index, org.jboss.jandex.AnnotationInstance delegate) {
        if (delegate.values().size() == 1
            && delegate.values().get(0).name().equals("value")
            && delegate.value().kind() == ARRAY
            && delegate.value().componentKind() == NESTED
            && delegate.value().asNestedArray().length > 0) {
            org.jboss.jandex.AnnotationInstance annotationInstance = delegate.value().asNestedArray()[0];
            org.jboss.jandex.ClassInfo classInfo = index.jandex.getClassByName(annotationInstance.name());
            return classInfo.classAnnotation(REPEATABLE) != null;
        }
        return false;
    }

    private static final DotName REPEATABLE = DotName.createSimple(Repeatable.class.getName());

    private static Stream<AnnotationInstance> resolveRepeatable(Index index, org.jboss.jandex.AnnotationInstance delegate) {
        return Stream.of((AnnotationValue[]) delegate.value().value())
            .map(annotationValue -> new AnnotationInstance(index, (org.jboss.jandex.AnnotationInstance) annotationValue.value()));
    }

    private final Index index;
    private final org.jboss.jandex.AnnotationInstance delegate;

    private AnnotationInstance(Index index, org.jboss.jandex.AnnotationInstance delegate) {
        this.index = requireNonNull(index);
        this.delegate = requireNonNull(delegate);
    }

    @Override public String toString() { return delegate.toString(false); }


    public Optional<ClassInfo> type() {
        return Optional.ofNullable(index.jandex.getClassByName(delegate.name()))
            .map(classInfo -> new ClassInfo(index, classInfo));
    }

    public AnnotationValue value(String name) {
        AnnotationValue value = delegate.value(name);
        if (value == null)
            return type().orElseThrow(() -> new RuntimeException("annotation not indexed: " + this))
                .method(name).orElseThrow(() -> new RuntimeException("no value '" + name + "' in " + this))
                .defaultValue();
        return value;
    }

    public String name() {
        return delegate.name().toString();
    }

    public ClassInfo targetClass() {
        return new ClassInfo(index, delegate.target().asClass());
    }
}

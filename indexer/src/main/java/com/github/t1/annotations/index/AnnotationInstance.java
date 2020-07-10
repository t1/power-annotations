package com.github.t1.annotations.index;

import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.DotName;

import java.lang.annotation.Repeatable;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.jboss.jandex.AnnotationValue.Kind.ARRAY;
import static org.jboss.jandex.AnnotationValue.Kind.NESTED;

public class AnnotationInstance {
    public static AnnotationInstance from(Object value) {
        return new AnnotationInstance(null, (org.jboss.jandex.AnnotationInstance) value);
    }

    static boolean isRepeatable(org.jboss.jandex.ClassInfo classInfo) {
        return classInfo.classAnnotation(REPEATABLE) != null;
    }

    /** the stream of one single or several repeatable annotations */
    static Stream<AnnotationInstance> resolveRepeatables(Index index, org.jboss.jandex.AnnotationInstance instance) {
        if (isRepeatable(index, instance))
            return resolveRepeatable(index, instance);
        return Stream.of(new AnnotationInstance(index, instance));
    }

    private static boolean isRepeatable(Index index, org.jboss.jandex.AnnotationInstance instance) {
        if (instance.values().size() == 1
            && instance.values().get(0).name().equals("value")
            && instance.value().kind() == ARRAY
            && instance.value().componentKind() == NESTED
            && instance.value().asNestedArray().length > 0) {
            org.jboss.jandex.AnnotationInstance annotationInstance = instance.value().asNestedArray()[0];
            org.jboss.jandex.ClassInfo classInfo = index.jandex.getClassByName(annotationInstance.name());
            return classInfo.classAnnotation(REPEATABLE) != null;
        }
        return false;
    }

    private static final DotName REPEATABLE = DotName.createSimple(Repeatable.class.getName());

    private static Stream<AnnotationInstance> resolveRepeatable(Index index, org.jboss.jandex.AnnotationInstance repeatable) {
        return Stream.of((org.jboss.jandex.AnnotationValue[]) repeatable.value().value())
            .map(annotationValue -> resolveAnnotationInstance(annotationValue, repeatable.target()))
            .map(annotationInstance -> new AnnotationInstance(index, annotationInstance));
    }

    private static org.jboss.jandex.AnnotationInstance resolveAnnotationInstance(org.jboss.jandex.AnnotationValue annotationValue, AnnotationTarget repeatable) {
        org.jboss.jandex.AnnotationInstance value = (org.jboss.jandex.AnnotationInstance) annotationValue.value();
        if (value.target() == null)
            value = org.jboss.jandex.AnnotationInstance.create(value.name(), repeatable, value.values());
        return value;
    }

    private final Index index;
    private final org.jboss.jandex.AnnotationInstance delegate;

    private AnnotationInstance(Index index, org.jboss.jandex.AnnotationInstance delegate) {
        this.index = index;
        this.delegate = requireNonNull(delegate);
    }

    @Override public String toString() { return delegate.toString(false) + " on " + targetString(); }


    private String targetString() {
        AnnotationTarget target = delegate.target();
        switch (target.kind()) {
            case CLASS:
                return target.asClass().name().toString();
            case FIELD:
                return target.asField().declaringClass().name() + "." + target.asField().name();
            case METHOD:
                return target.asMethod().declaringClass().name() + "." + target.asMethod().name();
            default:
                return target.toString();
        }
    }


    public ClassInfo type() { return new ClassInfo(index(), delegate.name()); }

    private Index index() { return requireNonNull(index); }

    public AnnotationValue value(String name) {
        org.jboss.jandex.AnnotationValue value = delegate.value(name);
        if (value == null)
            value = type().method(name)
                .orElseThrow(() -> new RuntimeException("no value '" + name + "' in " + this))
                .defaultValue();
        return new AnnotationValue(index, value);
    }

    public String name() {
        return delegate.name().toString();
    }

    public ClassInfo targetClass() {
        return new ClassInfo(index(), delegate.target().asClass());
    }
}

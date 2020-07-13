package com.github.t1.annotations.index;

import java.lang.annotation.ElementType;
import java.util.List;
import java.util.stream.Stream;

import static com.github.t1.annotations.index.AnnotationInstance.resolveRepeatables;
import static java.util.stream.Collectors.toList;

public abstract class AnnotationTarget {
    protected final Index index;
    private List<AnnotationInstance> annotations;

    public AnnotationTarget(Index index) { this.index = index; }

    public abstract ElementType elementType();

    public abstract String name();

    public Stream<AnnotationInstance> annotations(String name) {
        return annotations()
            .filter(annotationInstance -> annotationInstance.typeName().equals(name));
    }

    public final Stream<AnnotationInstance> annotations() {
        return getAnnotations().stream();
    }

    public List<AnnotationInstance> getAnnotations() {
        if (annotations == null)
            annotations = rawAnnotations()
                .flatMap(instance -> resolveRepeatables(index, instance))
                .collect(toList());
        return annotations;
    }

    protected abstract Stream<org.jboss.jandex.AnnotationInstance> rawAnnotations();

    public boolean isAnnotationPresent(String typeName) {
        return annotations(typeName).findAny().isPresent();
    }

    public void add(AnnotationInstance instance) {
        assert canBeAdded(instance.type());
        getAnnotations().add(instance.cloneWithTarget(this));
    }

    public void replace(AnnotationInstance instance) {
        ClassInfo annotationType = instance.type();
        assert annotationType.isImplicitlyAllowedOn(elementType());
        if (!annotationType.isRepeatableAnnotation())
            getAnnotations().removeIf(annotation -> annotation.type().equals(annotationType));
        getAnnotations().add(instance.cloneWithTarget(this));
    }

    public boolean canBeAdded(ClassInfo annotationType) {
        return (annotationType.isRepeatableAnnotation()
            || !isAnnotationPresent(annotationType.name()))
            && annotationType.isImplicitlyAllowedOn(elementType());
    }
}

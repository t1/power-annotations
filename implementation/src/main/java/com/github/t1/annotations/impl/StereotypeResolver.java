package com.github.t1.annotations.impl;

import com.github.t1.annotations.index.AnnotationInstance;
import com.github.t1.annotations.index.AnnotationTarget;
import com.github.t1.annotations.index.ClassInfo;
import com.github.t1.annotations.index.Index;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

class StereotypeResolver implements AnnotationResolver {
    private final Index index;

    StereotypeResolver(Index index) { this.index = index; }

    @Override public void resolve() {
        index.annotationTypes()
            .filter(this::isStereotypeType)
            .forEach(this::resolve);
    }

    private boolean isStereotypeType(ClassInfo classInfo) {
        return classInfo.annotations()
            .map(AnnotationInstance::typeName)
            .anyMatch(this::isStereotype);
    }

    private boolean isStereotype(String typeName) {
        return typeName.endsWith(".Stereotype");
    }

    private void resolve(ClassInfo stereotypeType) {
        index.allAnnotationInstancesOfType(stereotypeType)
            .forEach(target -> resolve(stereotypeType, target.target()));
    }

    private void resolve(ClassInfo stereotypeType, AnnotationTarget target) {
        stereotypeType.annotations()
            .filter(this::canBeAdded)
            .filter(annotationInstance -> target.canBeAdded(annotationInstance.type()))
            // .peek(instance -> System.out.println("add " + instance + " to " + target))
            .forEach(target::add);
    }

    private boolean canBeAdded(AnnotationInstance annotation) {
        return !isStereotype(annotation.typeName())
            && !DO_NON_RESOLVE.contains(annotation.typeName());
    }

    private static final List<String> DO_NON_RESOLVE = Stream.of(
        Retention.class, Target.class)
        .map(Class::getTypeName)
        .collect(toList());
}

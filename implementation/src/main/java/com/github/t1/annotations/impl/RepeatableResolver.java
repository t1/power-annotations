package com.github.t1.annotations.impl;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

import java.lang.annotation.Repeatable;
import java.util.stream.Stream;

import static org.jboss.jandex.AnnotationValue.Kind.ARRAY;
import static org.jboss.jandex.AnnotationValue.Kind.NESTED;

class RepeatableResolver {
    private final IndexView jandex;

    RepeatableResolver(IndexView jandex) { this.jandex = jandex; }

    Stream<AnnotationInstance> resolve(AnnotationInstance annotation) {
        if (isRepeatable(annotation))
            return resolveRepeatable(annotation);
        return Stream.of(annotation);
    }

    private boolean isRepeatable(AnnotationInstance annotation) {
        if (annotation.values().size() == 1
            && annotation.values().get(0).name().equals("value")
            && annotation.value().kind() == ARRAY
            && annotation.value().componentKind() == NESTED
            && annotation.value().asNestedArray().length > 0) {
            return isRepeatable(jandex.getClassByName(annotation.value().asNestedArray()[0].name()));
        }
        return false;
    }

    static boolean isRepeatable(ClassInfo contained) {
        return contained.classAnnotation(REPEATABLE) != null;
    }

    private static Stream<AnnotationInstance> resolveRepeatable(AnnotationInstance annotation) {
        return Stream.of((AnnotationValue[]) annotation.value().value())
            .map(annotationValue -> (AnnotationInstance) annotationValue.value());
    }

    private static final DotName REPEATABLE = DotName.createSimple(Repeatable.class.getName());
}

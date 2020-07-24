package com.github.t1.annotations.impl;

import com.github.t1.annotations.AmbiguousAnnotationResolutionException;
import com.github.t1.annotations.index.AnnotationTarget;

import java.lang.annotation.Annotation;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class PowerAnnotationsAmbiguousAnnotationResolutionException extends AmbiguousAnnotationResolutionException {
    public <T extends Annotation> PowerAnnotationsAmbiguousAnnotationResolutionException(
        Class<T> type, AnnotationTarget target, List<? extends Annotation> list) {
        super(type.getName() + " is ambiguous on " + target + ":"
            + list.stream().map(Annotation::toString).collect(joining("\n- ", "\n- ", "\n")));
    }
}

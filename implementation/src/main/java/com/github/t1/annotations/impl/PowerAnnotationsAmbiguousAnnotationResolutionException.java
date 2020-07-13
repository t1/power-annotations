package com.github.t1.annotations.impl;

import com.github.t1.annotations.AmbiguousAnnotationResolutionException;

import java.lang.annotation.Annotation;

public class PowerAnnotationsAmbiguousAnnotationResolutionException extends AmbiguousAnnotationResolutionException {
    public PowerAnnotationsAmbiguousAnnotationResolutionException(String typeName) {
        super("The annotation " + typeName + " is ambiguous. "
            + "You should query it with `all` not `get`.");
    }

    public <T extends Annotation> PowerAnnotationsAmbiguousAnnotationResolutionException(Class<T> type) {
        this(type.getName());
    }

    public <T extends Annotation> PowerAnnotationsAmbiguousAnnotationResolutionException(Class<T> type, String targetName) {
        super("The annotation " + type.getName() + " is ambiguous on " + targetName + ". "
            + "You should query it with `all` not `get`.");
    }
}

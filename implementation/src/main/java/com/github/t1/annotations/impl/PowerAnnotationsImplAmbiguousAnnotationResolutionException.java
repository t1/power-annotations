package com.github.t1.annotations.impl;

import com.github.t1.annotations.AmbiguousAnnotationResolutionException;

import java.lang.annotation.Annotation;

public class PowerAnnotationsImplAmbiguousAnnotationResolutionException extends AmbiguousAnnotationResolutionException {
    public <T extends Annotation> PowerAnnotationsImplAmbiguousAnnotationResolutionException(Class<T> type) {
        super("The annotation " + type.getName() + " is ambiguous. "
            + "You should query it with `all` not `get`.");
    }

    public <T extends Annotation> PowerAnnotationsImplAmbiguousAnnotationResolutionException(Class<T> type, String targetName) {
        super("The annotation " + type.getName() + " is ambiguous on " + targetName + ". "
            + "You should query it with `all` not `get`.");
    }
}

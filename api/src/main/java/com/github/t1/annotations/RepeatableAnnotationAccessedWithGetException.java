package com.github.t1.annotations;

import java.lang.annotation.Annotation;

public class RepeatableAnnotationAccessedWithGetException extends RuntimeException {
    private final Class<? extends Annotation> type;

    public RepeatableAnnotationAccessedWithGetException(Class<? extends Annotation> type) { this.type = type; }

    @Override public String getMessage() {
        return "The annotation " + type.getName() + " is repeatable, " +
            "so it should be queried with `all` not `get`.";
    }
}

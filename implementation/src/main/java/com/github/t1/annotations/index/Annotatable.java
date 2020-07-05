package com.github.t1.annotations.index;

import java.util.stream.Stream;

public interface Annotatable {
    String name();

    Stream<AnnotationInstance> annotations();

    default Stream<AnnotationInstance> annotations(String name) {
        return annotations()
            .filter(annotationInstance -> annotationInstance.name().equals(name));
    }
}

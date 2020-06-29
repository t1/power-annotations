package com.github.t1.annotations.impl;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.index.AnnotationInstance;
import com.github.t1.annotations.index.Index;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.t1.annotations.impl.Utils.toOptionalOrThrow;
import static java.util.stream.Collectors.toList;

class DirectAnnotations implements Annotations {

    private final Index index;
    private final Supplier<Stream<AnnotationInstance>> annotations;

    DirectAnnotations(Index index, Supplier<Stream<AnnotationInstance>> annotations) {
        this.index = index;
        this.annotations = annotations;
    }

    @Override public <T extends Annotation> Stream<T> all(Class<T> type) {
        return annotations.get()
            .filter(instance -> instance.name().equals(type.getName()))
            .map(AnnotationProxy::proxy)
            .map(type::cast);
    }

    @Override public List<Annotation> all() {
        return annotations.get()
            .map(AnnotationProxy::proxy)
            .collect(toList());
    }

    @Override public <T extends Annotation> Optional<T> get(Class<T> type) {
        return annotations.get()
            .filter(annotation -> annotation.name().equals(type.getName()))
            .map(AnnotationProxy::proxy)
            .map(type::cast)
            .collect(toOptionalOrThrow(list -> new PowerAnnotationsImplAmbiguousAnnotationResolutionException(type)));
    }
}

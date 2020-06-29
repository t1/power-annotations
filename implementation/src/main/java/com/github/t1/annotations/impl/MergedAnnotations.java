package com.github.t1.annotations.impl;

import com.github.t1.annotations.Annotations;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.t1.annotations.impl.Utils.toOptionalOrThrow;
import static java.util.stream.Collectors.toList;

class MergedAnnotations implements Annotations {
    private final List<Annotations> candidates;

    MergedAnnotations(List<Annotations> candidates) { this.candidates = candidates; }

    @Override public List<Annotation> all() {
        return candidates.stream()
            .flatMap(annotations -> annotations.all().stream())
            .collect(toList());
    }

    @Override public <T extends Annotation> Optional<T> get(Class<T> type) {
        return candidates.stream()
            .map(annotations -> annotations.get(type))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toOptionalOrThrow(list -> new PowerAnnotationsImplAmbiguousAnnotationResolutionException(type)));
    }

    @Override public <T extends Annotation> Stream<T> all(Class<T> type) {
        return candidates.stream().flatMap(annotations -> annotations.all(type));
    }
}

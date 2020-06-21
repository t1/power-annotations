package com.github.t1.annotations.impl;

import com.github.t1.annotations.Annotations;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

class EmptyAnnotations implements Annotations {
    @Override public List<Annotation> all() {
        return emptyList();
    }

    @Override public <T extends Annotation> Optional<T> get(Class<T> type) {
        return Optional.empty();
    }
}

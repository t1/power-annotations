package com.github.t1.annotations.impl;

import com.github.t1.annotations.Annotations;
import org.jboss.jandex.AnnotationInstance;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

class JandexAnnotations implements Annotations {

    private final List<Annotation> annotations;

    public JandexAnnotations(Collection<AnnotationInstance> annotations) {
        this.annotations = annotations.stream().map(this::proxy).collect(toList());
    }

    private Annotation proxy(AnnotationInstance jandexAnnotation) {
        return new AnnotationProxy(new JandexAnnotation(jandexAnnotation)).build();
    }


    @Override public List<Annotation> all() { return annotations; }

    @Override public <T extends Annotation> Optional<T> get(Class<T> type) {
        return all().stream()
            .filter(annotation -> annotation.annotationType().equals(type))
            .map(type::cast)
            .findFirst();
    }

    private static class JandexAnnotation implements AbstractAnnotation {
        private final AnnotationInstance jandexAnnotation;

        public JandexAnnotation(AnnotationInstance jandexAnnotation) { this.jandexAnnotation = jandexAnnotation; }

        @Override public Object property(String name) {
            return jandexAnnotation.value(name).value();
        }

        @Override public String toString() {
            return jandexAnnotation.toString(false);
        }

        @Override public String getTypeName() {
            return jandexAnnotation.name().toString();
        }
    }
}

package com.github.t1.annotations.impl;

import com.github.t1.annotations.AmbiguousAnnotationResolutionException;
import com.github.t1.annotations.Annotations;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.IndexView;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.t1.annotations.impl.CollectionUtils.toOptionalOrThrow;
import static java.util.stream.Collectors.toList;

class JandexAnnotations implements Annotations {

    private final Collection<AnnotationInstance> annotations;
    private final RepeatableResolver repeatableResolver;

    JandexAnnotations(IndexView jandex, Collection<AnnotationInstance> annotations) {
        this.annotations = annotations;
        this.repeatableResolver = new RepeatableResolver(jandex);
    }

    static Annotation proxy(AnnotationInstance jandexAnnotation) {
        return new AnnotationProxy(new JandexAnnotation(jandexAnnotation)).build();
    }


    @Override public <T extends Annotation> Stream<T> all(Class<T> type) {
        return annotations.stream()
            .flatMap(repeatableResolver::resolve)
            .filter(instance -> instance.name().toString().equals(type.getName()))
            .map(JandexAnnotations::proxy)
            .map(type::cast);
    }

    @Override public List<Annotation> all() {
        return annotations.stream()
            .flatMap(repeatableResolver::resolve)
            .map(JandexAnnotations::proxy)
            .collect(toList());
    }

    @Override public <T extends Annotation> Optional<T> get(Class<T> type) {
        return annotations.stream()
            .flatMap(repeatableResolver::resolve)
            .filter(annotation -> annotation.name().toString().equals(type.getName()))
            .map(JandexAnnotations::proxy)
            .map(type::cast)
            .collect(toOptionalOrThrow(list -> new AmbiguousAnnotationResolutionException("The annotation " + type.getName()
                + " is ambiguous on " + ". You should query it with `all` not `get`.") // TODO target info
            ));
    }

    private static class JandexAnnotation implements AbstractAnnotation {
        private final AnnotationInstance jandexAnnotation;

        JandexAnnotation(AnnotationInstance jandexAnnotation) { this.jandexAnnotation = jandexAnnotation; }

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

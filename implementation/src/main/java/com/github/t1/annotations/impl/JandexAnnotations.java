package com.github.t1.annotations.impl;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.RepeatableAnnotationAccessedWithGetException;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.Index;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

class JandexAnnotations implements Annotations {

    private final Index index; // we will need this when we completely stop using reflection
    private final Collection<AnnotationInstance> annotations;

    public JandexAnnotations(Index index, Collection<AnnotationInstance> annotations) {
        this.index = index;
        this.annotations = annotations;
    }


    static Annotation proxy(AnnotationInstance jandexAnnotation) {
        return new AnnotationProxy(new JandexAnnotation(jandexAnnotation)).build();
    }


    @Override public List<Annotation> all() {
        return annotations.stream()
            .map(JandexAnnotations::proxy)
            // TODO implement with Jandex: .flatMap(ReflectionAnnotationsLoader::resolveRepeatableAnnotations)
            .collect(toList());
    }

    @Override public <T extends Annotation> Optional<T> get(Class<T> type) {
        if (type.isAnnotationPresent(Repeatable.class)) // TODO use Jandex instead
            throw new RepeatableAnnotationAccessedWithGetException(type);
        return annotations.stream()
            .filter(annotation -> annotation.name().toString().equals(type.getName()))
            .map(JandexAnnotations::proxy)
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

package com.github.t1.annotations.impl;

import com.github.t1.annotations.AmbiguousAnnotationResolutionException;
import com.github.t1.annotations.Annotations;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.t1.annotations.impl.AnnotationProxy.loadClass;
import static com.github.t1.annotations.impl.CollectionUtils.toOptionalOrThrow;
import static java.util.stream.Collectors.toList;

class JandexAnnotations implements Annotations {

    private final Collection<AnnotationInstance> annotations;

    JandexAnnotations(Collection<AnnotationInstance> annotations) {
        this.annotations = annotations;
    }

    static Annotation proxy(AnnotationInstance jandexAnnotation) {
        return new AnnotationProxy(new JandexAnnotation(jandexAnnotation)).build();
    }


    @Override public <T extends Annotation> Stream<T> all(Class<T> type) {
        return annotations.stream()
            .flatMap(JandexAnnotations::resolveRepeatableAnnotations)
            .filter(instance -> instance.name().toString().equals(type.getName()))
            .map(JandexAnnotations::proxy)
            .map(type::cast)
            ;
    }

    @Override public List<Annotation> all() {
        return annotations.stream()
            .flatMap(JandexAnnotations::resolveRepeatableAnnotations)
            .map(JandexAnnotations::proxy)
            .collect(toList());
    }

    @Override public <T extends Annotation> Optional<T> get(Class<T> type) {
        return annotations.stream()
            .flatMap(JandexAnnotations::resolveRepeatableAnnotations)
            .filter(annotation -> annotation.name().toString().equals(type.getName()))
            .map(JandexAnnotations::proxy)
            .map(type::cast)
            .collect(toOptionalOrThrow(list -> new AmbiguousAnnotationResolutionException("The annotation " + type.getName()
                + " is ambiguous on " + ". You should query it with `all` not `get`.") // TODO target info
            ));
    }

    private static Stream<AnnotationInstance> resolveRepeatableAnnotations(AnnotationInstance annotation) {
        if (isRepeatable(annotation))
            return resolveRepeatable(annotation);
        return Stream.of(annotation);
    }

    private static boolean isRepeatable(AnnotationInstance annotation) {
        @SuppressWarnings("unchecked")
        Class<? extends Annotation> type = (Class<? extends Annotation>) loadClass(annotation.name().toString());
        for (Method method : type.getMethods())
            if ("value".equals(method.getName())
                && method.getReturnType().isArray()
                && method.getReturnType().getComponentType().isAnnotation()
                && method.getReturnType().getComponentType().isAnnotationPresent(Repeatable.class))
                return true;
        return false;
        // TODO implement with Jandex
        // return annotation.values().size() == 1
        //     && annotation.values().get(0).name().equals("value")
        //     && annotation.value().kind() == ARRAY
        //     && annotation.value().componentKind() == NESTED
        //     && ...
    }

    private static Stream<AnnotationInstance> resolveRepeatable(AnnotationInstance annotation) {
        return Stream.of((AnnotationValue[]) annotation.value().value())
            .map(annotationValue -> (AnnotationInstance) annotationValue.value());
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

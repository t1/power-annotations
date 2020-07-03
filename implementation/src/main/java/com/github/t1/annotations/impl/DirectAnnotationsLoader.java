package com.github.t1.annotations.impl;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.AnnotationsLoader;
import com.github.t1.annotations.index.AnnotationInstance;
import com.github.t1.annotations.index.Index;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.t1.annotations.impl.Utils.toOptionalOrThrow;
import static java.util.stream.Collectors.toList;

class DirectAnnotationsLoader extends AnnotationsLoader {
    private final Index index;
    private final AnnotationsLoader delegate;

    DirectAnnotationsLoader(Index index, AnnotationsLoader delegate) {
        this.index = index;
        this.delegate = delegate;
    }

    @Override public Annotations onType(Class<?> type) {
        return index.classInfo(type)
            .map(classInfo -> (Annotations) new DirectAnnotations(classInfo::annotations))
            .orElseGet(() -> delegate.onType(type));
    }

    @Override public Annotations onField(Class<?> type, String fieldName) {
        return index.classInfo(type)
            .map(classInfo -> classInfo.field(fieldName)
                .map(field -> (Annotations) new DirectAnnotations(field::annotations))
                .orElseThrow(() -> new FieldNotFoundException(fieldName, type)))
            .orElseGet(() -> delegate.onField(type, fieldName));
    }

    @Override public Annotations onMethod(Class<?> type, String methodName, Class<?>... argTypes) {
        return index.classInfo(type)
            .map(classInfo -> classInfo.findMethod(methodName, argTypes)
                .map(method -> (Annotations) new DirectAnnotations(method::annotations))
                .orElseThrow(() -> new MethodNotFoundException(methodName, argTypes, type)))
            .orElseGet(() -> delegate.onMethod(type, methodName, argTypes));
    }

    private static class DirectAnnotations implements Annotations {
        private final Supplier<Stream<AnnotationInstance>> annotations;

        DirectAnnotations(Supplier<Stream<AnnotationInstance>> annotations) {
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
}

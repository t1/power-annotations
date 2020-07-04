package com.github.t1.annotations.impl;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.AnnotationsLoader;
import com.github.t1.annotations.index.AnnotationInstance;
import com.github.t1.annotations.index.ClassInfo;
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
    private final AnnotationsLoader other;

    DirectAnnotationsLoader(Index index, AnnotationsLoader other) {
        this.index = index;
        this.other = other;
    }

    @Override public Annotations onType(Class<?> type) {
        ClassInfo classInfo = index.classInfo(type);
        return new DirectAnnotations(classInfo::annotations);
    }

    @Override public Annotations onField(Class<?> type, String fieldName) {
        return index.classInfo(type).field(fieldName)
            .map(field -> (Annotations) new DirectAnnotations(field::annotations))
            .orElseThrow(() -> new FieldNotFoundException(fieldName, type));
    }

    @Override public Annotations onMethod(Class<?> type, String methodName, Class<?>... argTypes) {
        return index.classInfo(type).method(methodName, argTypes)
            .map(method -> (Annotations) new DirectAnnotations(method::annotations))
            .orElseThrow(() -> new MethodNotFoundException(methodName, argTypes, type));
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

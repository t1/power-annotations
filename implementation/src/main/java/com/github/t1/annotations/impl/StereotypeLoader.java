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

import static com.github.t1.annotations.impl.Utils.or;
import static com.github.t1.annotations.impl.Utils.toOptionalOrThrow;
import static com.github.t1.annotations.impl.Utils.toStream;
import static java.util.stream.Collectors.toList;

class StereotypeLoader extends AnnotationsLoader {
    private final Index index;
    private final AnnotationsLoader loader;

    StereotypeLoader(Index index, AnnotationsLoader loader) {
        this.index = index;
        this.loader = loader;
    }

    @Override public Annotations onType(Class<?> type) {
        return index.classInfo(type)
            .map(classInfo -> (Annotations) new StereotypeAnnotations(
                classInfo.simpleName(), classInfo::annotations, loader.onType(type)))
            .orElseGet(() -> loader.onType(type));
    }

    @Override public Annotations onField(Class<?> type, String fieldName) {
        return index.classInfo(type)
            .flatMap(classInfo -> classInfo.field(fieldName))
            .map(fieldInfo -> (Annotations) new StereotypeAnnotations(
                fieldInfo.toString(), fieldInfo::annotations, loader.onField(type, fieldName)))
            .orElseGet(() -> loader.onField(type, fieldName));
    }

    @Override public Annotations onMethod(Class<?> type, String methodName, Class<?>... argTypes) {
        return index.classInfo(type)
            .flatMap(classInfo -> classInfo.method(methodName, argTypes))
            .map(methodInfo -> (Annotations) new StereotypeAnnotations(
                methodInfo.toString(), methodInfo::annotations, loader.onMethod(type, methodName, argTypes)))
            .orElseGet(() -> loader.onMethod(type, methodName, argTypes));
    }

    private static class StereotypeAnnotations implements Annotations {
        private final String targetName;
        private final Supplier<Stream<AnnotationInstance>> targetAnnotations;
        private final Annotations other;

        StereotypeAnnotations(
            String targetName,
            Supplier<Stream<AnnotationInstance>> targetAnnotations,
            Annotations other) {
            this.targetName = targetName;
            this.targetAnnotations = targetAnnotations;
            this.other = other;
        }

        @Override public List<Annotation> all() {
            return Stream.concat(
                other.all().stream(),
                stereotypes()
                    .flatMap(ClassInfo::annotations)
                    .map(AnnotationProxy::proxy))
                .collect(toList());
        }

        @Override public <T extends Annotation> Optional<T> get(Class<T> type) {
            return or(
                other.get(type),
                () -> annotationsFromStereotypes(type)
                    .collect(toOptionalOrThrow(list -> new PowerAnnotationsImplAmbiguousAnnotationResolutionException(type, targetName))));
        }

        @Override public <T extends Annotation> Stream<T> all(Class<T> type) {
            return Stream.concat(other.all(type), annotationsFromStereotypes(type));
        }

        private <T extends Annotation> Stream<T> annotationsFromStereotypes(Class<T> type) {
            return stereotypes()
                .flatMap(classInfo -> classInfo.annotations(type.getName()))
                .map(AnnotationProxy::proxy)
                .map(type::cast);
        }

        private Stream<ClassInfo> stereotypes() {
            return stereotypes(targetAnnotations.get());
        }

        private Stream<ClassInfo> stereotypes(Stream<AnnotationInstance> annotations) {
            return annotations
                .flatMap(annotationInstance -> toStream(annotationInstance.type()))
                .filter(this::isStereotype)
                .flatMap(this::andIndirectStereotypes);
        }

        private boolean isStereotype(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") ClassInfo classInfo) {
            return classInfo.annotations().anyMatch(this::isAnyStereotypeName);
        }

        private boolean isAnyStereotypeName(AnnotationInstance instance) {
            return instance.name().endsWith(".Stereotype");
        }

        private Stream<ClassInfo> andIndirectStereotypes(ClassInfo stereotype) {
            return Stream.concat(Stream.of(stereotype), stereotypes(stereotype.annotations()));
        }
    }
}

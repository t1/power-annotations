package com.github.t1.annotations.impl;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.AnnotationsLoader;
import com.github.t1.annotations.index.Annotatable;
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
import static java.util.stream.Collectors.toList;

class StereotypeLoader extends AnnotationsLoader {
    private final Index index;
    private final AnnotationsLoader other;

    StereotypeLoader(Index index, AnnotationsLoader other) {
        this.index = index;
        this.other = other;
    }

    @Override public Annotations onType(Class<?> type) {
        ClassInfo classInfo = index.classInfo(type);
        return new StereotypeAnnotations(classInfo.simpleName(), classInfo::annotations, other.onType(type));
    }

    @Override public Annotations onField(Class<?> type, String fieldName) {
        Annotations otherAnnotations = other.onField(type, fieldName);
        return index.classInfo(type).field(fieldName)
            .map(fieldInfo -> stereotypeAnnotations(fieldInfo, otherAnnotations))
            .orElse(otherAnnotations);
    }

    @Override public Annotations onMethod(Class<?> type, String methodName, Class<?>... argTypes) {
        Annotations otherAnnotations = other.onMethod(type, methodName, argTypes);
        return index.classInfo(type).method(methodName, argTypes)
            .map(methodInfo -> stereotypeAnnotations(methodInfo, otherAnnotations))
            .orElse(otherAnnotations);
    }

    private Annotations stereotypeAnnotations(Annotatable annotatable, Annotations otherAnnotations) {
        return new StereotypeAnnotations(annotatable.toString(), annotatable::annotations, otherAnnotations);
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
                .map(AnnotationInstance::type)
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

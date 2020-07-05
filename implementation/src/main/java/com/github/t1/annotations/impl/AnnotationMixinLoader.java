package com.github.t1.annotations.impl;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.AnnotationsLoader;
import com.github.t1.annotations.MixinFor;
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

class AnnotationMixinLoader extends AnnotationsLoader {
    private final Index index;
    private final AnnotationsLoader other;

    AnnotationMixinLoader(Index index, AnnotationsLoader other) {
        this.index = index;
        this.other = other;
    }

    @Override public Annotations onType(Class<?> type) {
        ClassInfo classInfo = index.classInfo(type);
        return new AnnotationMixinAnnotations(classInfo.simpleName(), classInfo::annotations, other.onType(type));
    }

    @Override public Annotations onField(Class<?> type, String fieldName) {
        Annotations otherAnnotations = other.onField(type, fieldName);
        return index.classInfo(type).field(fieldName)
            .map(fieldInfo -> annotationMixinAnnotations(fieldInfo, otherAnnotations))
            .orElse(otherAnnotations);
    }

    @Override public Annotations onMethod(Class<?> type, String methodName, Class<?>... argTypes) {
        Annotations otherAnnotations = other.onMethod(type, methodName, argTypes);
        return index.classInfo(type).method(methodName, argTypes)
            .map(methodInfo -> annotationMixinAnnotations(methodInfo, otherAnnotations))
            .orElse(otherAnnotations);
    }

    private Annotations annotationMixinAnnotations(Annotatable annotatable, Annotations other) {
        return new AnnotationMixinAnnotations(annotatable.toString(), annotatable::annotations, other);
    }

    private class AnnotationMixinAnnotations implements Annotations {
        private final String targetName;
        private final Supplier<Stream<AnnotationInstance>> targetAnnotations;
        private final Annotations other;

        AnnotationMixinAnnotations(
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
                targetAnnotations.get()
                    .flatMap(annotationInstance -> mixinsFor(annotationInstance.type()))
                    .map(AnnotationProxy::proxy))
                .collect(toList());
        }

        @Override public <T extends Annotation> Optional<T> get(Class<T> type) {
            return or(
                other.get(type),
                () -> fromMixins(type)
                    .collect(toOptionalOrThrow(list -> new PowerAnnotationsImplAmbiguousAnnotationResolutionException(type, targetName))));
        }

        @Override public <T extends Annotation> Stream<T> all(Class<T> type) {
            return Stream.concat(other.all(type), fromMixins(type));
        }

        private <T extends Annotation> Stream<T> fromMixins(Class<T> type) {
            return targetAnnotations.get()
                .flatMap(annotationInstance -> mixinsFor(annotationInstance.type()))
                .map(AnnotationInstance::targetClass)
                .flatMap(classInfo -> classInfo.annotations(type.getName()))
                .map(AnnotationProxy::proxy)
                .map(type::cast);
        }

        private Stream<AnnotationInstance> mixinsFor(ClassInfo type) {
            return index.annotations(MixinFor.class)
                .filter(mixin -> mixin.value("value").classValue().equals(type));
        }
    }
}

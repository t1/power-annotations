package com.github.t1.annotations.impl;

import com.github.t1.annotations.AmbiguousAnnotationResolutionException;
import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.AnnotationsLoader;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.t1.annotations.impl.CollectionUtils.toOptionalOrThrow;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

class StereotypeLoader extends AnnotationsLoader {
    private final IndexView jandex;
    private final AnnotationsLoader loader;

    StereotypeLoader(IndexView jandex, AnnotationsLoader loader) {
        this.jandex = jandex;
        this.loader = loader;
    }

    @Override public Annotations onType(Class<?> type) {
        ClassInfo classInfo = jandex.getClassByName(DotName.createSimple(type.getName()));
        return (classInfo == null) ? loader.onType(type)
            : new StereotypeAnnotations(classInfo.simpleName(), classInfo::classAnnotations, loader.onType(type));
    }

    @Override public Annotations onField(Class<?> type, String fieldName) {
        ClassInfo classInfo = jandex.getClassByName(DotName.createSimple(type.getName()));
        FieldInfo fieldInfo = classInfo.field(fieldName);
        return (fieldInfo == null) ? loader.onField(type, fieldName)
            : new StereotypeAnnotations(fieldInfo.toString(), fieldInfo::annotations, loader.onField(type, fieldName));
    }

    @Override public Annotations onMethod(Class<?> type, String methodName, Class<?>... argTypes) {
        ClassInfo classInfo = jandex.getClassByName(DotName.createSimple(type.getName()));
        MethodInfo methodInfo = classInfo.method(methodName);
        return (methodInfo == null) ? loader.onMethod(type, methodName, argTypes)
            : new StereotypeAnnotations(methodInfo.toString(), methodInfo::annotations, loader.onMethod(type, methodName, argTypes));
    }

    private class StereotypeAnnotations implements Annotations {
        private final String targetName;
        private final Supplier<Collection<AnnotationInstance>> targetAnnotations;
        private final Annotations other;

        StereotypeAnnotations(
            String targetName,
            Supplier<Collection<AnnotationInstance>> targetAnnotations,
            Annotations other) {
            this.targetName = targetName;
            this.targetAnnotations = targetAnnotations;
            this.other = other;
        }

        @Override public List<Annotation> all() {
            return Stream.concat(
                other.all().stream(),
                annotationsFromStereotypes()
                    .flatMap(classInfo -> classInfo.classAnnotations().stream())
                    .map(JandexAnnotations::proxy))
                .collect(toList());
        }

        @Override public <T extends Annotation> Optional<T> get(Class<T> type) {
            Optional<T> direct = other.get(type);
            // Optional.or requires JDK 9+
            return direct.isPresent() ? direct
                : annotationsFromStereotypes(type)
                .collect(toOptionalOrThrow(list ->
                    new AmbiguousAnnotationResolutionException("The annotation " + type.getName()
                        + " is ambiguous on " + targetName + ". You should query it with `all` not `get`.")
                ));
        }

        @Override public <T extends Annotation> Stream<T> all(Class<T> type) {
            return Stream.concat(other.all(type), annotationsFromStereotypes(type));
        }

        private <T extends Annotation> Stream<T> annotationsFromStereotypes(Class<T> type) {
            return annotationsFromStereotypes()
                .flatMap(classInfo -> annotations(type, classInfo))
                .map(JandexAnnotations::proxy)
                .map(type::cast);
        }

        private Stream<ClassInfo> annotationsFromStereotypes() {
            return targetAnnotations.get().stream()
                .map(this::getType)
                .filter(Objects::nonNull)
                .filter(this::isStereotype);
        }

        private ClassInfo getType(AnnotationInstance annotation) {
            return jandex.getClassByName(annotation.name());
        }

        private boolean isStereotype(ClassInfo type) {
            return type.classAnnotations().stream().anyMatch(this::isAnyStereotypeName);
        }

        private boolean isAnyStereotypeName(AnnotationInstance instance) {
            return instance.name().toString().endsWith(".Stereotype");
        }

        private Stream<AnnotationInstance> annotations(Class<?> type, ClassInfo classInfo) {
            DotName typeName = DotName.createSimple(type.getName());
            return classInfo.annotations().getOrDefault(typeName, emptyList()).stream();
        }
    }
}

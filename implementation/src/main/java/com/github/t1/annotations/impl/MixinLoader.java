package com.github.t1.annotations.impl;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.AnnotationsLoader;
import com.github.t1.annotations.MixinFor;
import com.github.t1.annotations.index.AnnotationInstance;
import com.github.t1.annotations.index.Index;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.t1.annotations.impl.Utils.or;
import static com.github.t1.annotations.impl.Utils.toOptionalOrThrow;
import static com.github.t1.annotations.impl.Utils.toStream;
import static java.util.stream.Collectors.toList;

class MixinLoader extends AnnotationsLoader {

    private final Index index;
    private final AnnotationsLoader delegate;

    MixinLoader(Index index, AnnotationsLoader delegate) {
        this.index = index;
        this.delegate = delegate;
    }

    @Override public Annotations onType(Class<?> type) {
        List<Annotations> candidates = mixinsFor(type)
            .map(AnnotationInstance::targetClass)
            .map(classInfo -> new MixinAnnotations(
                classInfo::annotations, classInfo::annotations, delegate.onType(type)))
            .collect(toList());
        return (candidates.isEmpty()) ? delegate.onType(type)
            : new MergedAnnotations(candidates);
    }

    @Override public Annotations onField(Class<?> type, String fieldName) {
        List<Annotations> candidates = mixinsFor(type)
            .map(AnnotationInstance::targetClass)
            .flatMap(targetClass -> toStream(targetClass.field(fieldName)))
            .map(fieldInfo -> (Annotations) new MixinAnnotations(
                fieldInfo::annotations, fieldInfo::annotations, delegate.onField(type, fieldName)))
            .collect(toList());
        return (candidates.isEmpty()) ? delegate.onField(type, fieldName)
            : new MergedAnnotations(candidates);
    }

    @Override public Annotations onMethod(Class<?> type, String methodName, Class<?>... argTypes) {
        List<Annotations> candidates = mixinsFor(type)
            .map(AnnotationInstance::targetClass)
            .flatMap(classInfo -> toStream(classInfo.method(methodName, argTypes)))
            .map(methodInfo -> (Annotations) new MixinAnnotations(
                methodInfo::annotations, methodInfo::annotations, delegate.onMethod(type, methodName, argTypes)))
            .collect(toList());
        return (candidates.isEmpty()) ? delegate.onMethod(type, methodName, argTypes)
            : new MergedAnnotations(candidates);
    }

    private Stream<AnnotationInstance> mixinsFor(Class<?> type) {
        return index.annotations(MixinFor.class)
            .filter(mixin -> isMixinFor(mixin, type));
    }

    private boolean isMixinFor(AnnotationInstance mixin, Class<?> type) {
        return mixin.value("value").classValue().getName().equals(type.getName());
    }

    private static class MixinAnnotations implements Annotations {
        private final Supplier<Stream<AnnotationInstance>> all;
        private final Function<String, Stream<AnnotationInstance>> get;
        private final Annotations other;

        MixinAnnotations(Supplier<Stream<AnnotationInstance>> all,
                         Function<String, Stream<AnnotationInstance>> get,
                         Annotations other) {
            this.all = all;
            this.get = get;
            this.other = other;
        }

        @Override public List<Annotation> all() {
            return Stream.concat(
                all.get().map(AnnotationProxy::proxy),
                other.all().stream())
                .collect(toList());
        }

        @Override public <T extends Annotation> Optional<T> get(Class<T> type) {
            return or(
                get.apply(type.getName())
                    .map(AnnotationProxy::proxy)
                    .map(type::cast)
                    .collect(toOptionalOrThrow(list -> new PowerAnnotationsImplAmbiguousAnnotationResolutionException(type))),
                () -> other.get(type));
        }

        @Override public <T extends Annotation> Stream<T> all(Class<T> type) {
            return Stream.concat(
                all.get()
                    .filter(instance -> instance.name().equals(type.getName()))
                    .map(AnnotationProxy::proxy)
                    .map(type::cast),
                other.all(type));
        }
    }
}

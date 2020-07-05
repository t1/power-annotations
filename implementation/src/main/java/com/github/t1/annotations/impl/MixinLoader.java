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
        Annotations otherAnnotations = delegate.onType(type);
        return mixinAnnotations(type, otherAnnotations, Optional::of);
    }

    @Override public Annotations onField(Class<?> type, String fieldName) {
        Annotations otherAnnotations = delegate.onField(type, fieldName);
        return mixinAnnotations(type, otherAnnotations, classInfo -> classInfo.field(fieldName));
    }

    @Override public Annotations onMethod(Class<?> type, String methodName, Class<?>... argTypes) {
        Annotations otherAnnotations = delegate.onMethod(type, methodName, argTypes);
        return mixinAnnotations(type, otherAnnotations, classInfo -> classInfo.method(methodName, argTypes));
    }

    private Annotations mixinAnnotations(Class<?> type, Annotations otherAnnotations, Function<ClassInfo, Optional<? extends Annotatable>> finder) {
        List<Annotations> candidates = mixinsFor(type)
            .map(AnnotationInstance::targetClass)
            .flatMap(classInfo -> toStream(finder.apply(classInfo)))
            .map(annotatable -> mixinAnnotations(annotatable, otherAnnotations))
            .collect(toList());
        return (candidates.isEmpty()) ? otherAnnotations : new MergedAnnotations(candidates);
    }

    private Stream<AnnotationInstance> mixinsFor(Class<?> type) {
        return index.annotations(MixinFor.class)
            .filter(mixin -> isMixinFor(mixin, type));
    }

    private boolean isMixinFor(AnnotationInstance mixin, Class<?> type) {
        return mixin.value("value").classValue().name().equals(type.getName());
    }

    private Annotations mixinAnnotations(Annotatable annotatable, Annotations otherAnnotations) {
        return new MixinAnnotations(annotatable::annotations, annotatable::annotations, otherAnnotations);
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

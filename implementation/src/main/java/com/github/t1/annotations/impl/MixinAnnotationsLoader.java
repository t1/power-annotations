package com.github.t1.annotations.impl;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.AnnotationsLoader;
import com.github.t1.annotations.MixinFor;
import com.github.t1.annotations.RepeatableAnnotationAccessedWithGetException;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.t1.annotations.impl.JandexAnnotations.proxy;
import static com.github.t1.annotations.impl.JandexAnnotationsLoader.findMethod;
import static java.util.stream.Collectors.toList;

public class MixinAnnotationsLoader extends AnnotationsLoader {

    public static AnnotationsLoader of(Index index, AnnotationsLoader delegate) {
        if (index == null)
            return delegate;
        return new MixinAnnotationsLoader(index, delegate);
    }

    private final Index index;
    private final AnnotationsLoader delegate;

    private MixinAnnotationsLoader(Index index, AnnotationsLoader delegate) {
        this.index = index;
        this.delegate = delegate;
    }

    @Override public Annotations onType(Class<?> type) {
        return mixinFor(type)
            .map(mixin -> mixin.target().asClass())
            .map(classInfo -> (Annotations) new MixinAnnotations(classInfo::classAnnotations, classInfo::classAnnotation, delegate.onType(type)))
            .orElseGet(() -> delegate.onType(type));
    }

    @Override public Annotations onField(Class<?> type, String fieldName) {
        return mixinFor(type)
            .map(mixin -> Optional.ofNullable(mixin.target().asClass().field(fieldName)))
            .flatMap(optional -> optional
                .map(fieldInfo -> (Annotations) new MixinAnnotations(fieldInfo::annotations, fieldInfo::annotation, delegate.onField(type, fieldName))))
            .orElseGet(() -> delegate.onField(type, fieldName));
    }

    @Override public Annotations onMethod(Class<?> type, String methodName, Class<?>... argTypes) {
        return mixinFor(type)
            .map(mixin -> mixin.target().asClass())
            .flatMap(classInfo -> findMethod(classInfo, methodName, argTypes)
                .map(methodInfo -> (Annotations) new MixinAnnotations(methodInfo::annotations, methodInfo::annotation, delegate.onMethod(type, methodName, argTypes))))
            .orElseGet(() -> delegate.onMethod(type, methodName, argTypes));
    }

    private Optional<AnnotationInstance> mixinFor(Class<?> type) {
        List<AnnotationInstance> list = index.getAnnotations(MIXIN_FOR).stream()
            .filter(mixin -> matches(mixin, type))
            .collect(toList());
        switch (list.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(list.get(0));
            default:
                throw new RuntimeException("multiple mixins for " + type + ": " + list.stream()
                    .map(AnnotationInstance::target).collect(toList()));
        }
    }

    private boolean matches(AnnotationInstance mixin, Class<?> type) {
        return mixin.value().asClass().name().toString().equals(type.getName());
    }

    private static class MixinAnnotations implements Annotations {
        private final Supplier<Collection<AnnotationInstance>> all;
        private final Function<DotName, AnnotationInstance> get;
        private final Annotations delegate;

        public MixinAnnotations(Supplier<Collection<AnnotationInstance>> all,
                                Function<DotName, AnnotationInstance> get,
                                Annotations delegate) {
            this.all = all;
            this.get = get;
            this.delegate = delegate;
        }

        @Override public List<Annotation> all() {
            return Stream.concat(
                all.get().stream().map(JandexAnnotations::proxy),
                delegate.all().stream())
                .collect(toList());
        }

        @Override public <T extends Annotation> Optional<T> get(Class<T> type) {
            if (type.isAnnotationPresent(Repeatable.class)) // TODO use Jandex instead
                throw new RepeatableAnnotationAccessedWithGetException(type);
            AnnotationInstance targetAnnotation = get.apply(DotName.createSimple(type.getName()));
            if (targetAnnotation == null)
                return delegate.get(type);
            @SuppressWarnings("unchecked")
            T proxy = (T) proxy(targetAnnotation);
            return Optional.of(proxy);
        }
    }

    private static final DotName MIXIN_FOR = DotName.createSimple(MixinFor.class.getName());
}

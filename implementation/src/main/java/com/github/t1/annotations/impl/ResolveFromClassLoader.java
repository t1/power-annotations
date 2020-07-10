package com.github.t1.annotations.impl;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.AnnotationsLoader;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.t1.annotations.impl.Utils.or;
import static java.util.stream.Collectors.toList;

class ResolveFromClassLoader extends AnnotationsLoader {
    private final AnnotationsLoader loader;
    private final AnnotationsLoader other;

    public ResolveFromClassLoader(AnnotationsLoader loader, AnnotationsLoader other) {
        this.loader = loader;
        this.other = other;
    }

    @Override public Annotations onType(Class<?> type) { return other.onType(type); }

    @Override public Annotations onField(Class<?> type, String fieldName) {
        return new ResolveFromClassAnnotations(other.onField(type, fieldName), type);
    }

    @Override public Annotations onMethod(Class<?> type, String methodName, Class<?>... argTypes) {
        return new ResolveFromClassAnnotations(other.onMethod(type, methodName, argTypes), type);
    }

    private class ResolveFromClassAnnotations implements Annotations {
        private final Annotations memberAnnotations;
        private final Annotations classAnnotations;

        public ResolveFromClassAnnotations(Annotations memberAnnotations, Class<?> declaringClass) {
            this.memberAnnotations = memberAnnotations;
            this.classAnnotations = loader.onType(declaringClass);
        }

        @Override public List<Annotation> all() {
            return memberAnnotations.all();
        }

        @Override public <T extends Annotation> Optional<T> get(Class<T> type) {
            return or(memberAnnotations.get(type),
                () -> classAnnotations.get(type));
        }

        @Override public <T extends Annotation> Stream<T> all(Class<T> type) {
            List<T> onMember = memberAnnotations.all(type).collect(toList());
            return onMember.isEmpty()
                ? classAnnotations.all(type)
                : onMember.stream();
        }
    }
}

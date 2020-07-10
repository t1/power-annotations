package com.github.t1.annotations.impl;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.AnnotationsLoader;
import com.github.t1.annotations.index.Index;

public class AnnotationsLoaderImpl extends AnnotationsLoader {
    final Index index;
    private final AnnotationsLoader loader;

    /** Used by the ServiceLoader */
    @SuppressWarnings("unused")
    public AnnotationsLoaderImpl() {
        this(Index.load());
    }

    /** visible for testing: we need to load different index files */
    public AnnotationsLoaderImpl(Index index) {
        this.index = index;
        this.loader = buildLoader();
    }

    private AnnotationsLoader buildLoader() {
        AnnotationsLoader stack = new DirectAnnotationsLoader(index);
        stack = new ResolveFromClassLoader(this, stack);
        stack = new StereotypeLoader(index, stack);
        stack = new MixinLoader(index, stack);
        stack = new AnnotationMixinLoader(index, stack);
        return stack;
    }

    @Override public Annotations onType(Class<?> type) {
        return loader.onType(type);
    }

    @Override public Annotations onField(Class<?> type, String fieldName) {
        return loader.onField(type, fieldName);
    }

    @Override public Annotations onMethod(Class<?> type, String methodName, Class<?>... argTypes) {
        return loader.onMethod(type, methodName, argTypes);
    }
}

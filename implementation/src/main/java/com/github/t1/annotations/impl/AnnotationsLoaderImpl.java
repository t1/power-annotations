package com.github.t1.annotations.impl;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.AnnotationsLoader;
import org.jboss.jandex.IndexView;

public class AnnotationsLoaderImpl extends AnnotationsLoader {
    private final IndexView jandex;
    private final AnnotationsLoader loader;

    /** Used by the ServiceLoader */
    @SuppressWarnings("unused")
    public AnnotationsLoaderImpl() {
        this(Jandexer.init());
    }

    /** visible for testing: we need to load different index files */
    public AnnotationsLoaderImpl(IndexView jandex) {
        this.jandex = jandex;
        this.loader = buildLoader();
    }

    private AnnotationsLoader buildLoader() {
        AnnotationsLoader stack = new EmptyAnnotationsLoader();
        stack = new JandexAnnotationsLoader(jandex, stack);
        stack = new StereotypeLoader(jandex, stack);
        stack = new MixinAnnotationsLoader(jandex, stack);
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

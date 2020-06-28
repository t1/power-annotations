package com.github.t1.annotations.impl;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.AnnotationsLoader;
import org.jboss.jandex.IndexView;

public class AnnotationsLoaderImpl extends AnnotationsLoader {
    /** visible for testing: we need to load a different index file */
    public static IndexView JANDEX;

    private final AnnotationsLoader loader;

    /** Used by the ServiceLoader */
    @SuppressWarnings("unused")
    public AnnotationsLoaderImpl() {
        if (JANDEX == null)
            JANDEX = Jandexer.initFromResource("META-INF/jandex.idx");
        this.loader = buildLoader();
    }

    private AnnotationsLoader buildLoader() {
        AnnotationsLoader stack = new EmptyAnnotationsLoader();
        stack = new JandexAnnotationsLoader(stack);
        stack = new MixinAnnotationsLoader(stack);
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

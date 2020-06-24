package com.github.t1.annotations.impl;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.AnnotationsLoader;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.t1.annotations.impl.AnnotationProxy.getClassLoader;

public class AnnotationsLoaderImpl extends AnnotationsLoader {

    private final AtomicBoolean skipReflection = new AtomicBoolean(false);
    private final AnnotationsLoader loader;

    /** Used by the ServiceLoader */
    @SuppressWarnings("unused")
    public AnnotationsLoaderImpl() {
        this("META-INF/jandex.idx");
    }

    /** visible for testing: we need to try to load an unknown index file */
    public AnnotationsLoaderImpl(String indexResource) {
        try (InputStream inputStream = getClassLoader().getResourceAsStream(indexResource)) {
            this.loader = buildLoader((inputStream == null) ? null : new IndexReader(inputStream).read());
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException("can't read " + indexResource, e);
        }
    }

    /** visible for testing: we need to load a different index file */
    public AnnotationsLoaderImpl(InputStream inputStream) {
        try {
            this.loader = buildLoader(new IndexReader(inputStream).read());
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException("can't read Jandex input stream", e);
        }
    }

    private AnnotationsLoader buildLoader(Index index) {
        AnnotationsLoader stack = new EmptyAnnotationsLoader();
        stack = new ReflectionAnnotationsLoader(skipReflection, stack);
        stack = JandexAnnotationsLoader.of(index, stack);
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

    /** visible for testing: it's not easy to simulate reflection being disabled */
    public void withoutReflection() {
        this.skipReflection.set(true);
    }
}

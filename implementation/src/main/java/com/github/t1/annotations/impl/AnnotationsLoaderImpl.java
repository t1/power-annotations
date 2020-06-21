package com.github.t1.annotations.impl;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.AnnotationsLoader;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static com.github.t1.annotations.impl.AnnotationProxy.getClassLoader;
import static com.github.t1.annotations.impl.ReflectionAnnotations.signature;

public
class AnnotationsLoaderImpl extends AnnotationsLoader {

    private final Index index;
    private boolean fallbackToReflection = true;

    /** Used by the ServiceLoader */
    @SuppressWarnings("unused")
    public AnnotationsLoaderImpl() {
        this("META-INF/jandex.idx");
    }

    public AnnotationsLoaderImpl(String indexResource) {
        try (InputStream inputStream = getClassLoader().getResourceAsStream(indexResource)) {
            this.index = new IndexReader(inputStream).read();
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException("can't read " + indexResource, e);
        }
    }

    public AnnotationsLoaderImpl(InputStream inputStream) {
        try {
            this.index = new IndexReader(inputStream).read();
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException("can't read Jandex input stream", e);
        }
    }

    @Override public Annotations onType(Class<?> type) {
        ClassInfo classInfo = info(type);
        if (classInfo == null) {
            if (fallbackToReflection)
                return ReflectionAnnotations.on(type);
            return new EmptyAnnotations();
        }
        return new JandexAnnotations(classInfo.classAnnotations());
    }

    @Override public Annotations onField(Class<?> type, String fieldName) {
        ClassInfo classInfo = info(type);
        if (classInfo == null) {
            if (fallbackToReflection)
                return ReflectionAnnotations.onField(type, fieldName);
            return new EmptyAnnotations();
        }
        FieldInfo field = classInfo.field(fieldName);
        if (field == null)
            throw new RuntimeException("no field '" + fieldName + "' in " + type);
        return new JandexAnnotations(field.annotations());
    }

    @Override public Annotations onMethod(Class<?> type, String methodName, Class<?>... argTypes) {
        ClassInfo classInfo = info(type);
        if (classInfo == null) {
            if (fallbackToReflection)
                return ReflectionAnnotations.onMethod(type, methodName, argTypes);
            return new EmptyAnnotations();
        }
        return findMethod(classInfo, methodName, argTypes)
            .map(method -> new JandexAnnotations(method.annotations()))
            .orElseThrow(() ->
                new RuntimeException("no method " + signature(methodName, argTypes) + " in " + type));
    }

    private Optional<MethodInfo> findMethod(ClassInfo classInfo, String methodName, Class<?>... argTypes) {
        return classInfo.methods().stream()
            .filter(methodInfo -> methodInfo.name().equals(methodName))
            .filter(methodInfo -> methodInfo.parameters().size() == argTypes.length)
            .filter(methodInfo -> matchTypes(methodInfo.parameters(), argTypes))
            .findFirst();
    }

    private boolean matchTypes(List<Type> parameters, Class<?>[] argTypes) {
        for (int i = 0; i < parameters.size(); i++) {
            Type paramType = parameters.get(i);
            Class<?> argType = argTypes[i];
            if (!paramType.name().toString().equals(argType.getName()))
                return false;
        }
        return true;
    }

    private ClassInfo info(Class<?> type) {
        DotName name = DotName.createSimple(type.getName());
        return index.getClassByName(name);
    }

    /** visible for testing: it's not easy to simulate reflection being disabled */
    public void disableReflectionFallback() {
        this.fallbackToReflection = false;
    }
}

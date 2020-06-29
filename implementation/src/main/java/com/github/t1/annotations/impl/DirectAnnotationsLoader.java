package com.github.t1.annotations.impl;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.AnnotationsLoader;
import com.github.t1.annotations.index.Index;

class DirectAnnotationsLoader extends AnnotationsLoader {
    private final Index index;
    private final AnnotationsLoader delegate;

    DirectAnnotationsLoader(Index index, AnnotationsLoader delegate) {
        this.index = index;
        this.delegate = delegate;
    }

    @Override public Annotations onType(Class<?> type) {
        return index.classInfo(type)
            .map(classInfo -> (Annotations) new DirectAnnotations(index, classInfo::annotations))
            .orElseGet(() -> delegate.onType(type));
    }

    @Override public Annotations onField(Class<?> type, String fieldName) {
        return index.classInfo(type)
            .map(classInfo -> classInfo.field(fieldName)
                .map(field -> (Annotations) new DirectAnnotations(index, field::annotations))
                .orElseThrow(() -> new FieldNotFoundException(fieldName, type)))
            .orElseGet(() -> delegate.onField(type, fieldName));
    }

    @Override public Annotations onMethod(Class<?> type, String methodName, Class<?>... argTypes) {
        return index.classInfo(type)
            .map(classInfo -> classInfo.findMethod(methodName, argTypes)
                .map(method -> (Annotations) new DirectAnnotations(index, method::annotations))
                .orElseThrow(() -> new MethodNotFoundException(methodName, argTypes, type)))
            .orElseGet(() -> delegate.onMethod(type, methodName, argTypes));
    }
}

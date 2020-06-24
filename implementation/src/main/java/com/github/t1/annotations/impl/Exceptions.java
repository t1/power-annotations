package com.github.t1.annotations.impl;

import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

class MethodNotFoundException extends RuntimeException {
    MethodNotFoundException(String methodName, Class<?>[] argTypes, Class<?> type) {
        this(methodName, argTypes, type, null);
    }

    MethodNotFoundException(String methodName, Class<?>[] argTypes, Class<?> type, Throwable cause) {
        super("no method " + signature(methodName, argTypes) + " in " + type, cause);
    }

    private static String signature(String methodName, Class<?>... argTypes) {
        return methodName + Stream.of(argTypes).map(Class::getSimpleName).collect(joining(", ", "(", ")"));
    }
}


class FieldNotFoundException extends RuntimeException {
    FieldNotFoundException(String fieldName, Class<?> type) {
        this(fieldName, type, null);
    }

    FieldNotFoundException(String fieldName, Class<?> type, Throwable cause) {
        super("no field '" + fieldName + "' in " + type, cause);
    }
}

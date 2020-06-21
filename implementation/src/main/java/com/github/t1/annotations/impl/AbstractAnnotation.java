package com.github.t1.annotations.impl;

public interface AbstractAnnotation {
    Object property(String name);
    String toString();
    String getTypeName();
}

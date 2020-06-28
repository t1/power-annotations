package com.github.t1.annotations.impl;

interface AbstractAnnotation {
    Object property(String name);
    @Override String toString();
    String getTypeName();
}

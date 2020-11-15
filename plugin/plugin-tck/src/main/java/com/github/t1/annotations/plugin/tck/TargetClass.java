package com.github.t1.annotations.plugin.tck;

@SomeStereotype
public class TargetClass {
    @SomeStereotype
    public void foo() {}

    @SomeStereotype
    private String bar;
}

package com.github.t1.powerannotations.demo;

import com.github.t1.annotations.MixinFor;

@MixinFor(GreetingBoundary.class)
@SomeAnnotation("mixed-in")
public class GreetingBoundaryMixin {}

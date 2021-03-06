package com.github.t1.powerannotations.demo;

import com.github.t1.annotations.Annotations;

import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@GraphQlBoundary
public class GreetingBoundary {
    private static final Logger LOG = Logger.getLogger(GreetingBoundary.class.getName());

    public AnnotationsResponse hello() {
        String mixedInValue = Annotations.on(GreetingBoundary.class)
            .get(SomeAnnotation.class)
            .map(SomeAnnotation::value)
            .orElse("nope");
        String stereotypedValue = Annotations.on(GreetingBoundary.class)
            .get(SomeOtherAnnotation.class)
            .map(SomeOtherAnnotation::value)
            .orElse("nope");
        String self = Annotations.on(GreetingBoundary.class).all()
            .map(Objects::toString)
            .collect(Collectors.joining("\n"));
        LOG.info("hello -> " + mixedInValue + "/" + stereotypedValue + "\n" + self);
        return new AnnotationsResponse()
            .setMixedInAnnotation(mixedInValue)
            .setStereotypedAnnotation(stereotypedValue);
    }
}

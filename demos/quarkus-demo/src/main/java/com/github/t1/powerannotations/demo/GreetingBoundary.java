package com.github.t1.powerannotations.demo;

import com.github.t1.annotations.Annotations;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@GraphQLApi // TODO this should be resolved from the stereotype!
@SomeAnnotation("direct")
@SomeStereotype
public class GreetingBoundary {
    private static final Logger LOG = Logger.getLogger(GreetingBoundary.class.getName());

    @Query
    public String hello() {
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
        return "mixed-in-annotation:" + mixedInValue + "\n"
            + "stereotyped-annotation:" + stereotypedValue;
    }
}

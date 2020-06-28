package com.github.t1.quarkus;

import com.github.t1.annotations.Annotations;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.logging.Logger;

@GraphQLApi
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
        LOG.info("hello -> " + mixedInValue + "/" + stereotypedValue);
        return "mixed-in-annotation:" + mixedInValue + "\n"
            + "stereotyped-annotation:" + stereotypedValue;
    }
}

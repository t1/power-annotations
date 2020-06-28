package com.github.t1.quarkus;

import com.github.t1.annotations.Annotations;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.logging.Logger;

@GraphQLApi
@SomeAnnotation("direct")
public class GreetingBoundary {
    private static final Logger LOG = Logger.getLogger(GreetingBoundary.class.getName());

    @Query
    public String hello() {
        String annotationValue = Annotations.on(GreetingBoundary.class)
            .get(SomeAnnotation.class)
            .map(SomeAnnotation::value)
            .orElse("nope");
        LOG.info("hello -> " + annotationValue);
        return "annotation-value:" + annotationValue;
    }
}

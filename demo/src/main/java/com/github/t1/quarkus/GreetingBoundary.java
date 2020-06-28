package com.github.t1.quarkus;

import com.github.t1.annotations.Annotations;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
@SomeAnnotation("direct")
public class GreetingBoundary {

    @Query
    public String hello() {
        return "ho:" + Annotations.on(GreetingBoundary.class)
            .get(SomeAnnotation.class)
            .map(SomeAnnotation::value)
            .orElse("nope");
    }
}

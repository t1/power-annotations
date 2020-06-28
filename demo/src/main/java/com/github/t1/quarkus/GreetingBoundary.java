package com.github.t1.quarkus;

import com.github.t1.annotations.Annotations;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hi")
@SomeAnnotation("direct")
public class GreetingBoundary {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "ho:" + Annotations.on(GreetingBoundary.class)
            .get(SomeAnnotation.class)
            .map(SomeAnnotation::value)
            .orElse("nope");
    }
}

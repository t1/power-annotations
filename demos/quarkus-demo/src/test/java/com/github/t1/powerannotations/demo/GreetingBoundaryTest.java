package com.github.t1.powerannotations.demo;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.BDDAssertions.then;

@QuarkusTest
class GreetingBoundaryTest {

    @GraphQlClientApi
    private interface Api {
        String hello();
    }

    private Api api;

    // @TestHTTPResource // <- this resolves to 0.0.0.0, which makes my LittleSnitch ask for permission
    URI uri = URI.create("http://localhost:8081");

    @BeforeEach void setUp() {
        api = GraphQlClientBuilder.newBuilder()
            .endpoint(uri + "/graphql")
            .build(Api.class);
    }

    @Test void testHelloEndpoint() {
        String hello = api.hello();

        then(hello).isEqualTo("" +
            "mixed-in-annotation:mixed-in\n" +
            "stereotyped-annotation:stereotyped");
    }
}

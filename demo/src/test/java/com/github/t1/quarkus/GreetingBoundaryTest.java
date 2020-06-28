package com.github.t1.quarkus;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.assertj.core.api.BDDAssertions.then;

@QuarkusTest
class GreetingBoundaryTest {

    @GraphQlClientApi
    private interface Api {
        String hello();
    }

    private Api api;

    @TestHTTPResource URL url;

    @BeforeEach void setUp() {
        api = GraphQlClientBuilder.newBuilder()
            .endpoint(url + "/graphql")
            .build(Api.class);
    }

    @Test void testHelloEndpoint() {
        String hello = api.hello();

        then(hello).isEqualTo("ho:direct");
    }
}

package com.github.t1.quarkus;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class GreetingBoundaryTest {

    @Test
    public void testHelloEndpoint() {
        when().get("/hi")

            .then()
            .statusCode(200)
            .body(is("ho:direct"));
    }
}

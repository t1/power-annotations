package test;

import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.StringReader;
import java.net.URI;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.assertj.core.api.BDDAssertions.then;

class GraphQlClientIT {
    private static final URI ENDPOINT = URI.create("http://localhost:8080/power-annotations-demo-war/graphql");
    private static final WebTarget WEB_TARGET = ClientBuilder.newClient().target(ENDPOINT);

    @Test void shouldReplyHello() {
        JsonObject result = query("{hello}");

        then(result.getJsonArray("errors")).isNull();
        then(result.getJsonObject("data").getString("hello")).isEqualTo("mixed-in-annotation:mixed-in\nstereotyped-annotation:stereotyped");
    }

    private JsonObject query(@SuppressWarnings("SameParameterValue") String query) {
        var responseString = WEB_TARGET.request(APPLICATION_JSON_TYPE)
            .post(json("{\"query\":\"" + query + "\"}"), String.class);
        return Json.createReader(new StringReader(responseString)).readObject();
    }
}

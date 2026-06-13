package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class GamesResourcesTest {

    @Test
    void testGamesEndpoint() {
        given()
                .when()
                .get("/games?name=R6&page=1&size=10")
                .then()
                .statusCode(200)
                .body(is("[{\"id\":1,\"name\":\"R6\",\"category\":\"FPS\"}]"));
    }

}
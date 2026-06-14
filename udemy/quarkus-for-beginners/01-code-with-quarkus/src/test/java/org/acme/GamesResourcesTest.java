package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.Header;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class GamesResourcesTest {

    @Test
    void testGamesEndpoint() {
        given()
            .when()
            .header(new Header("page", "1"))
            .header(new Header("size", "10"))
            .get("/games?name=battle")
            .then()
            .statusCode(200)
            .body(is("[{\"id\":2,\"name\":\"Battlefield 1\",\"category\":\"FPS\"}]"));
    }

    @Test
    void testGameEndpoint() {
        given()
            .when()
            .get("/games/1")
            .then()
            .statusCode(200)
            .body(is("{\"id\":1,\"name\":\"R6\",\"category\":\"FPS\"}"));
    }

}
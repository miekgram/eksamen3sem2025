package app.apiTest;

import app.config.ApplicationConfig;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TripResourceTest {

    private static final int PORT = 7070; // brug samme port som du bruger lokalt
    private static final String BASE_URL = "http://localhost:" + PORT + "/api";

    private Javalin app;
    private String adminToken;

    @BeforeAll
    void setUpAll() {
        // Start server med din ApplicationConfig
        app = ApplicationConfig.startServer(PORT);

        // RestAssured setup
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // Seed testdata via din Populator-route
        given()
                .when().get("/populate")
                .then().statusCode(anyOf(is(200), is(204)));

        // (Valgfrit) log ind som ADMIN, hvis du senere vil teste beskyttede endpoints
        adminToken = login("Anna", "admin123");
    }

    @AfterAll
    void tearDownAll() {
        ApplicationConfig.stopServer(app);
    }

    // ──────────────────────── TESTS ────────────────────────

    @Test
    @DisplayName("Server oppe og route overview virker")
    void serverIsUp_andRoutesVisible() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/routes")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("GET /trips/ uden filter returnerer data")
    void getAll_withoutFilter_returnsTrips() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/trips/") // trailing slash matcher get("/") i TripRoutes
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("name", hasItems(
                        "Bellevue Yoga",
                        "Cykeltur i KBH",
                        "Kanalrundfart i KBH i julebelysning"
                ));
    }

    @Test
    @DisplayName("GET /trips?category=BEACH filtrerer korrekt")
    void getAll_withCategoryFilter_returnsOnlyThatCategory() {
        given()
                .accept(ContentType.JSON)
                .queryParam("category", "BEACH") // case-insensitive i din getAll()
                .when()
                .get("/trips")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("category", everyItem(is("BEACH")));
    }

    @Test
    @DisplayName("GET /trips/1 returnerer en enkelt trip")
    void getById_existing_returnsTrip() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/trips/1")
                .then()
                .statusCode(200)
                .body("tripId", is(1))
                .body("name", not(isEmptyOrNullString()))
                .body("category", anyOf(is("BEACH"), is("CITY"), is("SEA"), is("SNOW"), is("FOREST")));
    }

    @Test
    @DisplayName("GET /trips/1/packing returnerer anbefalede items fra ekstern API")
    void packing_forTrip_returnsItems() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/trips/1/packing")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("[0].name", not(isEmptyOrNullString()));
    }

    @Test
    @DisplayName("GET /trips/1/packing/weight returnerer samlet vægt")
    void packingWeight_forTrip_returnsOk() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/trips/1/packing/weight")
                .then()
                .statusCode(200)
                // din controller returnerer en tekst—tjek at svaret indeholder 'grams'
                .body(containsString("grams"));
    }

    @Test
    @DisplayName("GET /trips/guides/totalprice returnerer en række pr. guide")
    void guidesTotalPrice_returnsRowsPerGuide() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/trips/guides/totalprice")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("name", hasItems("Adrian", "Allan"));
    }

    // ──────────────────────── HELPERS ────────────────────────

    private String login(String username, String password) {
        return given()
                .contentType(ContentType.JSON)
                .body("{\"username\":\"" + username + "\", \"password\":\"" + password + "\"}")
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract().path("token");
    }
}

package app.apiTest;

import app.config.ApplicationConfig;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CandidateResourceTest {

    private static final int PORT = 7070;
    private static final String BASE_URL = "http://localhost:" + PORT + "/api";

    private Javalin app;
    private String adminToken;

    @BeforeAll
    void setUpAll() {
        app = ApplicationConfig.startServer(PORT);

        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // seed data (roles, users, skills, candidates, links)
        given()
                .when().get("/populate")
                .then().statusCode(anyOf(is(200), is(201)));

        // login som ADMIN (fra Populator: username=Mie, password=1234)
        adminToken = login("Mie", "1234");
    }

    @AfterAll
    void tearDownAll() {
        ApplicationConfig.stopServer(app);
    }

    // ──────────────────────── TESTS ────────────────────────

    @Test
    @DisplayName("Server oppe og root endpoint svarer")
    void serverIsUp_rootOk() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/")
                .then()
                .statusCode(200)
                .body(anyOf(equalTo("Api is running"), containsString("Api is running")));
    }

    @Test
    @DisplayName("GET /candidates returnerer liste")
    void getAll_candidates_returnsList() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/candidates")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("[0].id", notNullValue())
                .body("[0].name", not(isEmptyOrNullString()));
    }

    @Test
    @DisplayName("GET /candidates?category=DB filtrerer til Maja (Populator)")
    void getAll_withCategoryFilter_DB_onlyMaja() {
        given()
                .accept(ContentType.JSON)
                .queryParam("category", "DB")
                .when()
                .get("/candidates")
                .then()
                .statusCode(200)
                .body("$", not(empty()))
                .body("size()", is(1))
                .body("[0].name", is("Maja Hansen"));
    }

    @Test
    @DisplayName("GET /candidates/1 indeholder enriched skills (US-5)")
    void getById_enrichedSkills() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/candidates/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("skills", not(empty()))
                .body("skills.slug", hasItems("java", "postgresql"))
                .body("skills.find { it.slug == 'java' }.popularityScore", notNullValue())
                .body("skills.find { it.slug == 'postgresql' }.averageSalary", notNullValue());
    }

    @Test
    @DisplayName("PUT /candidates/{cid}/skills/{sid} linker Spring Boot til Maja")
    void linkSkill_addsSpringBoot() {
        // link skillId=2 (spring-boot) til candidateId=1 (Maja)
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .put("/candidates/1/skills/2")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("skills.slug", hasItems("spring-boot")); // nu linket
    }

    @Test
    @DisplayName("POST /candidates (ADMIN) opretter kandidat")
    void create_candidate_admin() {
        int newId =
                given()
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(ContentType.JSON)
                        .body("""
                      {
                        "name": "Alice Andersen",
                        "phone": "88889999",
                        "education": "BSc Software Development"
                      }
                      """)
                        .when()
                        .post("/candidates")
                        .then()
                        .statusCode(201)
                        .body("name", is("Alice Andersen"))
                        .extract().path("id");

        // cleanup: slet igen
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/candidates/" + newId)
                .then()
                .statusCode(200)
                .body("id", is(newId));
    }

    @Test
    @DisplayName("PUT /candidates/{id} (ADMIN) opdaterer kandidat")
    void update_candidate_admin() {
        // opret først en kandidat
        int id =
                given()
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(ContentType.JSON)
                        .body("""
                      {
                        "name": "Temp User",
                        "phone": "11110000",
                        "education": "Temp Edu"
                      }
                      """)
                        .when()
                        .post("/candidates")
                        .then()
                        .statusCode(201)
                        .extract().path("id");

        // opdater
        given()
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .body("""
                  {
                    "name": "Temp User Updated",
                    "phone": "11110000",
                    "education": "Temp Edu"
                  }
                  """)
                .when()
                .put("/candidates/" + id)
                .then()
                .statusCode(200)
                .body("name", is("Temp User Updated"));
    }

    @Test
    @DisplayName("DELETE /candidates/{id} (ADMIN) sletter kandidat")
    void delete_candidate_admin() {
        // opret
        int id =
                given()
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(ContentType.JSON)
                        .body("""
                      {
                        "name": "Delete Me",
                        "phone": "99990000",
                        "education": "X"
                      }
                      """)
                        .when()
                        .post("/candidates")
                        .then()
                        .statusCode(201)
                        .extract().path("id");

        // slet
        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .delete("/candidates/" + id)
                .then()
                .statusCode(200)
                .body("id", is(id));
    }

    @Test
    @DisplayName("GET /reports/candidates/top-by-popularity (US-6)")
    void report_topByPopularity() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/reports/candidates/top-by-popularity")
                .then()
                .statusCode(200)
                .body("candidateId", is(1))
                // (93 + 86 [+ 88 hvis linket i test ovenfor]) / N
                // Da testen 'linkSkill_addsSpringBoot' kan køre i vilkårlig rækkefølge,
                // accepterer vi begge mulige averages:
                .body("averagePopularity", anyOf(is(89.5f), is(89.0f)));
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

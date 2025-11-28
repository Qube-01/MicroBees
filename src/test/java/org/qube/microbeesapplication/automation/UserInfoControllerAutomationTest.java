// package org.qube.microbeesapplication.automation;

// import io.restassured.RestAssured;
// import io.restassured.response.Response;
// import org.junit.jupiter.api.*;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.web.server.LocalServerPort;

// import static org.hamcrest.MatcherAssert.assertThat;
// import static org.hamcrest.Matchers.*;

// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT) // or RANDOM_PORT if preferred
// @TestInstance(TestInstance.Lifecycle.PER_CLASS)
// public class UserInfoControllerAutomationTest {

//     // Change port if RANDOM_PORT is used
//     @LocalServerPort
//     private int port;

//     private String baseUrl;
//     private static final String TENANT_ID = "automationTenant";
//     private String testMail;

//     @BeforeAll
//     public void setup() {
//         // Build base URL dynamically
//         baseUrl = "http://localhost:" + port + "/v1/microBees";
//         RestAssured.baseURI = baseUrl;
//         testMail = "testuser_" + System.currentTimeMillis() + "@automation.com";
//     }

//     @Test
//     public void testCreateUserInfo_Success() {
//         Response response = RestAssured
//                 .given()
//                 .queryParam("tenantId", TENANT_ID)
//                 .contentType("application/json")
//                 .body("{ \"name\": \"jane3\", \"lastName\": \"Doe\", \"mailId\": \"" + testMail + "\" }")
//                 .post("/userInfo");
//         assertThat(response.statusCode(), is(200));
//         assertThat(response.body().jsonPath().getString("mailId"), equalTo(testMail));
//     }

//     @Test
//     public void testCreateUserInfo_Duplicate() {
//         // First call to create
//         RestAssured
//                 .given()
//                 .queryParam("tenantId", TENANT_ID)
//                 .contentType("application/json")
//                 .body("{ \"name\": \"jane3\", \"lastName\": \"Doe\", \"mailId\": \"" + testMail + "\" }")
//                 .post("/userInfo");

//         // Second call to create (should throw duplicate)
//         Response dupResponse = RestAssured
//                 .given()
//                 .queryParam("tenantId", TENANT_ID)
//                 .contentType("application/json")
//                 .body("{ \"name\": \"jane3\", \"lastName\": \"Doe\", \"mailId\": \"" + testMail + "\" }")
//                 .post("/userInfo");
//         assertThat(dupResponse.statusCode(), is(400));
//     }

//     @Test
//     public void testCreateToken_Success() {
//         // Ensure user exists
//         RestAssured
//                 .given()
//                 .queryParam("tenantId", TENANT_ID)
//                 .contentType("application/json")
//                 .body("{ \"name\": \"jane3\", \"lastName\": \"Doe\", \"mailId\": \"" + testMail + "\" }")
//                 .post("/userInfo");

//         // Now get token
//         Response tokenResponse = RestAssured
//                 .given()
//                 .queryParam("tenantId", TENANT_ID)
//                 .contentType("application/json")
//                 .body("{ \"name\": \"jane3\", \"mailId\": \"" + testMail + "\" }")
//                 .post("/token");
//         assertThat(tokenResponse.statusCode(), is(200));
//         assertThat(tokenResponse.body().jsonPath().getString("access_token"), not(emptyOrNullString()));
//     }

//     @Test
//     public void testCreateToken_UserNotFound() {
//         Response response = RestAssured
//                 .given()
//                 .queryParam("tenantId", TENANT_ID)
//                 .contentType("application/json")
//                 .body("{ \"name\": \"ghost3\", \"mailId\": \"ghost@noexist.com\" }")
//                 .post("/token");
//         assertThat(response.statusCode(), is(401));
//     }

//     @Test
//     public void testDeleteUser_Success() {
//         // First, create a user
//         RestAssured
//                 .given()
//                 .queryParam("tenantId", TENANT_ID)
//                 .contentType("application/json")
//                 .body("{ \"name\": \"jane3\", \"lastName\": \"Doe\", \"mailId\": \"" + testMail + "\" }")
//                 .post("/userInfo");

//         // Delete the user
//         Response response = RestAssured
//                 .given()
//                 .queryParam("tenantId", TENANT_ID)
//                 .queryParam("email", testMail)
//                 .delete("/userInfo");
//         assertThat(response.statusCode(), is(200));
//         assertThat(response.body().asString(), containsString("User deleted successfully."));
//     }

//     @Test
//     public void testDeleteUser_NotFound() {
//         // Attempt to delete a non-existent user
//         Response response = RestAssured
//                 .given()
//                 .queryParam("tenantId", TENANT_ID)
//                 .queryParam("email", "ghost3_" + System.currentTimeMillis() + "@automation.com")
//                 .delete("/userInfo");
//         assertThat(response.statusCode(), is(404));
//         assertThat(response.body().asString(), containsString("User not found."));
//     }
// }

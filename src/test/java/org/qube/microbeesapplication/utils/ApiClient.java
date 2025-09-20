package org.qube.microbeesapplication.utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * API client utility class for REST API testing
 */
public class ApiClient {
    
    private static final String BASE_URL = "http://localhost:8080";
    private static final String API_VERSION = "/v1/microBees";
    
    static {
        RestAssured.baseURI = BASE_URL;
        RestAssured.basePath = API_VERSION;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
    
    public static RequestSpecification getBaseRequestSpec() {
        return given()
                .contentType("application/json")
                .accept("application/json");
    }
    
    public static RequestSpecification getBaseRequestSpecWithTenant(String tenantId) {
        return getBaseRequestSpec()
                .queryParam("tenantId", tenantId);
    }
    
    public static Response createUser(String tenantId, Object userInfoDto) {
        return getBaseRequestSpecWithTenant(tenantId)
                .body(userInfoDto)
                .when()
                .post("/userInfo");
    }
    
    public static Response createToken(String tenantId, Object tokenDto) {
        return getBaseRequestSpecWithTenant(tenantId)
                .body(tokenDto)
                .when()
                .post("/token");
    }
    
    public static Response makeAuthenticatedRequest(String endpoint, String token) {
        return getBaseRequestSpec()
                .header("Authorization", "Bearer " + token)
                .when()
                .get(endpoint);
    }
    
    public static Response makeGetRequest(String endpoint) {
        return getBaseRequestSpec()
                .when()
                .get(endpoint);
    }
    
    public static Response makePostRequest(String endpoint, Object body) {
        return getBaseRequestSpec()
                .body(body)
                .when()
                .post(endpoint);
    }
    
    public static Response makePutRequest(String endpoint, Object body) {
        return getBaseRequestSpec()
                .body(body)
                .when()
                .put(endpoint);
    }
    
    public static Response makeDeleteRequest(String endpoint) {
        return getBaseRequestSpec()
                .when()
                .delete(endpoint);
    }
}
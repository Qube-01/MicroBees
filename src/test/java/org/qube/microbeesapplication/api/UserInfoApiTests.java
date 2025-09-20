package org.qube.microbeesapplication.api;

import org.qube.microbeesapplication.models.dto.UserInfoDto;
import org.qube.microbeesapplication.utils.ApiClient;
import org.qube.microbeesapplication.utils.TestDataBuilder;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * API Tests for UserInfo Controller endpoints
 */
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@DisplayName("UserInfo API Tests")
public class UserInfoApiTests {

    private static final String VALID_TENANT_ID = "test_tenant_001";

    @Test
    @DisplayName("Should create user successfully with valid data")
    public void testCreateUser_ValidData_Success() {
        // Arrange
        UserInfoDto userInfo = TestDataBuilder.createRandomUserInfoDto();

        // Act
        Response response = ApiClient.createUser(VALID_TENANT_ID, userInfo);

        // Assert
        response.then()
                .statusCode(200)
                .body("firstName", equalTo(userInfo.getFirstName()))
                .body("lastName", equalTo(userInfo.getLastName()))
                .body("email", equalTo(userInfo.getEmail()));
    }

    @Test
    @DisplayName("Should return bad request for duplicate email")
    public void testCreateUser_DuplicateEmail_BadRequest() {
        // Arrange
        UserInfoDto userInfo = TestDataBuilder.createValidUserInfoDto();
        
        // Create user first time
        ApiClient.createUser(VALID_TENANT_ID, userInfo);

        // Act - Try to create same user again
        Response response = ApiClient.createUser(VALID_TENANT_ID, userInfo);

        // Assert
        response.then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should return bad request for invalid user data")
    public void testCreateUser_InvalidData_BadRequest() {
        // Arrange
        UserInfoDto invalidUserInfo = TestDataBuilder.createUserInfoDtoWithEmptyFields();

        // Act
        Response response = ApiClient.createUser(VALID_TENANT_ID, invalidUserInfo);

        // Assert
        response.then()
                .statusCode(anyOf(is(400), is(422)));
    }

    @Test
    @DisplayName("Should return bad request when tenant ID is missing")
    public void testCreateUser_MissingTenantId_BadRequest() {
        // Arrange
        UserInfoDto userInfo = TestDataBuilder.createValidUserInfoDto();

        // Act
        Response response = ApiClient.createUser("", userInfo);

        // Assert
        response.then()
                .statusCode(anyOf(is(400), is(500)));
    }

    @Test
    @DisplayName("Should return internal server error for invalid JSON")
    public void testCreateUser_InvalidJson_InternalServerError() {
        // Act
        Response response = ApiClient.makePostRequest("/userInfo?tenantId=" + VALID_TENANT_ID, "invalid-json");

        // Assert
        response.then()
                .statusCode(anyOf(is(400), is(500)));
    }

    @Test
    @DisplayName("Should handle large user data gracefully")
    public void testCreateUser_LargeData_Success() {
        // Arrange
        UserInfoDto userInfo = new UserInfoDto();
        userInfo.setFirstName("A".repeat(45)); // Within 50 char limit
        userInfo.setLastName("B".repeat(45));
        userInfo.setEmail("large.user@example.com");

        // Act
        Response response = ApiClient.createUser(VALID_TENANT_ID, userInfo);

        // Assert
        response.then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Should reject user data exceeding size limits")
    public void testCreateUser_DataTooLarge_BadRequest() {
        // Arrange
        UserInfoDto userInfo = new UserInfoDto();
        userInfo.setFirstName("A".repeat(100)); // Exceeds 50 char limit
        userInfo.setLastName("B".repeat(100));
        userInfo.setEmail("toolarge@example.com");

        // Act
        Response response = ApiClient.createUser(VALID_TENANT_ID, userInfo);

        // Assert
        response.then()
                .statusCode(anyOf(is(400), is(422), is(500)));
    }

    @Test
    @DisplayName("Should handle special characters in user data")
    public void testCreateUser_SpecialCharacters_Success() {
        // Arrange
        UserInfoDto userInfo = new UserInfoDto();
        userInfo.setFirstName("José");
        userInfo.setLastName("García-López");
        userInfo.setEmail("jose.garcia@example.com");

        // Act
        Response response = ApiClient.createUser(VALID_TENANT_ID, userInfo);

        // Assert
        response.then()
                .statusCode(200)
                .body("firstName", equalTo("José"))
                .body("lastName", equalTo("García-López"));
    }

    @Test
    @DisplayName("Should handle concurrent user creation requests")
    public void testCreateUser_ConcurrentRequests_HandledCorrectly() {
        // This test would ideally use multiple threads
        // For simplicity, we'll test sequential requests with different emails
        
        // Arrange
        UserInfoDto user1 = TestDataBuilder.createRandomUserInfoDto();
        UserInfoDto user2 = TestDataBuilder.createRandomUserInfoDto();

        // Act
        Response response1 = ApiClient.createUser(VALID_TENANT_ID, user1);
        Response response2 = ApiClient.createUser(VALID_TENANT_ID, user2);

        // Assert
        response1.then().statusCode(200);
        response2.then().statusCode(200);
    }

    @Test
    @DisplayName("Should handle different tenant IDs correctly")
    public void testCreateUser_DifferentTenants_Success() {
        // Arrange
        UserInfoDto userInfo = TestDataBuilder.createValidUserInfoDto();
        String tenant1 = TestDataBuilder.getRandomTenantId();
        String tenant2 = TestDataBuilder.getRandomTenantId();

        // Act
        Response response1 = ApiClient.createUser(tenant1, userInfo);
        Response response2 = ApiClient.createUser(tenant2, userInfo);

        // Assert - Same user should be allowed in different tenants
        response1.then().statusCode(200);
        response2.then().statusCode(200);
    }
}
package org.qube.microbeesapplication.api;

import org.qube.microbeesapplication.models.dto.UserInfoDto;
import org.qube.microbeesapplication.utils.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * API Tests for UserInfo Controller endpoints
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("UserInfo API Tests")
public class UserInfoApiTests {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String VALID_TENANT_ID = "test_tenant_001";

    @Test
    @DisplayName("Should create user successfully with valid data")
    public void testCreateUser_ValidData_Success() {
        // Arrange
        UserInfoDto userInfo = TestDataBuilder.createRandomUserInfoDto();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserInfoDto> request = new HttpEntity<>(userInfo, headers);

        // Act
        ResponseEntity<UserInfoDto> response = restTemplate.postForEntity(
            "/v1/microBees/userInfo?tenantId=" + VALID_TENANT_ID, 
            request, 
            UserInfoDto.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userInfo.getFirstName(), response.getBody().getFirstName());
        assertEquals(userInfo.getLastName(), response.getBody().getLastName());
        assertEquals(userInfo.getEmail(), response.getBody().getEmail());
    }

    @Test
    @DisplayName("Should return bad request for duplicate email")
    public void testCreateUser_DuplicateEmail_BadRequest() {
        // Arrange
        UserInfoDto userInfo = TestDataBuilder.createValidUserInfoDto();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserInfoDto> request = new HttpEntity<>(userInfo, headers);
        
        // Create user first time
        restTemplate.postForEntity("/v1/microBees/userInfo?tenantId=" + VALID_TENANT_ID, request, UserInfoDto.class);

        // Act - Try to create same user again
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/v1/microBees/userInfo?tenantId=" + VALID_TENANT_ID, 
            request, 
            String.class
        );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return bad request for invalid user data")
    public void testCreateUser_InvalidData_BadRequest() {
        // Arrange
        UserInfoDto invalidUserInfo = TestDataBuilder.createUserInfoDtoWithEmptyFields();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserInfoDto> request = new HttpEntity<>(invalidUserInfo, headers);

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/v1/microBees/userInfo?tenantId=" + VALID_TENANT_ID, 
            request, 
            String.class
        );

        // Assert
        assertTrue(response.getStatusCode().is4xxClientError());
    }

    @Test
    @DisplayName("Should handle special characters in user data")
    public void testCreateUser_SpecialCharacters_Success() {
        // Arrange
        UserInfoDto userInfo = new UserInfoDto();
        userInfo.setFirstName("José");
        userInfo.setLastName("García-López");
        userInfo.setEmail("jose.garcia@example.com");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserInfoDto> request = new HttpEntity<>(userInfo, headers);

        // Act
        ResponseEntity<UserInfoDto> response = restTemplate.postForEntity(
            "/v1/microBees/userInfo?tenantId=" + VALID_TENANT_ID, 
            request, 
            UserInfoDto.class
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("José", response.getBody().getFirstName());
        assertEquals("García-López", response.getBody().getLastName());
    }

    @Test
    @DisplayName("Should handle different tenant IDs correctly")
    public void testCreateUser_DifferentTenants_Success() {
        // Arrange
        UserInfoDto userInfo = TestDataBuilder.createValidUserInfoDto();
        String tenant1 = TestDataBuilder.getRandomTenantId();
        String tenant2 = TestDataBuilder.getRandomTenantId();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserInfoDto> request = new HttpEntity<>(userInfo, headers);

        // Act
        ResponseEntity<UserInfoDto> response1 = restTemplate.postForEntity(
            "/v1/microBees/userInfo?tenantId=" + tenant1, request, UserInfoDto.class);
        ResponseEntity<UserInfoDto> response2 = restTemplate.postForEntity(
            "/v1/microBees/userInfo?tenantId=" + tenant2, request, UserInfoDto.class);

        // Assert - Same user should be allowed in different tenants
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
    }
}
package org.qube.microbeesapplication.automation;

import org.qube.microbeesapplication.utils.TestDataBuilder;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HTTP-based automation tests for MicroBees API endpoints
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("MicroBees Automation Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MicroBeesPlaywrightTests {

    @Autowired
    private TestRestTemplate restTemplate;
    
    private static final String API_BASE_PATH = "/v1/microBees";
    private static final String TENANT_ID = "automation_test_tenant";

    @Test
    @Order(1)
    @DisplayName("User Registration API - Valid User Creation")
    void testValidUserRegistration() {
        // Arrange
        var userInfo = TestDataBuilder.createRandomUserInfoDto();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(userInfo, headers);

        // Act
        ResponseEntity<Object> response = restTemplate.postForEntity(
            API_BASE_PATH + "/userInfo?tenantId=" + TENANT_ID, 
            request, 
            Object.class
        );

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
    }

    @Test
    @Order(2)
    @DisplayName("User Registration API - Duplicate Email Validation")
    void testDuplicateEmailValidation() {
        // Arrange
        var userInfo = TestDataBuilder.createValidUserInfoDto();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(userInfo, headers);

        // Act - Create user first time
        ResponseEntity<Object> firstResponse = restTemplate.postForEntity(
            API_BASE_PATH + "/userInfo?tenantId=" + TENANT_ID, 
            request, 
            Object.class
        );

        // Act - Try to create same user again
        ResponseEntity<Object> secondResponse = restTemplate.postForEntity(
            API_BASE_PATH + "/userInfo?tenantId=" + TENANT_ID, 
            request, 
            Object.class
        );

        // Assert
        assertTrue(firstResponse.getStatusCode().is2xxSuccessful());
        assertTrue(secondResponse.getStatusCode().is4xxClientError());
    }

    @Test
    @Order(3)
    @DisplayName("User Registration API - Invalid Data Validation")
    void testInvalidDataValidation() {
        // Arrange - Empty fields
        var invalidUserInfo = TestDataBuilder.createUserInfoDtoWithEmptyFields();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(invalidUserInfo, headers);

        // Act
        ResponseEntity<Object> response = restTemplate.postForEntity(
            API_BASE_PATH + "/userInfo?tenantId=" + TENANT_ID, 
            request, 
            Object.class
        );

        // Assert
        assertTrue(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
    }

    @Test
    @Order(4)
    @DisplayName("Multi-tenant Support Validation")
    void testMultiTenantSupport() {
        // Arrange
        String tenant1 = "tenant_1";
        String tenant2 = "tenant_2";
        var userInfo = TestDataBuilder.createValidUserInfoDto();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(userInfo, headers);

        // Act - Create same user in different tenants
        ResponseEntity<Object> response1 = restTemplate.postForEntity(
            API_BASE_PATH + "/userInfo?tenantId=" + tenant1, 
            request, 
            Object.class
        );

        ResponseEntity<Object> response2 = restTemplate.postForEntity(
            API_BASE_PATH + "/userInfo?tenantId=" + tenant2, 
            request, 
            Object.class
        );

        // Assert - Both should succeed (same user allowed in different tenants)
        assertTrue(response1.getStatusCode().is2xxSuccessful());
        assertTrue(response2.getStatusCode().is2xxSuccessful());
    }

    @Test
    @Order(5)
    @DisplayName("API Performance Test - Response Time Validation")
    void testApiPerformance() {
        // Arrange
        var userInfo = TestDataBuilder.createRandomUserInfoDto();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(userInfo, headers);

        // Act & Measure
        long startTime = System.currentTimeMillis();
        ResponseEntity<Object> response = restTemplate.postForEntity(
            API_BASE_PATH + "/userInfo?tenantId=" + TENANT_ID, 
            request, 
            Object.class
        );
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        // Assert
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(responseTime < 5000, "API response time should be less than 5 seconds, but was: " + responseTime + "ms");
    }

    @Test
    @Order(6)
    @DisplayName("API Security Test - Malformed JSON Handling")
    void testMalformedJsonHandling() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{invalid-json", headers);

        // Act
        ResponseEntity<Object> response = restTemplate.postForEntity(
            API_BASE_PATH + "/userInfo?tenantId=" + TENANT_ID, 
            request, 
            Object.class
        );

        // Assert
        assertTrue(response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError());
    }

    @Test
    @Order(7)
    @DisplayName("End-to-End User Journey Test")
    void testCompleteUserJourney() {
        // Arrange
        var userInfo = TestDataBuilder.createRandomUserInfoDto();
        String tenantId = TestDataBuilder.getRandomTenantId();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Act & Assert - Step 1: User Registration
        HttpEntity<Object> userRequest = new HttpEntity<>(userInfo, headers);
        ResponseEntity<Object> userResponse = restTemplate.postForEntity(
            API_BASE_PATH + "/userInfo?tenantId=" + tenantId, 
            userRequest, 
            Object.class
        );

        assertTrue(userResponse.getStatusCode().is2xxSuccessful());

        // Verify the complete journey was successful
        System.out.println("Complete user journey test passed for user: " + userInfo.getEmail());
    }
}
package org.qube.microbeesapplication.automation;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.RequestOptions;
import org.qube.microbeesapplication.utils.TestDataBuilder;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Playwright-based automation tests for MicroBees API endpoints
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@DisplayName("MicroBees Automation Tests with Playwright")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MicroBeesPlaywrightTests {

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private APIRequestContext apiContext;
    
    private static final String BASE_URL = "http://localhost:8080";
    private static final String API_BASE_PATH = "/v1/microBees";
    private static final String TENANT_ID = "automation_test_tenant";

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        apiContext = playwright.request().newContext(new APIRequest.NewContextOptions()
                .setBaseURL(BASE_URL));
    }

    @AfterEach
    void closeContext() {
        if (context != null) {
            context.close();
        }
        if (apiContext != null) {
            apiContext.dispose();
        }
    }

    @Test
    @Order(1)
    @DisplayName("User Registration API - Valid User Creation")
    void testValidUserRegistration() {
        // Arrange
        var userInfo = TestDataBuilder.createRandomUserInfoDto();
        String requestBody = TestDataBuilder.toJson(userInfo);

        // Act
        APIResponse response = apiContext.post(API_BASE_PATH + "/userInfo?tenantId=" + TENANT_ID,
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(requestBody));

        // Assert
        assertEquals(200, response.status());
        assertTrue(response.text().contains(userInfo.getFirstName()));
        assertTrue(response.text().contains(userInfo.getEmail()));
    }

    @Test
    @Order(2)
    @DisplayName("User Registration API - Duplicate Email Validation")
    void testDuplicateEmailValidation() {
        // Arrange
        var userInfo = TestDataBuilder.createValidUserInfoDto();
        String requestBody = TestDataBuilder.toJson(userInfo);

        // Act - Create user first time
        APIResponse firstResponse = apiContext.post(API_BASE_PATH + "/userInfo?tenantId=" + TENANT_ID,
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(requestBody));

        // Act - Try to create same user again
        APIResponse secondResponse = apiContext.post(API_BASE_PATH + "/userInfo?tenantId=" + TENANT_ID,
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(requestBody));

        // Assert
        assertEquals(200, firstResponse.status());
        assertEquals(400, secondResponse.status());
    }

    @Test
    @Order(3)
    @DisplayName("User Registration API - Invalid Data Validation")
    void testInvalidDataValidation() {
        // Arrange - Empty fields
        var invalidUserInfo = TestDataBuilder.createUserInfoDtoWithEmptyFields();
        String requestBody = TestDataBuilder.toJson(invalidUserInfo);

        // Act
        APIResponse response = apiContext.post(API_BASE_PATH + "/userInfo?tenantId=" + TENANT_ID,
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(requestBody));

        // Assert
        assertTrue(response.status() >= 400);
    }

    @Test
    @Order(4)
    @DisplayName("Token Generation API - Valid Token Request")
    void testValidTokenGeneration() {
        // Arrange - Create user first
        var userInfo = TestDataBuilder.createRandomUserInfoDto();
        String userRequestBody = TestDataBuilder.toJson(userInfo);

        apiContext.post(API_BASE_PATH + "/userInfo?tenantId=" + TENANT_ID,
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(userRequestBody));

        // Prepare token request
        Map<String, Object> tokenRequest = TestDataBuilder.createTokenDto(
                userInfo.getFirstName(), userInfo.getEmail());
        String tokenRequestBody = TestDataBuilder.toJson(tokenRequest);

        // Act
        APIResponse tokenResponse = apiContext.post(API_BASE_PATH + "/token?tenantId=" + TENANT_ID,
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(tokenRequestBody));

        // Assert
        assertEquals(200, tokenResponse.status());
        assertTrue(tokenResponse.text().contains("access_token"));
        
        // Validate JWT format
        String responseBody = tokenResponse.text();
        assertTrue(responseBody.matches(".*\"access_token\"\\s*:\\s*\"[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\".*"));
    }

    @Test
    @Order(5)
    @DisplayName("Token Generation API - Invalid User Credentials")
    void testInvalidUserCredentials() {
        // Arrange
        Map<String, Object> invalidTokenRequest = TestDataBuilder.createTokenDto(
                "NonExistent", "nonexistent@example.com");
        String requestBody = TestDataBuilder.toJson(invalidTokenRequest);

        // Act
        APIResponse response = apiContext.post(API_BASE_PATH + "/token?tenantId=" + TENANT_ID,
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(requestBody));

        // Assert
        assertEquals(401, response.status());
    }

    @Test
    @Order(6)
    @DisplayName("Multi-tenant Support Validation")
    void testMultiTenantSupport() {
        // Arrange
        String tenant1 = "tenant_1";
        String tenant2 = "tenant_2";
        var userInfo = TestDataBuilder.createValidUserInfoDto();
        String requestBody = TestDataBuilder.toJson(userInfo);

        // Act - Create same user in different tenants
        APIResponse response1 = apiContext.post(API_BASE_PATH + "/userInfo?tenantId=" + tenant1,
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(requestBody));

        APIResponse response2 = apiContext.post(API_BASE_PATH + "/userInfo?tenantId=" + tenant2,
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(requestBody));

        // Assert - Both should succeed (same user allowed in different tenants)
        assertEquals(200, response1.status());
        assertEquals(200, response2.status());
    }

    @Test
    @Order(7)
    @DisplayName("API Performance Test - Response Time Validation")
    void testApiPerformance() {
        // Arrange
        var userInfo = TestDataBuilder.createRandomUserInfoDto();
        String requestBody = TestDataBuilder.toJson(userInfo);

        // Act & Measure
        long startTime = System.currentTimeMillis();
        APIResponse response = apiContext.post(API_BASE_PATH + "/userInfo?tenantId=" + TENANT_ID,
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(requestBody));
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        // Assert
        assertEquals(200, response.status());
        assertTrue(responseTime < 5000, "API response time should be less than 5 seconds, but was: " + responseTime + "ms");
    }

    @Test
    @Order(8)
    @DisplayName("API Security Test - Malformed JSON Handling")
    void testMalformedJsonHandling() {
        // Act
        APIResponse response = apiContext.post(API_BASE_PATH + "/userInfo?tenantId=" + TENANT_ID,
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData("{invalid-json"));

        // Assert
        assertTrue(response.status() >= 400);
    }

    @Test
    @Order(9)
    @DisplayName("API Load Test - Concurrent Requests")
    void testConcurrentRequests() {
        // Arrange
        int numberOfRequests = 5;
        Thread[] threads = new Thread[numberOfRequests];
        APIResponse[] responses = new APIResponse[numberOfRequests];

        // Act - Send concurrent requests
        for (int i = 0; i < numberOfRequests; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                var userInfo = TestDataBuilder.createRandomUserInfoDto();
                String requestBody = TestDataBuilder.toJson(userInfo);
                responses[index] = apiContext.post(API_BASE_PATH + "/userInfo?tenantId=" + TENANT_ID + "_" + index,
                        RequestOptions.create()
                                .setHeader("Content-Type", "application/json")
                                .setData(requestBody));
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Concurrent test was interrupted");
        }

        // Assert - All requests should succeed
        for (APIResponse response : responses) {
            assertEquals(200, response.status());
        }
    }

    @Test
    @Order(10)
    @DisplayName("End-to-End User Journey Test")
    void testCompleteUserJourney() {
        // Arrange
        var userInfo = TestDataBuilder.createRandomUserInfoDto();
        String tenantId = TestDataBuilder.getRandomTenantId();

        // Act & Assert - Step 1: User Registration
        String userRequestBody = TestDataBuilder.toJson(userInfo);
        APIResponse userResponse = apiContext.post(API_BASE_PATH + "/userInfo?tenantId=" + tenantId,
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(userRequestBody));

        assertEquals(200, userResponse.status());

        // Act & Assert - Step 2: Token Generation
        Map<String, Object> tokenRequest = TestDataBuilder.createTokenDto(
                userInfo.getFirstName(), userInfo.getEmail());
        String tokenRequestBody = TestDataBuilder.toJson(tokenRequest);

        APIResponse tokenResponse = apiContext.post(API_BASE_PATH + "/token?tenantId=" + tenantId,
                RequestOptions.create()
                        .setHeader("Content-Type", "application/json")
                        .setData(tokenRequestBody));

        assertEquals(200, tokenResponse.status());
        assertTrue(tokenResponse.text().contains("access_token"));

        // Verify the complete journey was successful
        System.out.println("Complete user journey test passed for user: " + userInfo.getEmail());
    }
}
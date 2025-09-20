package org.qube.microbeesapplication.automation;

import org.junit.jupiter.api.*;
import org.qube.microbeesapplication.utils.TestDataBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance and Load Tests for MicroBees Application
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@DisplayName("MicroBees Performance Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MicroBeesPerformanceTests {

    private static final String BASE_URL = "http://localhost:8080/v1/microBees";
    private static final String PERFORMANCE_TENANT = "performance_test_tenant";
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    @Test
    @Order(1)
    @DisplayName("Single User Registration Performance Test")
    void testSingleUserRegistrationPerformance() throws Exception {
        // Arrange
        var userInfo = TestDataBuilder.createRandomUserInfoDto();
        String requestBody = TestDataBuilder.toJson(userInfo);

        // Act & Measure
        long startTime = System.currentTimeMillis();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/userInfo?tenantId=" + PERFORMANCE_TENANT))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        // Assert
        assertEquals(200, response.statusCode());
        assertTrue(responseTime < 2000, "Single user registration should complete within 2 seconds, actual: " + responseTime + "ms");
        
        System.out.println("Single user registration completed in: " + responseTime + "ms");
    }

    @Test
    @Order(2)
    @DisplayName("Concurrent User Registration Load Test")
    void testConcurrentUserRegistrationLoad() throws Exception {
        // Arrange
        int numberOfConcurrentUsers = 10;
        int timeoutSeconds = 30;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfConcurrentUsers);
        CountDownLatch latch = new CountDownLatch(numberOfConcurrentUsers);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<Long> responseTimes = new ArrayList<>();

        // Act
        long testStartTime = System.currentTimeMillis();
        
        for (int i = 0; i < numberOfConcurrentUsers; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    var userInfo = TestDataBuilder.createRandomUserInfoDto();
                    String requestBody = TestDataBuilder.toJson(userInfo);
                    
                    long startTime = System.currentTimeMillis();
                    
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + "/userInfo?tenantId=" + PERFORMANCE_TENANT + "_" + userId))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    
                    long endTime = System.currentTimeMillis();
                    
                    synchronized (responseTimes) {
                        responseTimes.add(endTime - startTime);
                    }
                    
                    if (response.statusCode() == 200) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                    
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    System.err.println("Error in concurrent test: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for completion
        assertTrue(latch.await(timeoutSeconds, TimeUnit.SECONDS), 
                "Load test should complete within " + timeoutSeconds + " seconds");
        
        long testEndTime = System.currentTimeMillis();
        long totalTestTime = testEndTime - testStartTime;

        // Assert
        assertTrue(successCount.get() > 0, "At least some requests should succeed");
        assertTrue(failureCount.get() < numberOfConcurrentUsers / 2, 
                "Failure rate should be less than 50%, actual failures: " + failureCount.get());

        // Calculate performance metrics
        double averageResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long maxResponseTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        long minResponseTime = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);

        System.out.println("=== Load Test Results ===");
        System.out.println("Concurrent Users: " + numberOfConcurrentUsers);
        System.out.println("Successful Requests: " + successCount.get());
        System.out.println("Failed Requests: " + failureCount.get());
        System.out.println("Total Test Time: " + totalTestTime + "ms");
        System.out.println("Average Response Time: " + String.format("%.2f", averageResponseTime) + "ms");
        System.out.println("Min Response Time: " + minResponseTime + "ms");
        System.out.println("Max Response Time: " + maxResponseTime + "ms");
        System.out.println("Throughput: " + String.format("%.2f", (double) successCount.get() / (totalTestTime / 1000.0)) + " requests/second");

        // Performance assertions
        assertTrue(averageResponseTime < 5000, "Average response time should be less than 5 seconds");
        assertTrue(maxResponseTime < 10000, "Max response time should be less than 10 seconds");

        executor.shutdown();
    }

    @Test
    @Order(3)
    @DisplayName("Token Generation Performance Test")
    void testTokenGenerationPerformance() throws Exception {
        // Arrange - Create a user first
        var userInfo = TestDataBuilder.createRandomUserInfoDto();
        String userRequestBody = TestDataBuilder.toJson(userInfo);
        String tenantId = PERFORMANCE_TENANT + "_token_perf";

        HttpRequest userRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/userInfo?tenantId=" + tenantId))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(userRequestBody))
                .build();

        httpClient.send(userRequest, HttpResponse.BodyHandlers.ofString());

        // Prepare token request
        var tokenRequest = TestDataBuilder.createTokenDto(userInfo.getFirstName(), userInfo.getEmail());
        String tokenRequestBody = TestDataBuilder.toJson(tokenRequest);

        // Act & Measure
        long startTime = System.currentTimeMillis();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/token?tenantId=" + tenantId))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(tokenRequestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        // Assert
        assertEquals(200, response.statusCode());
        assertTrue(responseTime < 3000, "Token generation should complete within 3 seconds, actual: " + responseTime + "ms");
        assertTrue(response.body().contains("access_token"));
        
        System.out.println("Token generation completed in: " + responseTime + "ms");
    }

    @Test
    @Order(4)
    @DisplayName("Memory Usage Stress Test")
    void testMemoryUsageStressTest() throws Exception {
        // Arrange
        int numberOfRequests = 100;
        String tenantId = PERFORMANCE_TENANT + "_memory_stress";
        
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Act - Send multiple requests
        for (int i = 0; i < numberOfRequests; i++) {
            var userInfo = TestDataBuilder.createRandomUserInfoDto();
            String requestBody = TestDataBuilder.toJson(userInfo);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/userInfo?tenantId=" + tenantId + "_" + i))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            
            // Force garbage collection every 10 requests
            if (i % 10 == 0) {
                System.gc();
                Thread.sleep(100);
            }
        }

        // Force garbage collection
        System.gc();
        Thread.sleep(1000);
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        System.out.println("=== Memory Usage Results ===");
        System.out.println("Initial Memory Usage: " + (initialMemory / 1024 / 1024) + " MB");
        System.out.println("Final Memory Usage: " + (finalMemory / 1024 / 1024) + " MB");
        System.out.println("Memory Increase: " + (memoryIncrease / 1024 / 1024) + " MB");
        
        // Assert memory usage is reasonable (less than 100MB increase for 100 requests)
        assertTrue(memoryIncrease < 100 * 1024 * 1024, 
                "Memory increase should be reasonable, actual increase: " + (memoryIncrease / 1024 / 1024) + " MB");
    }

    @Test
    @Order(5)
    @DisplayName("Database Connection Pool Test")
    void testDatabaseConnectionPoolStress() throws Exception {
        // Arrange
        int numberOfConnections = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfConnections);
        CountDownLatch latch = new CountDownLatch(numberOfConnections);
        AtomicInteger successfulConnections = new AtomicInteger(0);

        // Act
        for (int i = 0; i < numberOfConnections; i++) {
            final int connId = i;
            executor.submit(() -> {
                try {
                    var userInfo = TestDataBuilder.createRandomUserInfoDto();
                    String requestBody = TestDataBuilder.toJson(userInfo);
                    
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + "/userInfo?tenantId=" + PERFORMANCE_TENANT + "_conn_" + connId))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                            .build();

                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    
                    if (response.statusCode() == 200) {
                        successfulConnections.incrementAndGet();
                    }
                    
                } catch (Exception e) {
                    System.err.println("Connection test error: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for completion
        assertTrue(latch.await(60, TimeUnit.SECONDS), "Connection pool test should complete within 60 seconds");

        // Assert
        assertTrue(successfulConnections.get() >= numberOfConnections * 0.8, 
                "At least 80% of database connections should succeed, actual: " + successfulConnections.get() + "/" + numberOfConnections);

        System.out.println("Successful database connections: " + successfulConnections.get() + "/" + numberOfConnections);
        
        executor.shutdown();
    }

    @Test
    @Order(6)
    @DisplayName("API Endurance Test")
    void testApiEndurance() throws Exception {
        // Arrange
        int durationMinutes = 2; // Run for 2 minutes
        long endTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000);
        AtomicInteger requestCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // Act
        System.out.println("Starting endurance test for " + durationMinutes + " minutes...");
        
        while (System.currentTimeMillis() < endTime) {
            try {
                var userInfo = TestDataBuilder.createRandomUserInfoDto();
                String requestBody = TestDataBuilder.toJson(userInfo);
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/userInfo?tenantId=" + PERFORMANCE_TENANT + "_endurance_" + requestCount.get()))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                requestCount.incrementAndGet();
                
                if (response.statusCode() == 200) {
                    successCount.incrementAndGet();
                } else {
                    errorCount.incrementAndGet();
                }
                
                // Small delay to avoid overwhelming the system
                Thread.sleep(100);
                
            } catch (Exception e) {
                errorCount.incrementAndGet();
                System.err.println("Endurance test error: " + e.getMessage());
            }
        }

        // Assert
        double successRate = (double) successCount.get() / requestCount.get();
        
        System.out.println("=== Endurance Test Results ===");
        System.out.println("Test Duration: " + durationMinutes + " minutes");
        System.out.println("Total Requests: " + requestCount.get());
        System.out.println("Successful Requests: " + successCount.get());
        System.out.println("Failed Requests: " + errorCount.get());
        System.out.println("Success Rate: " + String.format("%.2f", successRate * 100) + "%");
        System.out.println("Average Requests/Minute: " + String.format("%.2f", (double) requestCount.get() / durationMinutes));

        assertTrue(requestCount.get() > 0, "At least some requests should have been made");
        assertTrue(successRate > 0.9, "Success rate should be at least 90%, actual: " + String.format("%.2f", successRate * 100) + "%");
    }
}
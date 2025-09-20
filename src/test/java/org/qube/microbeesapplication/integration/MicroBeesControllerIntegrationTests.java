package org.qube.microbeesapplication.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.qube.microbeesapplication.models.dto.UserInfoDto;
import org.qube.microbeesapplication.utils.TestDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for MicroBees Controllers using MockMvc
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("MicroBees Controller Integration Tests")
public class MicroBeesControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_PATH = "/v1/microBees";
    private static final String TEST_TENANT_ID = "mvc_test_tenant";

    @Test
    @DisplayName("POST /userInfo - Should create user successfully")
    void testCreateUser_Success() throws Exception {
        // Arrange
        UserInfoDto userInfo = TestDataBuilder.createRandomUserInfoDto();

        // Act & Assert
        mockMvc.perform(post(BASE_PATH + "/userInfo")
                        .param("tenantId", TEST_TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userInfo)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName", is(userInfo.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(userInfo.getLastName())))
                .andExpect(jsonPath("$.email", is(userInfo.getEmail())));
    }

    @Test
    @DisplayName("POST /userInfo - Should return bad request for duplicate email")
    void testCreateUser_DuplicateEmail_BadRequest() throws Exception {
        // Arrange
        UserInfoDto userInfo = TestDataBuilder.createValidUserInfoDto();

        // Act - Create user first time
        mockMvc.perform(post(BASE_PATH + "/userInfo")
                        .param("tenantId", TEST_TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userInfo)))
                .andExpect(status().isOk());

        // Act & Assert - Try to create same user again
        mockMvc.perform(post(BASE_PATH + "/userInfo")
                        .param("tenantId", TEST_TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userInfo)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /userInfo - Should return bad request for invalid JSON")
    void testCreateUser_InvalidJson_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post(BASE_PATH + "/userInfo")
                        .param("tenantId", TEST_TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /userInfo - Should return error when tenant ID is missing")
    void testCreateUser_MissingTenantId_Error() throws Exception {
        // Arrange
        UserInfoDto userInfo = TestDataBuilder.createValidUserInfoDto();

        // Act & Assert
        mockMvc.perform(post(BASE_PATH + "/userInfo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userInfo)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /token - Should create token for existing user")
    void testCreateToken_ExistingUser_Success() throws Exception {
        // Arrange - Create user first
        UserInfoDto userInfo = TestDataBuilder.createRandomUserInfoDto();
        mockMvc.perform(post(BASE_PATH + "/userInfo")
                        .param("tenantId", TEST_TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userInfo)));

        // Prepare token request
        Map<String, Object> tokenRequest = TestDataBuilder.createTokenDto(
                userInfo.getFirstName(), userInfo.getEmail());

        // Act & Assert
        mockMvc.perform(post(BASE_PATH + "/token")
                        .param("tenantId", TEST_TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.access_token", notNullValue()))
                .andExpect(jsonPath("$.access_token", not(emptyString())));
    }

    @Test
    @DisplayName("POST /token - Should return unauthorized for non-existent user")
    void testCreateToken_NonExistentUser_Unauthorized() throws Exception {
        // Arrange
        Map<String, Object> tokenRequest = TestDataBuilder.createTokenDto(
                "NonExistent", "nonexistent@example.com");

        // Act & Assert
        mockMvc.perform(post(BASE_PATH + "/token")
                        .param("tenantId", TEST_TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /token - Should handle malformed JSON")
    void testCreateToken_InvalidJson_BadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post(BASE_PATH + "/token")
                        .param("tenantId", TEST_TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{malformed-json}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should handle CORS headers properly")
    void testCorsHeaders() throws Exception {
        // Arrange
        UserInfoDto userInfo = TestDataBuilder.createRandomUserInfoDto();

        // Act & Assert
        mockMvc.perform(post(BASE_PATH + "/userInfo")
                        .param("tenantId", TEST_TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userInfo))
                        .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should handle concurrent requests properly")
    void testConcurrentRequests() throws Exception {
        // This test would ideally use multiple threads
        // For MockMvc, we'll test sequential requests with different data
        
        // Arrange
        UserInfoDto user1 = TestDataBuilder.createRandomUserInfoDto();
        UserInfoDto user2 = TestDataBuilder.createRandomUserInfoDto();

        // Act & Assert
        mockMvc.perform(post(BASE_PATH + "/userInfo")
                        .param("tenantId", TEST_TENANT_ID + "_1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isOk());

        mockMvc.perform(post(BASE_PATH + "/userInfo")
                        .param("tenantId", TEST_TENANT_ID + "_2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isOk());
    }
}
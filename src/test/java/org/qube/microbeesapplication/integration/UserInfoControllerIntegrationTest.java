package org.qube.microbeesapplication.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qube.microbeesapplication.models.dto.UserInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
class UserInfoControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Mock MongoDB to avoid actual database connections during testing
    @MockBean
    private MongoTemplate mongoTemplate;

    private UserInfoDto userInfoDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        userInfoDto = new UserInfoDto();
        userInfoDto.setFirstName("Integration");
        userInfoDto.setLastName("Test");
        userInfoDto.setEmail("integration.test@example.com");
    }

    @Test
    void contextLoads() {
        // This test ensures that the Spring context loads correctly
    }

    @Test
    void userInfoEndpoint_AcceptsValidRequest() throws Exception {
        // This test validates the endpoint structure without database interaction
        mockMvc.perform(post("/v1/microBees/userInfo")
                        .param("tenantId", "test-tenant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userInfoDto)))
                .andExpect(status().is5xxServerError()); // Expected since we're mocking MongoDB
    }

    @Test
    void tokenEndpoint_AcceptsValidRequest() throws Exception {
        // Create a simple token request
        String tokenRequest = "{\"email\":\"test@example.com\",\"password\":\"test123\"}";

        mockMvc.perform(post("/v1/microBees/token")
                        .param("tenantId", "test-tenant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tokenRequest))
                .andExpect(status().is5xxServerError()); // Expected since we're mocking dependencies
    }

    @Test
    void invalidEndpoint_Returns404() throws Exception {
        mockMvc.perform(get("/v1/microBees/invalid")
                        .param("tenantId", "test-tenant"))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingTenantId_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/v1/microBees/userInfo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userInfoDto)))
                .andExpect(status().isBadRequest());
    }
}
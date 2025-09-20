package org.qube.microbeesapplication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qube.microbeesapplication.models.dto.TokenDto;
import org.qube.microbeesapplication.models.dto.UserInfoDto;
import org.qube.microbeesapplication.service.TokenService;
import org.qube.microbeesapplication.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserInfoController.class)
class UserInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private UserInfoService userInfoService;

    @MockBean
    private TokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserInfoDto userInfoDto;
    private TokenDto tokenDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        userInfoDto = new UserInfoDto();
        userInfoDto.setFirstName("John");
        userInfoDto.setLastName("Doe");
        userInfoDto.setEmail("john.doe@example.com");

        tokenDto = new TokenDto();
        tokenDto.setEmail("john.doe@example.com");
    }

    @Test
    void createUser_Success() throws Exception {
        // Arrange
        when(userInfoService.newUserLogin(any(UserInfoDto.class), anyString()))
                .thenReturn(userInfoDto);

        // Act & Assert
        mockMvc.perform(post("/v1/microBees/userInfo")
                        .param("tenantId", "tenant1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userInfoDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(userInfoService, times(1)).newUserLogin(any(UserInfoDto.class), eq("tenant1"));
    }

    @Test
    void createUser_DuplicateKeyException() throws Exception {
        // Arrange
        when(userInfoService.newUserLogin(any(UserInfoDto.class), anyString()))
                .thenThrow(new DuplicateKeyException("User with this email already exists"));

        // Act & Assert
        mockMvc.perform(post("/v1/microBees/userInfo")
                        .param("tenantId", "tenant1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userInfoDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User with this email already exists"));

        verify(userInfoService, times(1)).newUserLogin(any(UserInfoDto.class), eq("tenant1"));
    }

    @Test
    void createUser_GeneralException() throws Exception {
        // Arrange
        when(userInfoService.newUserLogin(any(UserInfoDto.class), anyString()))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(post("/v1/microBees/userInfo")
                        .param("tenantId", "tenant1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userInfoDto)))
                .andExpect(status().isInternalServerError());

        verify(userInfoService, times(1)).newUserLogin(any(UserInfoDto.class), eq("tenant1"));
    }

    @Test
    void createToken_Success() throws Exception {
        // Arrange
        String expectedToken = "jwt.token.here";
        when(tokenService.getToken(anyString(), any(TokenDto.class)))
                .thenReturn(expectedToken);

        // Act & Assert
        mockMvc.perform(post("/v1/microBees/token")
                        .param("tenantId", "tenant1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value(expectedToken));

        verify(tokenService, times(1)).getToken(eq("tenant1"), any(TokenDto.class));
    }

    @Test
    void createToken_SecurityException() throws Exception {
        // Arrange
        when(tokenService.getToken(anyString(), any(TokenDto.class)))
                .thenThrow(new SecurityException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/v1/microBees/token")
                        .param("tenantId", "tenant1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenDto)))
                .andExpect(status().isUnauthorized());

        verify(tokenService, times(1)).getToken(eq("tenant1"), any(TokenDto.class));
    }

    @Test
    void createToken_GeneralException() throws Exception {
        // Arrange
        when(tokenService.getToken(anyString(), any(TokenDto.class)))
                .thenThrow(new RuntimeException("Token generation failed"));

        // Act & Assert
        mockMvc.perform(post("/v1/microBees/token")
                        .param("tenantId", "tenant1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenDto)))
                .andExpect(status().isInternalServerError());

        verify(tokenService, times(1)).getToken(eq("tenant1"), any(TokenDto.class));
    }

    @Test
    void createUser_MissingTenantId() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/v1/microBees/userInfo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userInfoDto)))
                .andExpect(status().isBadRequest());

        verify(userInfoService, never()).newUserLogin(any(UserInfoDto.class), anyString());
    }

    @Test
    void createUser_InvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/v1/microBees/userInfo")
                        .param("tenantId", "tenant1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest());

        verify(userInfoService, never()).newUserLogin(any(UserInfoDto.class), anyString());
    }
}
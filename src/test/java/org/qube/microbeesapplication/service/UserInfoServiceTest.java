package org.qube.microbeesapplication.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.qube.microbeesapplication.config.MultiTenantMongoTemplate;
import org.qube.microbeesapplication.models.dto.UserInfoDto;
import org.qube.microbeesapplication.models.jpa.UserInfoJpa;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserInfoServiceTest {

    @Mock
    private MultiTenantMongoTemplate mongoTemplate;

    @Mock
    private MongoTemplate mockMongoTemplate;

    private UserInfoService userInfoService;

    private UserInfoDto userInfoDto;
    private UserInfoJpa userInfoJpa;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create service with mocked dependencies
        userInfoService = new UserInfoService(mongoTemplate);
        
        userInfoDto = new UserInfoDto();
        userInfoDto.setFirstName("John");
        userInfoDto.setLastName("Doe");
        userInfoDto.setEmail("john.doe@example.com");

        userInfoJpa = new UserInfoJpa();
        userInfoJpa.setId("123");
        userInfoJpa.setFirstName("John");
        userInfoJpa.setLastName("Doe");
        userInfoJpa.setEmail("john.doe@example.com");
    }

    @Test
    void newUserLogin_Success() throws Exception {
        // Arrange
        String tenantId = "tenant1";
        when(mongoTemplate.getMongoTemplate(tenantId)).thenReturn(mockMongoTemplate);
        when(mockMongoTemplate.save(any(UserInfoJpa.class))).thenReturn(userInfoJpa);

        // Act
        UserInfoDto result = userInfoService.newUserLogin(userInfoDto, tenantId);

        // Assert
        assertNotNull(result);
        assertEquals(userInfoDto.getFirstName(), result.getFirstName());
        assertEquals(userInfoDto.getLastName(), result.getLastName());
        assertEquals(userInfoDto.getEmail(), result.getEmail());

        verify(mongoTemplate, times(1)).getMongoTemplate(tenantId);
        verify(mockMongoTemplate, times(1)).save(any(UserInfoJpa.class));
    }

    @Test
    void newUserLogin_DuplicateKeyException() {
        // Arrange
        String tenantId = "tenant1";
        when(mongoTemplate.getMongoTemplate(tenantId)).thenReturn(mockMongoTemplate);
        when(mockMongoTemplate.save(any(UserInfoJpa.class))).thenThrow(new DuplicateKeyException("Duplicate key"));

        // Act & Assert
        DuplicateKeyException exception = assertThrows(DuplicateKeyException.class, () -> {
            userInfoService.newUserLogin(userInfoDto, tenantId);
        });

        assertEquals("User with this email already exists", exception.getMessage());
        verify(mongoTemplate, times(1)).getMongoTemplate(tenantId);
        verify(mockMongoTemplate, times(1)).save(any(UserInfoJpa.class));
    }

    @Test
    void newUserLogin_GeneralException() {
        // Arrange
        String tenantId = "tenant1";
        when(mongoTemplate.getMongoTemplate(tenantId)).thenReturn(mockMongoTemplate);
        when(mockMongoTemplate.save(any(UserInfoJpa.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            userInfoService.newUserLogin(userInfoDto, tenantId);
        });

        assertEquals("Couldn't save user info", exception.getMessage());
        verify(mongoTemplate, times(1)).getMongoTemplate(tenantId);
        verify(mockMongoTemplate, times(1)).save(any(UserInfoJpa.class));
    }

    @Test
    void newUserLogin_NullTenantId() {
        // Arrange
        String tenantId = null;

        // Act & Assert
        assertThrows(Exception.class, () -> {
            userInfoService.newUserLogin(userInfoDto, tenantId);
        });
    }

    @Test
    void newUserLogin_EmptyTenantId() {
        // Arrange
        String tenantId = "";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            userInfoService.newUserLogin(userInfoDto, tenantId);
        });
    }

    @Test
    void newUserLogin_NullUserInfoDto() {
        // Arrange
        String tenantId = "tenant1";
        UserInfoDto nullDto = null;

        // Act & Assert
        assertThrows(Exception.class, () -> {
            userInfoService.newUserLogin(nullDto, tenantId);
        });
    }
}
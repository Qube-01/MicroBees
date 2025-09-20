package org.qube.microbeesapplication.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.qube.microbeesapplication.models.dto.UserInfoDto;
import org.qube.microbeesapplication.service.UserInfoService;
import org.qube.microbeesapplication.utils.TestDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserInfo Service
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("UserInfo Service Integration Tests")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserInfoServiceIntegrationTests {

    @Autowired
    private UserInfoService userInfoService;

    private static final String TEST_TENANT_ID = "integration_test_tenant";

    @Test
    @DisplayName("Should save user successfully with valid data")
    void testSaveUser_ValidData_Success() throws Exception {
        // Arrange
        UserInfoDto userInfo = TestDataBuilder.createRandomUserInfoDto();

        // Act
        UserInfoDto savedUser = userInfoService.newUserLogin(userInfo, TEST_TENANT_ID);

        // Assert
        assertNotNull(savedUser);
        assertEquals(userInfo.getFirstName(), savedUser.getFirstName());
        assertEquals(userInfo.getLastName(), savedUser.getLastName());
        assertEquals(userInfo.getEmail(), savedUser.getEmail());
    }

    @Test
    @DisplayName("Should throw DuplicateKeyException for duplicate email")
    void testSaveUser_DuplicateEmail_ThrowsException() throws Exception {
        // Arrange
        UserInfoDto userInfo = TestDataBuilder.createRandomUserInfoDto();
        
        // Act - Save user first time
        userInfoService.newUserLogin(userInfo, TEST_TENANT_ID);

        // Assert - Should throw exception on duplicate
        assertThrows(DuplicateKeyException.class, () -> {
            userInfoService.newUserLogin(userInfo, TEST_TENANT_ID);
        });
    }

    @Test
    @DisplayName("Should handle different tenants correctly")
    void testSaveUser_DifferentTenants_Success() throws Exception {
        // Arrange
        UserInfoDto userInfo = TestDataBuilder.createValidUserInfoDto();
        String tenant1 = TestDataBuilder.getRandomTenantId();
        String tenant2 = TestDataBuilder.getRandomTenantId();

        // Act & Assert - Same user should be allowed in different tenants
        UserInfoDto savedUser1 = userInfoService.newUserLogin(userInfo, tenant1);
        UserInfoDto savedUser2 = userInfoService.newUserLogin(userInfo, tenant2);

        assertNotNull(savedUser1);
        assertNotNull(savedUser2);
        assertEquals(userInfo.getEmail(), savedUser1.getEmail());
        assertEquals(userInfo.getEmail(), savedUser2.getEmail());
    }

    @Test
    @DisplayName("Should handle special characters in user data")
    void testSaveUser_SpecialCharacters_Success() throws Exception {
        // Arrange
        UserInfoDto userInfo = new UserInfoDto();
        userInfo.setFirstName("José");
        userInfo.setLastName("García-López");
        userInfo.setEmail("jose.garcia.special@example.com");

        // Act
        UserInfoDto savedUser = userInfoService.newUserLogin(userInfo, TEST_TENANT_ID);

        // Assert
        assertNotNull(savedUser);
        assertEquals("José", savedUser.getFirstName());
        assertEquals("García-López", savedUser.getLastName());
    }

    @Test
    @DisplayName("Should handle null lastName gracefully")
    void testSaveUser_NullLastName_Success() throws Exception {
        // Arrange
        UserInfoDto userInfo = new UserInfoDto();
        userInfo.setFirstName("SingleName");
        userInfo.setLastName(null);
        userInfo.setEmail("singlename@example.com");

        // Act
        UserInfoDto savedUser = userInfoService.newUserLogin(userInfo, TEST_TENANT_ID);

        // Assert
        assertNotNull(savedUser);
        assertEquals("SingleName", savedUser.getFirstName());
        assertNull(savedUser.getLastName());
    }
}
package org.qube.microbeesapplication.utils;

import org.qube.microbeesapplication.models.dto.UserInfoDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Utility class for building test data objects
 */
public class TestDataBuilder {
    
    private static final Random random = new Random();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static UserInfoDto createValidUserInfoDto() {
        UserInfoDto userInfo = new UserInfoDto();
        userInfo.setFirstName("John");
        userInfo.setLastName("Doe");
        userInfo.setEmail("john.doe@example.com");
        return userInfo;
    }
    
    public static UserInfoDto createRandomUserInfoDto() {
        UserInfoDto userInfo = new UserInfoDto();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        userInfo.setFirstName("User" + random.nextInt(1000));
        userInfo.setLastName("Test" + random.nextInt(1000));
        userInfo.setEmail("user" + timestamp + random.nextInt(1000) + "@test.com");
        return userInfo;
    }
    
    public static UserInfoDto createUserInfoDtoWithInvalidEmail() {
        UserInfoDto userInfo = new UserInfoDto();
        userInfo.setFirstName("Invalid");
        userInfo.setLastName("User");
        userInfo.setEmail("invalid-email");
        return userInfo;
    }
    
    public static UserInfoDto createUserInfoDtoWithEmptyFields() {
        UserInfoDto userInfo = new UserInfoDto();
        userInfo.setFirstName("");
        userInfo.setLastName("");
        userInfo.setEmail("");
        return userInfo;
    }
    
    public static Map<String, Object> createTokenDto(String firstName, String email) {
        Map<String, Object> tokenDto = new HashMap<>();
        tokenDto.put("firstName", firstName);
        tokenDto.put("email", email);
        return tokenDto;
    }
    
    public static String getRandomTenantId() {
        return "tenant_" + random.nextInt(10000);
    }
    
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }
    
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }
}
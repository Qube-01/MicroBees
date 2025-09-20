package org.qube.microbeesapplication.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.qube.microbeesapplication.models.dto.UserInfoDto;
import org.qube.microbeesapplication.models.jpa.UserInfoJpa;

import static org.junit.jupiter.api.Assertions.*;

class MicroBeesMapperTest {

    private MicroBeesMapper microBeesMapper;
    private UserInfoDto userInfoDto;
    private UserInfoJpa expectedUserInfoJpa;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        microBeesMapper = new MicroBeesMapper();
        
        userInfoDto = new UserInfoDto();
        userInfoDto.setFirstName("John");
        userInfoDto.setLastName("Doe");
        userInfoDto.setEmail("john.doe@example.com");

        expectedUserInfoJpa = new UserInfoJpa();
        expectedUserInfoJpa.setFirstName("John");
        expectedUserInfoJpa.setLastName("Doe");
        expectedUserInfoJpa.setEmail("john.doe@example.com");
    }

    @Test
    void convertModel_Success() {
        // Act
        UserInfoJpa result = microBeesMapper.convertModel(userInfoDto);

        // Assert
        assertNotNull(result);
        assertEquals(userInfoDto.getFirstName(), result.getFirstName());
        assertEquals(userInfoDto.getLastName(), result.getLastName());
        assertEquals(userInfoDto.getEmail(), result.getEmail());
    }

    @Test
    void convertModel_NullDto() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            microBeesMapper.convertModel(null);
        });
    }

    @Test
    void convertModel_PartialData() {
        // Arrange
        UserInfoDto partialDto = new UserInfoDto();
        partialDto.setFirstName("Jane");
        // lastName and email are null

        // Act
        UserInfoJpa result = microBeesMapper.convertModel(partialDto);

        // Assert
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        assertNull(result.getLastName());
        assertNull(result.getEmail());
    }

    @Test
    void convertModel_EmptyStrings() {
        // Arrange
        UserInfoDto emptyDto = new UserInfoDto();
        emptyDto.setFirstName("");
        emptyDto.setLastName("");
        emptyDto.setEmail("");

        // Act
        UserInfoJpa result = microBeesMapper.convertModel(emptyDto);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getFirstName());
        assertEquals("", result.getLastName());
        assertEquals("", result.getEmail());
    }

    @Test
    void convertModel_LongStrings() {
        // Arrange
        UserInfoDto longDto = new UserInfoDto();
        longDto.setFirstName("A".repeat(100)); // Longer than @Size(max = 50) constraint
        longDto.setLastName("B".repeat(100));
        longDto.setEmail("C".repeat(100) + "@example.com");

        // Act
        UserInfoJpa result = microBeesMapper.convertModel(longDto);

        // Assert
        assertNotNull(result);
        assertEquals(longDto.getFirstName(), result.getFirstName());
        assertEquals(longDto.getLastName(), result.getLastName());
        assertEquals(longDto.getEmail(), result.getEmail());
    }

    @Test
    void convertModel_SpecialCharacters() {
        // Arrange
        UserInfoDto specialDto = new UserInfoDto();
        specialDto.setFirstName("José");
        specialDto.setLastName("O'Connor");
        specialDto.setEmail("josé.o'connor@example.com");

        // Act
        UserInfoJpa result = microBeesMapper.convertModel(specialDto);

        // Assert
        assertNotNull(result);
        assertEquals("José", result.getFirstName());
        assertEquals("O'Connor", result.getLastName());
        assertEquals("josé.o'connor@example.com", result.getEmail());
    }
}
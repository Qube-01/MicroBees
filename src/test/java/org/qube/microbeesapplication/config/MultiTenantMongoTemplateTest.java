package org.qube.microbeesapplication.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultiTenantMongoTemplateTest {

    @Test
    void constructor_InitializesCorrectly() {
        // Test that constructor doesn't throw exception with valid parameters
        assertDoesNotThrow(() -> {
            new MultiTenantMongoTemplate(
                    "mongodb://localhost/test",
                    "org.qube.microbeesapplication.models.jpa",
                    "TEST_"
            );
        });
    }

    @Test 
    void constructor_HandlesNullConnectionString() {
        // Test that constructor handles null connection string
        assertThrows(Exception.class, () -> {
            new MultiTenantMongoTemplate(
                    null,
                    "org.qube.microbeesapplication.models.jpa", 
                    "TEST_"
            );
        });
    }

    @Test
    void constructor_HandlesEmptyPackage() {
        // Test that constructor handles empty package name
        assertDoesNotThrow(() -> {
            new MultiTenantMongoTemplate(
                    "mongodb://localhost/test",
                    "",
                    "TEST_"
            );
        });
    }

    @Test
    void constructor_HandlesNullPrefix() {
        // Test that constructor handles null prefix
        assertThrows(Exception.class, () -> {
            new MultiTenantMongoTemplate(
                    "mongodb://localhost/test",
                    "org.qube.microbeesapplication.models.jpa",
                    null
            );
        });
    }
}
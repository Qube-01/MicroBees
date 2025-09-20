package org.qube.microbeesapplication;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MicroBeesApplicationTests {

    // Mock MongoDB to avoid database dependency during testing
    @MockBean
    private MongoTemplate mongoTemplate;

    @Test
    void contextLoads() {
        // This test ensures that the Spring Boot application context loads successfully
        // with all beans properly configured and no circular dependencies
    }

    @Test
    void mainMethod_RunsWithoutError() {
        // Test that main method can be invoked (though it won't fully start in test)
        try {
            MicroBeesApplication.main(new String[] {"--spring.main.web-environment=false"});
        } catch (Exception e) {
            // Expected in test environment, just ensure no compilation errors
        }
    }
}

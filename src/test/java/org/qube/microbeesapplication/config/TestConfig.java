package org.qube.microbeesapplication.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {
    
    @Bean
    @Primary
    public MongoTemplate mongoTemplate() {
        return mock(MongoTemplate.class);
    }
    
    @Bean
    @Primary 
    public MultiTenantMongoTemplate multiTenantMongoTemplate() {
        return mock(MultiTenantMongoTemplate.class);
    }
}
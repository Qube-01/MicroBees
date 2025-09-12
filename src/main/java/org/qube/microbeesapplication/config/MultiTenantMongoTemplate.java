package org.qube.microbeesapplication.config;

import com.mongodb.client.MongoClients;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.qube.microbeesapplication.utils.Constants.CONTAINER_PREFIX;

@Component
@RequiredArgsConstructor
public class MultiTenantMongoTemplate {

    private final Map<String, MongoDatabaseFactory>  mongoDatabaseFactoryMap = new ConcurrentHashMap<>();

    private MongoDatabaseFactory getMongoDatabaseFactory(String tenantId) {
        return mongoDatabaseFactoryMap.computeIfAbsent(tenantId, id ->
                new SimpleMongoClientDatabaseFactory(
                        MongoClients.create("mongodb+srv://kousikd2003_db_user:mUOQ5yQb7cE2ntIp@cluster0.xt9pqbq.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0"),
                        CONTAINER_PREFIX+tenantId));
    }

    public MongoTemplate getMongoTemplate(String tenantId) {
        return new MongoTemplate(this.getMongoDatabaseFactory(tenantId));
    }
}

package org.qube.microbeesapplication.config;

import com.mongodb.client.MongoClients;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.qube.microbeesapplication.utils.Constants.CONTAINER_PREFIX;

@Slf4j
@Component
public class MultiTenantMongoTemplate {

    private final Map<String, MongoTemplate> mongoTemplateMap = new ConcurrentHashMap<>();

    private final MongoMappingContext mongoMappingContext = new MongoMappingContext();
    private final Map<Class<?>, List<IndexDefinition>> indexDefinitionsCache = new ConcurrentHashMap<>();
    private final Set<Class<?>> entitySet;

    private final String connectionString;
    private final String containerPrefix;

    public MultiTenantMongoTemplate(
            @Value("${spring.data.mongodb.uri}") String connectionString,
            @Value("${app.mongo.entity-package:org.qube.microbeesapplication}") String entityBasePackage,
            @Value("${app.container-prefix:" + CONTAINER_PREFIX + "}") String containerPrefix
    ) {
        this.connectionString = connectionString;
        this.containerPrefix = containerPrefix;
        this.entitySet = scanForDocumentEntities(entityBasePackage);

        mongoMappingContext.setInitialEntitySet(entitySet);
        try {
            mongoMappingContext.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MongoMappingContext", e);
        }
        initIndexCache();
    }

    private void initIndexCache() {
        MongoPersistentEntityIndexResolver resolver = new MongoPersistentEntityIndexResolver(mongoMappingContext);
        for (Class<?> entity : entitySet) {
            List<IndexDefinition> defs = new ArrayList<>();
            resolver.resolveIndexFor(entity).forEach(defs::add);
            if (!defs.isEmpty()) {
                indexDefinitionsCache.put(entity, defs);
                log.info("Cached {} index definition(s) for {}", defs.size(), entity.getSimpleName());
            }
        }
    }

    private Set<Class<?>> scanForDocumentEntities(String basePackage) {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Document.class));

        Set<Class<?>> classes = new HashSet<>();
        for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
            try {
                classes.add(Class.forName(bd.getBeanClassName()));
            } catch (ClassNotFoundException e) {
                log.warn("Failed to load class {}", bd.getBeanClassName(), e);
            }
        }
        return classes;
    }

    private MongoTemplate createMongoTemplate(String tenantId) {
        MongoDatabaseFactory factory = new SimpleMongoClientDatabaseFactory(
                MongoClients.create(connectionString),
                containerPrefix + tenantId
        );
        MongoTemplate template = new MongoTemplate(factory);
        applyIndexes(template);

        return template;
    }

    private void applyIndexes(MongoTemplate template) {
        indexDefinitionsCache.forEach((entity, defs) -> {
            defs.forEach(def -> {
                try {
                    template.indexOps(entity).ensureIndex(def);
                } catch (Exception ex) {
                    log.warn("Could not create index for {} in DB {}: {}",
                            entity.getSimpleName(), template.getDb().getName(), ex.getMessage());
                }
            });
        });
    }

    public MongoTemplate getMongoTemplate(String tenantId) {
        return mongoTemplateMap.computeIfAbsent(tenantId, this::createMongoTemplate);
    }
}

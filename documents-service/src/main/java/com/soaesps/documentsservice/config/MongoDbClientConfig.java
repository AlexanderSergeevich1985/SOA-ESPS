package com.soaesps.documentsservice.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * MongoDB client configuration.
 * NOTE: In Spring Boot 3, this class can be deleted entirely — see Option 2.
 */
@Configuration
public class MongoDbClientConfig {

    @Value("${spring.mongoDb.server:localhost}")
    private String mongoDbAddress;

    @Value("${spring.mongoDb.port:27017}")
    private int mongoDbPort;

    @Value("${spring.mongoDb.databaseName:documents}")
    private String mongoDbDatabaseName;

    @Bean
    public MongoClient mongoClient() {
        String connectionString = String.format("mongodb://%s:%d", mongoDbAddress, mongoDbPort);
        return MongoClients.create(connectionString);
    }

    /**
     * Injected MongoClient as a method parameter instead of calling mongo() directly.
     * This is cleaner and avoids relying on CGLIB proxy interception.
     */
    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, mongoDbDatabaseName);
    }
}
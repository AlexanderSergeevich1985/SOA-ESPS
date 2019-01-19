package com.soaesps.documentsservice.config;

import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoDbClientConfig {
    @Value("${spring.mongoDb.server}")
    private String mongoDbAddress;

    @Value("${spring.mongoDb.port}")
    private String mongoDbPort;

    @Value("${spring.mongoDb.databaseName}")
    private String mongoDbDatabaseName;

    @Bean
    public MongoClient mongo() {
        return new MongoClient(mongoDbAddress, Integer.getInteger(mongoDbPort));
    }

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(mongo(), mongoDbDatabaseName);
    }
}
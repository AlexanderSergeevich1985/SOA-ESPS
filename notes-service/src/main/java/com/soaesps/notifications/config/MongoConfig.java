package com.soaesps.notifications.config;

import com.mongodb.*;
import com.mongodb.client.MongoClient;

import com.mongodb.client.MongoClients;
import com.mongodb.connection.ServerSettings;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.soaesps.notifications.repository")
public class MongoConfig extends AbstractMongoClientConfiguration {
    private static final int MONGO_MAX_POOL_SIZE = 4;

    private static final int MONGO_MIN_POOL_SIZE = 1;

    @Value("${spring.data.mongodb.host}")
    private String mongoDbHost;

    @Value("${spring.data.mongodb.port}")
    private String mongoDbPort;

    @Value("${spring.data.mongodb.username}")
    private String username;

    @Value("${spring.data.mongodb.password}")
    private String password;

    @Value("${spring.data.mongodb.database}")
    private String mongoDbDatabaseName;

    @Override
    public String getDatabaseName() {
        return mongoDbDatabaseName;
    }

    @Override
    @Bean
    public MongoClient mongoClient() {
        final ServerAddress address = new ServerAddress("127.0.0.1", 27017);
        final MongoCredential credential = MongoCredential
                .createCredential(username, getDatabaseName(), password.toCharArray());
        final MongoClientOptions options = new MongoClientOptions.Builder().build();
        final MongoClientSettings settings = MongoClientSettings.builder()
                .readPreference(ReadPreference.secondaryPreferred())
                .applyToConnectionPoolSettings(builder -> builder.maxSize(MONGO_MAX_POOL_SIZE).minSize(MONGO_MIN_POOL_SIZE))
                .applyToServerSettings(builder -> builder
                        .applySettings(ServerSettings
                                .builder()
                                .applyConnectionString(connectionString())
                                .build()))
                .credential(credential)
                .build();
        return MongoClients.create();
    }

    protected ConnectionString connectionString() {
        return new ConnectionString("mongodb://".concat(mongoDbHost).concat(":").concat(mongoDbPort));
    }
}
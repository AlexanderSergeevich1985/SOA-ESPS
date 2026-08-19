package com.soaesps.notifications.repository;

import com.soaesps.notifications.domain.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@ExtendWith(SpringExtension.class)
public class MongoDbTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    public void setUp() {
        // Initialization before each test if needed
    }

    @Test
    public void testMongoConnection() {
        Message message = new Message();
        // Set fields for your message object here

        Message savedMessage = mongoTemplate.save(message);

        // Modern JUnit 5 assertions
        assertNotNull(savedMessage);
        assertNotNull(savedMessage.getMessageId());
    }
}

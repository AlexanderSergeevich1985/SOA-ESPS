package com.soaesps.notifications.repository;

import com.soaesps.notifications.domain.Message;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

@DataMongoTest
@ExtendWith(SpringExtension.class)
@RunWith(SpringRunner.class)
public class MongoDbTest {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MessageRepository messageRepository;

    @Before
    public void before() {
        Assert.assertNotNull(messageRepository);
    }

    @Test
    public void test() {
        final Message message = messageRepository.save(getTestMsg());
        Assert.assertNotNull(message);
        Assert.assertNotNull(message.getMessageId());
        Assert.assertNotNull(messageRepository.findById(message.getMessageId()));
    }

    protected Message getTestMsg() {
        final Message message = new Message();
        message.setMailer("test@mail.com");

        return message;
    }
}
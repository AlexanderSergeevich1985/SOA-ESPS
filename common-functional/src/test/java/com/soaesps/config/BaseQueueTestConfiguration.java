package com.soaesps.config;

import com.soaesps.Utils.DataStructure.BaseQueueTest;
import com.soaesps.core.Utils.DataStructure.BaseQueue;
import com.soaesps.core.Utils.DataStructure.LimitedQueue;
import com.soaesps.core.Utils.DataStructure.QueueI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BaseQueueTestConfiguration {
    @Bean("baseQueue")
    public QueueI<BaseQueueTest.TestObject> baseQueue() {
        QueueI<BaseQueueTest.TestObject> queue = new BaseQueue<>();

        return queue;
    }

    @Bean("limitedQueue")
    public QueueI<BaseQueueTest.TestObject> limitedQueue() {
        QueueI<BaseQueueTest.TestObject> queue = new LimitedQueue<>();

        return queue;
    }
}
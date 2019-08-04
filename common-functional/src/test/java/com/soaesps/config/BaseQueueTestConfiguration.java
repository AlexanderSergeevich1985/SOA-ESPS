package com.soaesps.config;

import com.soaesps.Utils.DataStructure.BaseQueueTest;
import com.soaesps.core.Utils.DataStructure.BaseQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BaseQueueTestConfiguration {
    @Bean("baseQueue")
    public BaseQueue<BaseQueueTest.TestObject> baseQueue() {
        BaseQueue<BaseQueueTest.TestObject> queue = new BaseQueue();
        return queue;
    }
}
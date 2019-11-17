package com.soaesps.aggregator.config;

import com.soaesps.aggregator.messages.BaseMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MessageConverter;

@Configuration
public class StreamConfiguration {
    @Bean
    public MessageConverter providesTextPlainMessageConverter() {
        return new BaseMessageConverter();
    }
}
package com.soaesps.aggregator.config;

import com.soaesps.aggregator.messages.BaseMessageConverter;
import com.soaesps.aggregator.processor.ProcessorI;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MessageConverter;

@Configuration
@EnableBinding(ProcessorI.class)
public class StreamConfiguration {
    @Bean
    public MessageConverter providesTextPlainMessageConverter() {
        return new BaseMessageConverter();
    }
}
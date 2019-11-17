package com.soaesps.aggregator.processor;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface ProcessorI {
    String INPUT = "myInput";

    @Input
    SubscribableChannel input();

    @Output("myOutput")
    MessageChannel output();

    @Output
    MessageChannel anotherOutput();
}
package com.soaesps.aggregator.processor;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface SinkI {
    String INPUT = "msgInput";

    @Input(SinkI.INPUT)
    SubscribableChannel msgInput();
}
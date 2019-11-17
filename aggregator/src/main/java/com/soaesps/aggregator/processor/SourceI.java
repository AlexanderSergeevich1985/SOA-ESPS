package com.soaesps.aggregator.processor;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface SourceI {
    String OUTPUT = "msgOutput";

    @Output(SourceI.OUTPUT)
    MessageChannel msgOutput();
}
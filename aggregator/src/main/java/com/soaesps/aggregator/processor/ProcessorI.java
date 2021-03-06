package com.soaesps.aggregator.processor;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface ProcessorI extends SourceI, SinkI {
    String SPECIAL_OUTPUT = "specialOutput";

    @Output
    MessageChannel specialOutput();
}
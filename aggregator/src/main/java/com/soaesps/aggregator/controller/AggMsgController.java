package com.soaesps.aggregator.controller;

import com.soaesps.aggregator.domain.CompletionTask;
import com.soaesps.aggregator.domain.RegisteredTask;
import com.soaesps.aggregator.service.AggregatorServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class AggMsgController {
    @Autowired
    private AggregatorServiceI aggregatorServiceI;

    @MessageMapping("/task")
    @SendTo("/topic/register")
    public RegisteredTask register(final RegisteredTask regTask) throws Exception {
        return aggregatorServiceI.register(regTask);
    }

    @MessageMapping("/task")
    @SendTo("/topic/complete")
    public CompletionTask complete(final CompletionTask compTask) throws Exception {
        return aggregatorServiceI.complete(compTask);
    }
}
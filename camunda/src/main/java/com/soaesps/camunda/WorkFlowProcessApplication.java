package com.soaesps.camunda;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.camunda.bpm.spring.boot.starter.event.PostDeployEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@EnableProcessApplication
public class WorkFlowProcessApplication {
    @Autowired
    private RuntimeService runtimeService;

    public static void main(final String[] args) {
        SpringApplication.run(WorkFlowProcessApplication.class, args);
    }

    @EventListener
    private void processPostDeploy(final PostDeployEvent event) {
        runtimeService.startProcessInstanceByKey("loanApproval");
    }
}
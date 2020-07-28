package com.soaesps.dataprocess;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.dataflow.server.EnableDataFlowServer;

@EnableDataFlowServer
@SpringBootApplication
public class DataProcessApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataProcessApplication.class, args);
    }
}
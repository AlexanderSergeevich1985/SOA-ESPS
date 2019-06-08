package com.soaesps.schedulerservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

@SpringBootApplication
@EnableConfigurationProperties
@EnableAutoConfiguration
public class SchedulerApplication implements EnvironmentAware {//extends ResourceServerConfigurerAdapter {
    private static Logger logger = LoggerFactory.getLogger(SchedulerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
    }

    @Override
    public void setEnvironment(Environment environment) {
        //logger.info("URL = {}", environment.getRequiredProperty("url"));
    }
}
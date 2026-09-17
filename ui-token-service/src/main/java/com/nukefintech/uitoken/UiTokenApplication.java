package com.nukefintech.uitoken;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Production-ready reactive entry point for the UI Token Service.
 * Orchestrates server-driven UI tokens metadata logic.
 */
@SpringBootApplication
public class UiTokenApplication {

    public static void main(String[] args) {
        SpringApplication.run(UiTokenApplication.class, args);
    }
}
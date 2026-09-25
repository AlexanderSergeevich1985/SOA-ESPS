package com.soaesps.coordinator.config;

import com.soaesps.coordinator.filter.RateLimitingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilter(
            RateLimitingFilter rateLimitingFilter) {

        FilterRegistrationBean<RateLimitingFilter> registration =
                new FilterRegistrationBean<>();

        registration.setFilter(rateLimitingFilter);
        registration.addUrlPatterns("/api/*"); // Apply to API endpoints
        registration.setOrder(1); // Execute early in the chain

        return registration;
    }
}
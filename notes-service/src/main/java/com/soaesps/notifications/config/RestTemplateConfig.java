package com.soaesps.notifications.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        // Initialize RestTemplate with custom request factory
        return new RestTemplate(clientHttpRequestFactory());
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        // 1. Configure the connection pool
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);             // Maximum total connections allowed
        connectionManager.setDefaultMaxPerRoute(20);     // Maximum connections allowed per route/host

        // 2. Configure request timeouts
        RequestConfig requestConfig = RequestConfig.custom()
                // Time to wait for a connection from the connection pool
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(3000))
                // Time to wait for data after the connection is established (Read Timeout)
                .setResponseTimeout(Timeout.ofMilliseconds(5000))
                .build();

        // 3. Build the Apache CloseableHttpClient
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        // 4. Wrap the HttpClient inside Spring's request factory
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        // Time to establish the initial TCP connection (Connect Timeout)
        factory.setConnectTimeout(2000);

        return factory;
    }
}
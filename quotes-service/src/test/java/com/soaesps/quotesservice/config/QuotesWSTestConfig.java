package com.soaesps.quotesservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;

@TestConfiguration
public class QuotesWSTestConfig {
    @Bean
    Jaxb2Marshaller jaxb2Marshaller() {
        final Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setContextPath("com.nukefintech.types.market");

        return jaxb2Marshaller;
    }

    @Bean
    public WebServiceTemplate webServiceTemplate() {
        final WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
        webServiceTemplate.setMarshaller(jaxb2Marshaller());
        webServiceTemplate.setUnmarshaller(jaxb2Marshaller());
        webServiceTemplate.setDefaultUri("http://localhost:8080/nukefintech/ws/quotes/");

        return webServiceTemplate;
    }
}
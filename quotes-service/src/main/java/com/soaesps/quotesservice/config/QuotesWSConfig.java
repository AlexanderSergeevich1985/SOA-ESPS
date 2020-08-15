package com.soaesps.quotesservice.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition;

import javax.servlet.Servlet;

@EnableWs
@Configuration
public class QuotesWSConfig {
    @Bean
    public ServletRegistrationBean<Servlet> messageDispatcherServlet(
            ApplicationContext applicationContext) {
        final MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);

        return new ServletRegistrationBean<>(servlet, "/nukefintech/ws/*");
    }

    @Bean(name = "quotes")
    public Wsdl11Definition defaultWsdl11Definition() {
        final SimpleWsdl11Definition wsdl11Definition = new SimpleWsdl11Definition();
        wsdl11Definition.setWsdl(new ClassPathResource("/wsdl/Quotes.wsdl"));

        return wsdl11Definition;
    }
}
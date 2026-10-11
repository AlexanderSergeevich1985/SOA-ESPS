package com.soaesps.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
@EnableDiscoveryClient
@EnableMethodSecurity
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    @Configuration
    protected static class OAuth2AuthorizationConfig {

        // Modern Spring Authorization Server client repository template mapping legacy layout
        @Bean
        public RegisteredClientRepository inMemoryRegisteredClientRepository(Environment env, TokenSettings tokenSettings) {
            List<RegisteredClient> clients = new ArrayList<>();

            // 1. Browser client setup
            clients.add(RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId("browser")
                    .clientAuthenticationMethod(ClientAuthenticationMethod.NONE) // Public client mapping form credentials
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .authorizationGrantType(AuthorizationGrantType.PASSWORD)
                    .scope("ui")
                    .tokenSettings(tokenSettings)
                    .build());

            // 2. Profile Service client setup
            clients.add(createMicroserviceClient("profile-service", env.getProperty("PROFILE_SERVICE_PASSWORD"), tokenSettings));

            // 3. Payments Service client setup
            clients.add(createMicroserviceClient("payments-service", env.getProperty("PAYMENTS_SERVICE_PASSWORD"), tokenSettings));

            // 4. Message Process client setup
            clients.add(createMicroserviceClient("msg-process", env.getProperty("MSG_PROCESS_PASSWORD"), tokenSettings));

            return new InMemoryRegisteredClientRepository(clients);
        }

        /**
         * Helper factory mapping secure server-to-server microservice OAuth2 credentials.
         */
        private RegisteredClient createMicroserviceClient(String clientName, String clientSecret, TokenSettings tokenSettings) {
            // Fallback placeholder if system environment variable defaults are absent during boot verification
            String secureSecret = (clientSecret != null && !clientSecret.isEmpty()) ? clientSecret : "temporary-dev-secret-" + clientName;

            return RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId(clientName)
                    .clientSecret("{noop}" + secureSecret) // Using {noop} delegation plain text mapping for internal routing
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .scope("server")
                    .tokenSettings(tokenSettings)
                    .build();
        }
    }
}

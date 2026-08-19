package com.soaesps.notifications.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Initializes the Firebase Admin SDK from a service-account JSON file.
 * The bean is created only when notification.push.enabled=true,
 * so local/dev profiles run without any Firebase credentials.
 */
@Configuration
@ConditionalOnProperty(name = "notification.push.enabled", havingValue = "true")
public class FirebaseConfiguration {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfiguration.class);

    /**
     * Supports standard Spring resource prefixes like "file:/path/to/file.json"
     * or "classpath:firebase-credentials.json".
     */
    @Value("${notification.push.firebase.credentials-path}")
    private Resource credentialsResource;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        if (credentialsResource == null || !credentialsResource.exists()) {
            throw new IllegalStateException("Firebase push is enabled, but credentials file at '"
                    + credentialsResource + "' does not exist!");
        }

        // Do NOT use try-with-resources here. GoogleCredentials needs the stream
        // to remain open or unmanaged by the local block for background OAuth2 token refreshing.
        InputStream is = credentialsResource.getInputStream();

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(is))
                .build();

        log.info("Firebase Admin SDK successfully initialized from {}", credentialsResource.getDescription());
        return FirebaseApp.initializeApp(options);
    }

    /**
     * Provides the FirebaseMessaging client as a Spring bean for dependency injection
     * inside FcmNotificationChannel.
     */
    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}

package com.soaesps.coordinator.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for gRPC mTLS security.
 */
@Data
@Component
@ConfigurationProperties(prefix = "coordinator.grpc.security")
public class GrpcSecurityProperties {

    /**
     * Enables or disables mTLS. Should be true in production.
     */
    private boolean enabled = true;

    /**
     * Path to the CA certificate used to verify the remote server's certificate.
     */
    private String trustCertCollectionFilePath;

    /**
     * Path to the client's certificate chain (proves client identity to the server).
     */
    private String clientCertChainFilePath;

    /**
     * Path to the client's private key (must be in PKCS#8 format).
     */
    private String clientPrivateKeyFilePath;
}
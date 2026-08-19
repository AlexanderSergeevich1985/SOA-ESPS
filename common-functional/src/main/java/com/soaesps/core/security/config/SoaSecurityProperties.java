package com.soaesps.core.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized security properties for all SOA-ESPS microservices.
 * Each service declares which authentication mechanisms it requires via application.yml.
 */
@ConfigurationProperties(prefix = "soa.security")
public class SoaSecurityProperties {

    private final Mtls mtls = new Mtls();
    private final Jwt jwt = new Jwt();

    /** Paths accessible without any authentication (health checks, public API). */
    private List<String> permitAll = new ArrayList<>(List.of("/actuator/health/**", "/actuator/info"));

    /** Paths reserved exclusively for inter-service mTLS calls. */
    private List<String> internal = new ArrayList<>(List.of("/api/internal/**"));

    public Mtls getMtls() { return mtls; }
    public Jwt getJwt() { return jwt; }
    public List<String> getPermitAll() { return permitAll; }
    public void setPermitAll(List<String> permitAll) { this.permitAll = permitAll; }
    public List<String> getInternal() { return internal; }
    public void setInternal(List<String> internal) { this.internal = internal; }

    public static class Mtls {
        /** Enables X.509 client-certificate authentication for inter-service calls. */
        private boolean enabled = true;
        /** Role granted to any service authenticated via a trusted client certificate. */
        private String serviceRole = "SERVICE";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getServiceRole() { return serviceRole; }
        public void setServiceRole(String serviceRole) { this.serviceRole = serviceRole; }
    }

    public static class Jwt {
        /** Enables OAuth2 Resource Server (Bearer token) authentication for end users. */
        private boolean enabled = true;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}
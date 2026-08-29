package com.soaesps.auth.service;

import com.soaesps.auth.repository.OAuth2TokenRepository;
import com.soaesps.core.security.repository.AuthAuditRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.token.Token;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
class AuthTokenServiceTest {

    @MockitoBean
    private OAuth2TokenRepository tokenRepository;

    @MockitoBean
    private AuthAuditRepository authAuditRepository;

    @Autowired
    private TokenService tokenService;

    @Test
    void contextLoads() {
        assertNotNull(tokenService);
    }

    @Test
    void allocateToken_test() {
        Token token = tokenService.allocateToken("test");
        assertNotNull(token);
    }

    @Test
    void verifyToken_test() {
        Token token = tokenService.allocateToken("test");
        assertNotNull(token);

        Token verified = tokenService.verifyToken(token.getKey());
        assertNotNull(verified);
    }
}
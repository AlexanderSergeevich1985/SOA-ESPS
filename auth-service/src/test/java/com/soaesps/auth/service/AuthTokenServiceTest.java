package com.soaesps.auth.service;

import com.soaesps.auth.repository.OAuth2TokenRepository;
import com.soaesps.core.security.repository.AuthAuditRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.token.Token;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AuthTokenServiceTest {
    @MockBean
    private OAuth2TokenRepository tokenRepository;

    @MockBean
    private AuthAuditRepository authAuditRepository;

    @Autowired
    private TokenService tokenService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void contextLoads() {
        Assert.assertNotNull(tokenService);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void allocateToken_test() {
        Token token = tokenService.allocateToken("test");
        Assert.assertNotNull(token);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    public void verifyToken_test() {
        Token token = tokenService.allocateToken("test");
        Assert.assertNotNull(token);
        token = tokenService.verifyToken(token.getKey());
        Assert.assertNotNull(token);
    }
}
package com.soaesps.auth.service.security;

import com.soaesps.auth.repository.OAuth2TokenRepository;
import com.soaesps.core.DataModels.security.AuthAudit;
import com.soaesps.core.DataModels.security.BaseOAuth2AccessToken;
import com.soaesps.core.DataModels.security.BaseUserDetails;
import com.soaesps.core.DataModels.security.SecActionStatus;
import com.soaesps.core.Utils.HashGeneratorHelper;
import com.soaesps.core.security.repository.AuthAuditRepository;
import com.soaesps.core.security.util.SecurityHelper;
import org.apache.commons.math3.random.RandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

@Service
public class AuthTokenService {
    private static final Logger logger = LoggerFactory.getLogger(AuthTokenService.class);

    public static final Integer DEFAULT_RAND_MAX_SEED = 100;
    public static final Integer DEFAULT_RAND_STRING_SIZE = 20;

    @Value("${app.security.secret-hash:secret}")
    private String secretHash;

    private PublicKey publicKey;
    private PrivateKey privateKey;
    private Integer serverInteger;
    private SecureRandom secureRandom;

    private final AccessTokenFactory accessTokenFactory;
    private final OAuth2TokenRepository oAuth2TokenRepository;
    private final AuthAuditRepository authAuditRepository;

    private static final RandomGenerator rng = HashGeneratorHelper
            .getRandomGenerator(Math.toIntExact(System.currentTimeMillis() % DEFAULT_RAND_MAX_SEED));

    @Autowired
    public AuthTokenService(final AccessTokenFactory accessTokenFactory,
                            final OAuth2TokenRepository tokenRepository,
                            final AuthAuditRepository authAuditRepository) {
        this.accessTokenFactory = accessTokenFactory;
        this.oAuth2TokenRepository = tokenRepository;
        this.authAuditRepository = authAuditRepository;
    }

    public BaseOAuth2AccessToken allocateToken(String extendedInformation) {
        final BaseUserDetails userDetails = null; // TODO: Здесь нужно передавать реального пользователя
        BaseOAuth2AccessToken token = null;
        try {
            token = accessTokenFactory.createAccessToken(userDetails);
            if (token != null) {
                oAuth2TokenRepository.save(token);
                final AuthAudit authAudit = new AuthAudit();
                authAuditRepository.save(authAudit);
            }
        } catch (final IOException ex) {
            logger.error("[AuthTokenService/allocateToken]: Ошибка при создании токена", ex);
        } catch (final Exception ex) {
            logger.error("[AuthTokenService/allocateToken]: Непредвиденная ошибка", ex);
        }

        return token;
    }

    public BaseOAuth2AccessToken verifyToken(final String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        try {
            if (AccessTokenFactory.checkTokenKey(key)) {
                return getToken(key);
            }
        } catch (IOException ex) {
            logger.warn("Не удалось верифицировать ключ токена: {}", key, ex);
            return null;
        }

        return null;
    }

    private BaseOAuth2AccessToken getToken(String key) {
        BaseOAuth2AccessToken token = null;
        try {
            token = accessTokenFactory.createAccessToken(null);
            if (token != null) {
                token.setKey(key);
            }
        } catch (IOException ex) {
            logger.error("Ошибка при инициализации токена", ex);
        }
        return token;
    }

    private AuthAudit authAudit(final BaseUserDetails details,
                                final String ipAdress,
                                final SecActionStatus status) {
        final AuthAudit authAudit = SecurityHelper.initAuthAudit();

        if (details != null && details.getId() != null) {
            authAudit.setUserId(Long.valueOf(details.getId()));
        }
        authAudit.setIpAddress(ipAdress);
        authAudit.setStatus(status);

        return authAudit;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(final PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(final PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public String getSecretHash() {
        return secretHash;
    }

    public void setSecretHash(final String secretHash) {
        this.secretHash = secretHash;
    }
}
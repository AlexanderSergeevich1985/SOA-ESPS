package com.soaesps.auth.service.security;

import com.soaesps.auth.repository.OAuth2TokenRepository;
import com.soaesps.core.DataModels.security.AuthAudit;
import com.soaesps.core.DataModels.security.BaseOAuth2AccessToken;
import com.soaesps.core.DataModels.security.BaseUserDetails;
import com.soaesps.core.DataModels.security.SecActionStatus;
import com.soaesps.core.Utils.HashGeneratorHelper;
import com.soaesps.core.Utils.TimeSynchronizer;
import com.soaesps.core.security.repository.AuthAuditRepository;
import com.soaesps.core.security.util.SecurityHelper;
import org.apache.commons.math3.random.RandomGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.token.Token;
import org.springframework.security.core.token.TokenService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class AuthTokenService implements TokenService {
    static private final Logger logger;

    static {
        logger = Logger.getLogger(TimeSynchronizer.class.getName());
        logger.setLevel(Level.INFO);
    }

    public static Integer DEFAULT_RAND_MAX_SEED = 100;

    public static Integer DEFAULT_RAND_STRING_SIZE = 20;

    @Value("secret")
    private String secretHash;

    private PublicKey publicKey;

    private PrivateKey privateKey;

    private Integer serverInteger;

    private SecureRandom secureRandom;

    private final AccessTokenFactory accessTokenFactory;

    private final OAuth2TokenRepository oAuth2TokenRepository;

    private AuthAuditRepository authAuditRepository;

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

    @Override
    public Token allocateToken(String extendedInformation) {
        final BaseUserDetails userDetails = null;
        BaseOAuth2AccessToken token = null;
        try {
            token = accessTokenFactory.createAccessToken(userDetails);
            oAuth2TokenRepository.save(token);
            final AuthAudit authAudit = new AuthAudit();
            authAuditRepository.save(authAudit);
        } catch (final IOException ex) {
            if(logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, "[AuthTokenService/allocateToken]: ", ex);
            }
        }

        return token;
    }

    @Override
    public Token verifyToken(final String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        try {
            if (AccessTokenFactory.checkTokenKey(key)) {
                return getToken(key);
            }
        } catch (IOException ex) {
            return null;
        }

        return null;
    }

    private BaseOAuth2AccessToken getToken(String key) {
        BaseOAuth2AccessToken token = null;
        try {
            token = accessTokenFactory.createAccessToken(null);
            token.setKey(key);
        } catch (IOException ex) {}

        return token;
    }

    /*private String generateRandomStr(final Long creationTime) throws NoSuchAlgorithmException {
        HashGeneratorHelper.mixTwoString();
        return
    }

    private String computeServerSecret(long time) {
        return serverSecret + ":" + new Long(time % serverInteger).intValue();
    }

    public void setServerSecret(String serverSecret) {
        this.serverSecret = serverSecret;
    }

    public void setSecureRandom(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }*/


    /*public void setPseudoRandomNumberBytes(int pseudoRandomNumberBytes) {
        Assert.isTrue(pseudoRandomNumberBytes >= 0,
                "Must have a positive pseudo random number bit size");
        this.pseudoRandomNumberBytes = pseudoRandomNumberBytes;
    }

    public void setServerInteger(Integer serverInteger) {
        this.serverInteger = serverInteger;
    }

    public void afterPropertiesSet() {
        Assert.hasText(serverSecret, "Server secret required");
        Assert.notNull(serverInteger, "Server integer required");
        Assert.notNull(secureRandom, "SecureRandom instance required");
    }

    public AuthAudit verifyAccessToken(final BaseOAuth2AccessToken accessToken) {
        if (accessToken != null) {
            accessToken.getExpiresIn()
        }
        accessTokenFactory.
        final AuthAudit authAudit = new AuthAudit();
        authAudit.setActionDate(DateTimeHelper.getLocalCurrentTime());
        authAudit.setUserId(Long.valueOf(details.getId()));
        authAudit.setIpAddress(ipAdress);
        authAudit.setStatus(status);

        return authAudit;
    }*/

    private AuthAudit authAudit(final BaseUserDetails details,
                                final String ipAdress,
                                final SecActionStatus status) {
        final AuthAudit authAudit = SecurityHelper.initAuthAudit();
        authAudit.setUserId(Long.valueOf(details.getId()));
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
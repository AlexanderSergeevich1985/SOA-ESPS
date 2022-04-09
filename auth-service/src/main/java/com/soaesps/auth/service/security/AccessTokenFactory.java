package com.soaesps.auth.service.security;

import com.soaesps.auth.repository.OAuth2TokenRepository;

import com.soaesps.core.DataModels.security.BaseOAuth2AccessToken;
import com.soaesps.core.DataModels.security.BaseOAuth2RefreshToken;
import com.soaesps.core.Utils.CryptoHelper;
import com.soaesps.core.Utils.DateTimeHelper;

import com.soaesps.core.Utils.HashGeneratorHelper;
import com.soaesps.core.Utils.audit.AuditHelper;
import org.apache.commons.math3.random.RandomGenerator;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.security.*;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

import static com.soaesps.core.exception.ExceptionMsg.INVALID_TOKEN_MSG;

public interface AccessTokenFactory {
    Integer DEFAULT_RAND_MAX_SEED = 100;

    Integer DEFAULT_RAND_STRING_SIZE = 20;

    String SERVER_SECRET = HashGeneratorHelper.getRandSequence(DEFAULT_RAND_MAX_SEED,
            DEFAULT_RAND_STRING_SIZE);

    public static class AccessTokenFactoryHolder {
        private static RandomGenerator rng = HashGeneratorHelper
                .getRandomGenerator(new Random(System.nanoTime()).nextInt());
    }

    static String calcTokenKey(String secret, Long epochMilli) throws IOException {
        try {
            Long ip = AuditHelper.getNumericIP(AuditHelper.getClientIpAddressIfServletRequestExist());
            String userName = AuditHelper.getClientUserName();

            return calcTokenKey(secret, ip.toString(), epochMilli.toString(), userName);
        } catch (NoSuchAlgorithmException ex) {
            throw new IOException(ex);
        }
    }

    static String calcTokenKey(String secret, String... values) throws IOException, NoSuchAlgorithmException {
        String tokenKey = String.join(":", values);
        String tokenKeyWithSecret = tokenKey + secret;

        return tokenKey + ":" + CryptoHelper.getObjectDigest(tokenKeyWithSecret);
    }

    static boolean checkTokenKey(String tokenKey) throws IOException {
        return checkTokenKey(SERVER_SECRET, tokenKey);
    }

    static boolean checkTokenKey(String secret, String tokenKey) throws IOException {
        String[] values = tokenKey.split(":");
        Long ip = AuditHelper.getNumericIP(AuditHelper.getClientIpAddressIfServletRequestExist());
        if (!ip.equals(Long.valueOf(values[0]))) {
            throw new AuthenticationServiceException(INVALID_TOKEN_MSG);
        }
        if (Long.valueOf(values[1]) <= ZonedDateTime.now().toInstant().toEpochMilli()) {
            throw new AuthenticationServiceException(INVALID_TOKEN_MSG);
        }
        String userName = AuditHelper.getClientUserName();
        if (userName == null || !userName.equals(values[2])) {
            throw new AuthenticationServiceException(INVALID_TOKEN_MSG);
        }
        String key = null;
        try {
            key = calcTokenKey(secret, ip.toString(), values[1], userName);
        } catch (NoSuchAlgorithmException ex) {
            throw new IOException(ex);
        }

        return tokenKey.equals(key);
    }

    static boolean isNotExpired(final Date expiration) {
        return expiration != null && expiration
                .before(Date.from(DateTimeHelper.getLocalCurrentTime().toInstant(ZoneOffset.UTC)));
    }

    static boolean isNotFaked(final BaseOAuth2RefreshToken refreshToken, String secret)
            throws IOException, NoSuchAlgorithmException {
        final String digest = refreshToken.getValue();
        refreshToken.setValue(secret);

        return CryptoHelper.getObjectDigest(refreshToken).equals(digest);
    }

    BaseOAuth2AccessToken createAccessToken(final UserDetails userDetails) throws IOException;

    static AccessTokenFactory getInstance(final OAuth2TokenRepository tokenRepository,
                                   final String secret, final long validityPeriod) {
        return new AccessTokenFactoryImpl(tokenRepository, secret, validityPeriod);
    }

    class AccessTokenFactoryImpl implements AccessTokenFactory {


        private static final RandomGenerator rng = HashGeneratorHelper
                .getRandomGenerator(Math.toIntExact(System.currentTimeMillis() % DEFAULT_RAND_MAX_SEED));

        private PublicKey publicKey;

        private PrivateKey privateKey;

        //private final String secret;

        private final long validityPeriod;

        private OAuth2TokenRepository tokenRepository;

        public AccessTokenFactoryImpl(final OAuth2TokenRepository tokenRepository,
                                      final String secret, final long validityPeriod) {
            this.tokenRepository = tokenRepository;
            //this.secret = secret;
            this.validityPeriod = validityPeriod;
            KeyPair keys = CryptoHelper.generate_RSA();
            this.privateKey = keys.getPrivate();
            this.publicKey = keys.getPublic();
        }

        public BaseOAuth2AccessToken createAccessToken(final UserDetails userDetails)
                throws IOException {
            final String secretStr = SERVER_SECRET;//(publicKey != null && privateKey != null) ? genSecretStr() : secret;
            final BaseOAuth2AccessToken accessToken =
                    new BaseOAuth2AccessToken(secretStr, validityPeriod);
            accessToken.setKey(calcTokenKey(secretStr, accessToken.getExpiration().getTime()));
            accessToken.getAdditionalInformation().put("pubKey",
                    Base64.getEncoder().encodeToString(publicKey.getEncoded()));

            final BaseOAuth2RefreshToken refreshToken =
                    new BaseOAuth2RefreshToken(secretStr, 10 * validityPeriod);
            try {
                refreshToken.setValue(CryptoHelper.getObjectDigest(refreshToken));
                accessToken.setRefreshToken(refreshToken);
                accessToken.setValue(CryptoHelper.getObjectDigest(accessToken));
            } catch (final NoSuchAlgorithmException ex) {
                return null;
            }
            //!!!!tokenRepository.save(accessToken);

            return accessToken;
        }

        public BaseOAuth2AccessToken reIssueAccessToken(final BaseOAuth2RefreshToken refreshToken)
                throws IOException, NoSuchAlgorithmException {
            final String secretStr = SERVER_SECRET;
            if (!isValidRefreshToken(refreshToken, secretStr)) {
                return null;
            }
            Optional<BaseOAuth2AccessToken> accessToken = tokenRepository.findByRefreshToken(refreshToken);
            if (!accessToken.isPresent()) {
                return null;
            }
            accessToken.get().setExpiresIn(validityPeriod);
            final String digest = HashGeneratorHelper.mixTwoString(secretStr,
                    accessToken.get().getExpiration().toString());
            accessToken.get().setValue(digest);
            accessToken.get().setValue(CryptoHelper.getObjectDigest(accessToken.get()));
            tokenRepository.save(accessToken.get());

            return accessToken.get();
        }

        protected boolean isValidRefreshToken(final BaseOAuth2RefreshToken refreshToken, String secret)
                throws IOException, NoSuchAlgorithmException {
            if (!isNotExpired(refreshToken.getExpiration())
                    || !isNotFaked(refreshToken, secret) || !isExist(refreshToken)) {
                return false;
            }

            return true;
        }

        protected boolean isExist(final BaseOAuth2RefreshToken refreshToken) {
            return tokenRepository.findByRefreshToken(refreshToken).isPresent();
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
    }
}
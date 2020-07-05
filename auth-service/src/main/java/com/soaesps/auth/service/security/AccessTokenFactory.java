package com.soaesps.auth.service.security;

import com.soaesps.auth.repository.OAuth2TokenRepository;

import com.soaesps.core.DataModels.security.BaseOAuth2AccessToken;
import com.soaesps.core.DataModels.security.BaseOAuth2RefreshToken;
import com.soaesps.core.Utils.CryptoHelper;
import com.soaesps.core.Utils.DateTimeHelper;

import com.soaesps.core.Utils.HashGeneratorHelper;
import org.apache.commons.math3.random.RandomGenerator;
import org.springframework.security.core.userdetails.UserDetails;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.*;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

public interface AccessTokenFactory {
    BaseOAuth2AccessToken createAccessToken(final UserDetails userDetails) throws IOException;

    static AccessTokenFactory getInstance(final OAuth2TokenRepository tokenRepository,
                                   final String secret, final long validityPeriod) {
        return new AccessTokenFactoryImpl(tokenRepository, secret, validityPeriod);
    }

    class AccessTokenFactoryImpl implements AccessTokenFactory {
        public static Integer DEFAULT_RAND_MAX_SEED = 100;

        public static Integer DEFAULT_RAND_STRING_SIZE = 20;

        private static final RandomGenerator rng = HashGeneratorHelper
                .getRandomGenerator(Math.toIntExact(System.currentTimeMillis() % DEFAULT_RAND_MAX_SEED));

        private PublicKey publicKey;

        private PrivateKey privateKey;

        private final String secret;

        private final long validityPeriod;

        private OAuth2TokenRepository tokenRepository;

        public AccessTokenFactoryImpl(final OAuth2TokenRepository tokenRepository,
                                      final String secret, final long validityPeriod) {
            this.tokenRepository = tokenRepository;
            this.secret = secret;
            this.validityPeriod = validityPeriod;
        }

        public BaseOAuth2AccessToken createAccessToken(final UserDetails userDetails)
                throws IOException  {
            final String secretStr = (publicKey != null && privateKey != null) ? genSecretStr() : secret;
            final BaseOAuth2AccessToken accessToken =
                    new BaseOAuth2AccessToken(secretStr, validityPeriod);
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
            tokenRepository.save(accessToken);

            return accessToken;
        }

        public BaseOAuth2AccessToken reIssueAccessToken(final BaseOAuth2RefreshToken refreshToken)
                throws IOException, NoSuchAlgorithmException {
            if (!isValidRefreshToken(refreshToken)) {
                return null;
            }
            Optional<BaseOAuth2AccessToken> accessToken = tokenRepository.findByRefreshToken(refreshToken);
            if (!accessToken.isPresent()) {
                return null;
            }
            accessToken.get().setExpiresIn(validityPeriod);
            final String digest = HashGeneratorHelper.mixTwoString(secret,
                    accessToken.get().getExpiration().toString());
            accessToken.get().setValue(digest);
            accessToken.get().setValue(CryptoHelper.getObjectDigest(accessToken.get()));
            tokenRepository.save(accessToken.get());

            return accessToken.get();
        }

        protected boolean isValidRefreshToken(final BaseOAuth2RefreshToken refreshToken)
                throws IOException, NoSuchAlgorithmException {
            if (!isNotExpired(refreshToken.getExpiration())
                    || !isNotFaked(refreshToken) || !isExist(refreshToken)) {
                return false;
            }

            return true;
        }

        protected boolean isNotExpired(final Date expiration) {
            return expiration != null && expiration
                    .before(Date.from(DateTimeHelper.getLocalCurrentTime().toInstant(ZoneOffset.UTC)));
        }

        protected boolean isNotFaked(final BaseOAuth2RefreshToken refreshToken)
                throws IOException, NoSuchAlgorithmException {
            final String digest = refreshToken.getValue();
            refreshToken.setValue(secret);

            return CryptoHelper.getObjectDigest(refreshToken).equals(digest);
        }

        protected boolean isExist(final BaseOAuth2RefreshToken refreshToken) {
            return tokenRepository.findByRefreshToken(refreshToken).isPresent();
        }

        public String genSecretStr() throws IOException {
            final Long creationTime = System.currentTimeMillis();
            try {
                final String randStr = HashGeneratorHelper.getRandSequence(rng, DEFAULT_RAND_STRING_SIZE);
                final String mixStr = HashGeneratorHelper.mixTwoString(secret, randStr);
                final byte[] sign = CryptoHelper.signMessage(mixStr.concat(":" + creationTime), privateKey);

                return DatatypeConverter.printHexBinary(sign).concat(":").concat(randStr).concat(":" + creationTime);

            } catch (final NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
                throw new IOException(ex);
            }
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
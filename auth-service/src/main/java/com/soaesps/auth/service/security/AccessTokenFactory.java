package com.soaesps.auth.service.security;

import com.soaesps.auth.repository.OAuth2TokenRepository;

import com.soaesps.core.DataModels.security.BaseOAuth2AccessToken;
import com.soaesps.core.DataModels.security.BaseOAuth2RefreshToken;
import com.soaesps.core.Utils.CryptoHelper;
import com.soaesps.core.Utils.DateTimeHelper;

import com.soaesps.core.Utils.HashGeneratorHelper;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;

public interface AccessTokenFactory {
    BaseOAuth2AccessToken createAccessToken(final UserDetails userDetails) throws IOException;

    static AccessTokenFactory getInstance(final OAuth2TokenRepository tokenRepository,
                                   final String secret, final long validityPeriod) {
        return new AccessTokenFactoryImpl(tokenRepository, secret, validityPeriod);
    }

    class AccessTokenFactoryImpl implements AccessTokenFactory {
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
            final BaseOAuth2AccessToken accessToken =
                    new BaseOAuth2AccessToken(secret, validityPeriod);

            final BaseOAuth2RefreshToken refreshToken =
                    new BaseOAuth2RefreshToken(secret, 10 * validityPeriod);
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
    }
}
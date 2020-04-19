package com.soaesps.auth.repository;

import com.soaesps.core.DataModels.security.BaseOAuth2AccessToken;

import com.soaesps.core.DataModels.security.BaseOAuth2RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("tokenRepository")
public interface OAuth2TokenRepository extends JpaRepository<BaseOAuth2AccessToken, Long> {
    Optional<BaseOAuth2AccessToken> findByRefreshToken(final BaseOAuth2RefreshToken refreshToken);
}
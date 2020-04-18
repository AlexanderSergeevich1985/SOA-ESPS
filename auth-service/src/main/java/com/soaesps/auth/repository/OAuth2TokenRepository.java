package com.soaesps.auth.repository;

import com.soaesps.auth.domain.CustomAuthenticationToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuth2TokenRepository extends JpaRepository<CustomAuthenticationToken, Long> {
    @Override
    Optional<CustomAuthenticationToken> findById(Long aLong);
}
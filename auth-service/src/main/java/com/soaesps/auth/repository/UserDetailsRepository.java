package com.soaesps.auth.repository;

import com.soaesps.core.DataModels.security.BaseUserDetails;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDetailsRepository extends JpaRepository<BaseUserDetails, Long> {
    Optional<BaseUserDetails> findByUserName(String name);
}
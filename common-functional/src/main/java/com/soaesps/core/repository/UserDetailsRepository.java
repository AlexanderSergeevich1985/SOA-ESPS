package com.soaesps.core.repository;

import com.soaesps.core.DataModels.security.BaseUserDetails;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDetailsRepository extends JpaRepository<BaseUserDetails, Long> {
}
package com.soaesps.core.security.repository;

import com.soaesps.core.DataModels.security.AuthAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
@Transactional
public interface AuthAuditRepository extends JpaRepository<AuthAudit, Integer> {
}
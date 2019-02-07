package com.soaesps.auth.repository;

import com.soaesps.core.DataModels.security.BaseUserDetails;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDetailsRepository extends CrudRepository<BaseUserDetails, String> {
    Optional<BaseUserDetails> findById(final long id);
    Optional<BaseUserDetails> findByName(String name);
}
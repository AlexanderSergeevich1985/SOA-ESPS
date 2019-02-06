package com.soaesps.auth.repository;

import com.soaesps.core.DataModels.security.BaseUserDetails;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDetailsRepository extends CrudRepository<BaseUserDetails, String> {
}

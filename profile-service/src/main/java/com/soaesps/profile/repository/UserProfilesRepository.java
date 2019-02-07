package com.soaesps.profile.repository;

import com.soaesps.core.DataModels.security.UserProfile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfilesRepository extends CrudRepository<UserProfile, Long> {
    Optional<UserProfile> findByName(final String name);
}
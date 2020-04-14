package com.soaesps.profile.repository;

import com.soaesps.core.DataModels.security.UserProfile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfilesRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByName(final String name);
}